package com.auth.authserver2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //https://spring.academy/courses/spring-academy-secure-rest-api-oauth2/lessons/the-big-picture
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .oauth2ResourceServer(config -> config.jwt(Customizer.withDefaults())) // authentication
                .authorizeHttpRequests(config -> config.anyRequest().authenticated()) // authorization
                .httpBasic(Customizer.withDefaults())
                .cors(corsConfig -> corsConfigurationSource())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }


//    @Bean
//    public AuthenticationManager authenticationManager(UserDetailsService uds) {
//        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
//        daoProvider.setUserDetailsService(uds);
//        return new ProviderManager(daoProvider);
//    }

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
        configuration.setAllowedMethods(Arrays.asList("GET","POST", "PUT", "DELETE", "OPTIONS"));

        //This is for sending credentials through cookies. If false, you can use headers for basic or authorization tokens instead
        //if you want a JWT httponly cookie, this must be set to true
        //also, if this is true, we must add the headers that are allowed too!
        configuration.setAllowCredentials(true);

        //what headers to accept
        configuration.setAllowedHeaders(Arrays.asList("Authorization")); //'*' is for all headers

        //List of headers that are set in the actual response header
//        configuration.setExposedHeaders();

        //Default maxAge is set to 1800 seconds (30 minutes). Indicates how long the preflight responses can be cached.
        //A preflight response is like a "ping" the origin (e.g. SPA) does to the backend that asks "Hey can i actually access you?"
        //and if the IP adress is in the setAllowedOrigins, the backend will say "Yes!"
        //This maxAge thing tells the frontend that the backend will allow the "Yes" to be valid for X amount of time.
        configuration.setMaxAge(600L);

        //the "which", ie which paths are the CORS config is applied to
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
