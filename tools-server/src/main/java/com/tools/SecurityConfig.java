package com.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableOAuth2Sso
//@RequiredArgsConstructor(onConstructor_={@Autowired})
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    //private final LoginUrlFilter loginUrlFilter;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        String[] urls = {"/login-redirect", "/ping"};//, "/api/posts/**"
        http
//                .addFilterBefore(loginUrlFilter, ExceptionTranslationFilter.class)
                .antMatcher("/**")
                .authorizeRequests()
//                .antMatchers("/**").permitAll()
                .antMatchers(urls)// /api/posts/**
                //.permitAll()
                //.anyRequest()
                .authenticated()

                .anyRequest().permitAll()
                .and()
                .logout().logoutSuccessUrl("/").permitAll()
                .and().csrf().disable()

                ;
//        http.addFilterAfter(loginUrlFilter, ExceptionTranslationFilter.class);
//                .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }

    @Bean
    public PrincipalExtractor githubPrincipalExtractor() {
        return new GithubPrincipalExtractor();
    }

//    @Bean
//    public UserAuthenticationConverter userAuthenticationConverter () {
//        return new MyUserConverter();
//    }
//
//    @Bean
//    public AccessTokenConverter accessTokenConverter() {
//        DefaultAccessTokenConverter datc
//                = new DefaultAccessTokenConverter();
//        datc.setUserTokenConverter(userAuthenticationConverter());
//        return datc;
//    }
}
