package com.fmning.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableOAuth2Sso
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final ToolsProperties toolsProperties;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        String[] urls = toolsProperties.isProduction() ? new String[]{"/login-redirect", "/api/posts/**", "/api/email"}
            : new String[]{};

        http
                .antMatcher("/**")
                .authorizeRequests()
                .antMatchers(urls)
                .authenticated()
                .anyRequest().permitAll()
                .and()
                .logout().logoutSuccessUrl("https://sso.fmning.com/authentication/logout").permitAll()
                .and().csrf().disable();
    }

    @Bean
    public PrincipalExtractor toolsPrincipalExtractor(ObjectMapper objectMapper) {
        return new ToolsPrincipalExtractor(objectMapper);
    }
}
