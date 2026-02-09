package com.example.security.security;

import com.example.security.entity.Doctor;
import com.example.security.entity.type.RoleType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.io.IOException;

import static com.example.security.entity.type.RoleType.*;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityFilter {
    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws  Exception{
      httpSecurity.sessionManagement(session->session
                      .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                      .csrf(csrf -> csrf.disable())

              .authorizeHttpRequests(auth->auth
                      .requestMatchers("/public/**","/auth/**").permitAll()
                      .requestMatchers("/admin/**").hasRole(RoleType.ADMIN.name())
                      .requestMatchers("/doctors/**").hasAnyRole(DOCTOR.name(),ADMIN.name())
                      .anyRequest().authenticated())
              .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
              .oauth2Login(oAuth2 -> oAuth2.failureHandler(
                      (request, response, authException) ->
                      {
                       log.error("OAuth2 error:{}",authException.getMessage());
                      })
                      .successHandler(oAuth2SuccessHandler)
              );

      return httpSecurity.build();
    }

}
