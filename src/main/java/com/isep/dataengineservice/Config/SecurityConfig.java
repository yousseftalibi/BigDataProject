package com.isep.dataengineservice.Config;

import lombok.var;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EnableWebSecurity
@Configuration

public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        List allowedMethods = new ArrayList<String>();
        allowedMethods.add("GET");
        allowedMethods.add("POST");
        allowedMethods.add("PUT");

        http
                .cors(
                        cors -> {
                            cors
                                    .configurationSource(request -> {
                                                var corsConfiguration = new CorsConfiguration();
                                                corsConfiguration.setAllowedOrigins(Collections.singletonList(("*")));
                                                corsConfiguration.setAllowedMethods( allowedMethods );
                                                corsConfiguration.setAllowedHeaders(Collections.singletonList(("*")));
                                                return corsConfiguration;
                                            }
                                    );
                        })
                .csrf().disable();
        return http.build();
    }

}
