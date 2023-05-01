package com.isep.dataengineservice.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@EnableWebSecurity
@Configuration

public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .cors(
                        cors -> {
                            cors
                                    .configurationSource(request -> {
                                                var corsConfiguration = new CorsConfiguration();
                                                corsConfiguration.setAllowedOrigins(List.of("*"));
                                                corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT"));
                                                corsConfiguration.setAllowedHeaders(List.of("*"));
                                                return corsConfiguration;
                                            }
                                    );
                        })
                .csrf().disable();
        return http.build();
    }

}
