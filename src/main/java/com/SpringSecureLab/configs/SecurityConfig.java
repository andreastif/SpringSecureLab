package com.SpringSecureLab.configs;

import com.SpringSecureLab.filters.BlacklistedTokenFilter;
import com.SpringSecureLab.filters.DecryptTokenFilter;
import com.SpringSecureLab.filters.ExtractAuthenticationFilter;
import com.SpringSecureLab.utils.RSAKeyProperties;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@Configuration
public class SecurityConfig {
    private final RSAKeyProperties keyProperties;

    private final BlacklistedTokenFilter blacklistedTokenFilter;

    private final DecryptTokenFilter decryptTokenFilter;

    @Autowired
    public SecurityConfig(RSAKeyProperties keyProperties, BlacklistedTokenFilter blacklistedTokenFilter, DecryptTokenFilter decryptTokenFilter) {
        this.keyProperties = keyProperties;
        this.blacklistedTokenFilter = blacklistedTokenFilter;
        this.decryptTokenFilter = decryptTokenFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    //this AuthenticationManager is for finding and processing users when a user tries to log in
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService uds) {
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(uds);
        daoProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(daoProvider);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //https://spring.academy/courses/spring-academy-secure-rest-api-oauth2/lessons/the-big-picture
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        //See https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html#oauth2resourceserver-jwt-architecture
        http.oauth2ResourceServer(conf -> conf.jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(jwtDecoder()) //the decoder
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()) //This is if we want to customize how the decoded JWT is to be set into the spring security context
        ));

        http.authorizeHttpRequests(config -> config
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/members").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/members/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/confirm").permitAll()
                        .requestMatchers(HttpMethod.GET, "/.well-known/jwks.json").permitAll()
                        .anyRequest().authenticated()
                ); // authorization

        //adding my custom filter that extracts the JWT from the cookie and provide it to the authenticationfilter
        http.addFilterBefore(new ExtractAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(decryptTokenFilter, ExtractAuthenticationFilter.class);
        http.addFilterAfter(blacklistedTokenFilter, ExtractAuthenticationFilter.class);

        http.cors(corsConfig -> corsConfigurationSource());

                //read up on when CSRF is needed, here https://www.baeldung.com/csrf-stateless-rest-api
                //and if you want to implement, here https://www.baeldung.com/spring-security-csrf
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }


    //We inject this to the SecurityFilterChain above, this is the new DSL configuration
    //https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html#oauth2resourceserver-jwt-decoder-dsl

    @Bean
    public JwtDecoder jwtDecoder() {
        //adjusting our decoder so that we can check for more claims like issuer and audience.
        //we would add a claim validator just like the aud one if we have more custom claims
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(keyProperties.getPublicKey()).build();
        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> issuerValidator = new JwtIssuerValidator("http://localhost:8080"); //the one issuing the token
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>("aud", aud -> aud.contains("http://localhost:8080")); //whom the token is for
        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(defaultValidators, issuerValidator, audienceValidator); //adding it all inside the decoder
        jwtDecoder.setJwtValidator(combinedValidator);

        return jwtDecoder;
    }

    //This is for extracting the JWT roles and adding them to the context authorities since spring wont pick it up automatically
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles"); //telling Spring Security WHERE our roles are located (default scp/scope)
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(""); //i have no prefixes, all names live in DB, so no need to append here
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimDelimiter(","); //my lines are delimited by "," not " "
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtConverter;
    }



    //this has nothing to do with OAuthResource server, here we are simulating a Authorization Server that mints tokens.
    @Bean
    public JwtEncoder jwtEncoder() {

        JWK jwk = new RSAKey
                .Builder(keyProperties.getPublicKey())
                .privateKey(keyProperties.getPrivateKey())
                .build();

        JWKSource<SecurityContext> jwkSet = new ImmutableJWKSet<>(new JWKSet(jwk));

        return new NimbusJwtEncoder(jwkSet);
    }


    //if you name this to corsConfigurationSource(), then you can use the Customzier.withDefaults() in the filter above because it will auto inject this bean
    //if you name it something else or have more than one config with different names, you have to inject the wanted bean manually like this:
    //.cors(config -> config.configurationSource(corsConfigBean())
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        //https://www.youtube.com/watch?v=HRwlT_etr60 Dan Vega - CORS explanation
        //https://reflectoring.io/spring-cors/ great written summary/guide
        //This defines a bean that can be used to inject into different CORS configuration sections

        CorsConfiguration configuration = new CorsConfiguration();
        //the "who" that is being accepted, p.s, the moz is the RESTer mozilla extension origin
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "moz-extension://ca99f7ae-5a3d-46e7-a87d-6eda4792c909"));
        //the "what" is being accepted, Options = pre-flight request
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        //This is for sending credentials through cookies. If false, you can use headers for basic or authorization tokens instead
        //if you want a JWT httponly cookie, this must be set to true
        //also, if this is true, we must add the headers that are allowed too!
        configuration.setAllowCredentials(true);
//
//        //what headers to accept
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); //'*' is for all headers

        //List of headers that are set in the actual response header
//        configuration.setExposedHeaders();

        //Default maxAge is set to 1800 seconds (30 minutes). Indicates how long the preflight responses can be cached.
        //A preflight response is like a "ping" the origin (e.g. SPA) does to the backend that asks "Hey can i actually access you?"
        //and if the IP adress is in the setAllowedOrigins, the backend will say "Yes!"
        //This maxAge thing tells the frontend that the backend will allow the "Yes" to be valid for X amount of time.
        //just note that spring security automatically disallows all caching (Cache-Control) so this doesn't even matter
        configuration.setMaxAge(600L);

        //the "which", ie which paths are the CORS config is applied to
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
