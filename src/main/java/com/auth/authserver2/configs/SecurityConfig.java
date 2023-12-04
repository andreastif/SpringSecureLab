package com.auth.authserver2.configs;

import com.auth.authserver2.utils.RSAKeyProperties;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@Configuration
public class SecurityConfig {

    //todo: implement key rotation? (watch josh longs video) (nice to have)
    //todo: HttpOnly Cookie + Csrf
    //todo: properties -> issuer + aud check
    //todo: Hookup the current Frontend application


    private final RSAKeyProperties keyProperties;

    @Autowired
    public SecurityConfig(RSAKeyProperties keyProperties) {
        this.keyProperties = keyProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    //this AuthenticationManager is for minting tokens if the user hits the login endpoint and has no token
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
                        .requestMatchers(HttpMethod.POST, "/api/v1/members").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/members/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/confirm").permitAll()
                        .requestMatchers(HttpMethod.GET, "/.well-known/jwks.json").permitAll()
                        .anyRequest().authenticated()
                ); // authorization
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
        return NimbusJwtDecoder.withPublicKey(keyProperties.getPublicKey()).build();
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
                .Builder(keyProperties.getPublicKey()).privateKey(keyProperties.getPrivateKey()).build();
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
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "moz-extension://ca99f7ae-5a3d-46e7-a87d-6eda4792c909", "PostmanRuntime/7.35.0"));
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
