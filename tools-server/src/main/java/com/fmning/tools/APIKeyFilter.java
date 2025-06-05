package com.fmning.tools;

import com.fmning.tools.repository.ConfigRepo;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class APIKeyFilter extends GenericFilterBean {

    private final ConfigRepo configRepo;

    private String apiKey;

    @PostConstruct
    public void init() {
        apiKey = configRepo.findById("API_KEY")
                .orElseThrow(() -> new IllegalStateException("Failed to get API KEY")).getValue();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        String apiKeyValue = ((HttpServletRequest) request).getHeader("x-api-key");

        if (apiKey.equals(apiKeyValue)) {
            Set<GrantedAuthority> mappedAuthorities = Set.of(new SimpleGrantedAuthority("ADMIN"));

            Authentication auth = new UsernamePasswordAuthenticationToken("", "", mappedAuthorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}
