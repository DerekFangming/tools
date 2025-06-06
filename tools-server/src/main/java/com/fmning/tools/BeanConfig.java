package com.fmning.tools;

import com.fmning.tools.domain.Post;
import com.fmning.tools.dto.PostDto;
import com.fmning.tools.service.PostService;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.File;
import java.util.*;

@Configuration
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class BeanConfig {

    private final PostService postService;
    private final ToolsProperties toolsProperties;
    private final APIKeyFilter apiKeyFilter;

    @PostConstruct
    private void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(Post.class, PostDto.class)
                .addMappings(mapper -> {
                    mapper.using((Converter<Integer, List<String>>) context -> {
                        List<String> imageNames = new ArrayList<>();
                        File folder = new File(PostService.IMG_DIR + context.getSource());
                        if (folder.listFiles() != null) {
                            for (File f : Objects.requireNonNull(folder.listFiles())) {
                                String[] components = f.getAbsolutePath().replace("\\", "/").split("/");
                                imageNames.add(components[components.length - 1]);
                            }
                        }

                        return imageNames;
                    }).map(Post::getId, PostDto::setImageNames);

                    mapper.using((Converter<String, List<String>>) context -> {
                        if (context.getSource() == null) {
                            return Collections.emptyList();
                        } else {
                            return Arrays.asList(context.getSource().split(","));
                        }
                    }).map(Post::getImgUrls, PostDto::setImageUrls);

                    mapper.using((Converter<Integer, String>) context -> postService.getPostUrl(context.getSource())).map(Post::getId, PostDto::setUrl);
                });

        return modelMapper;
    }

    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String[] urls = toolsProperties.isProduction() ? new String[]{"/login-redirect", "/api/posts/**", "/api/email"}
                : new String[]{"/whoami", "/whoami1", "/whoami2"};

        http.authorizeHttpRequests((requests) -> requests.requestMatchers(urls).authenticated().anyRequest().permitAll())
                .logout((logout) -> logout
                        .logoutSuccessUrl(toolsProperties.getSsoBaseUrl() + "/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")))
                .oauth2Login((oauth2Login) -> oauth2Login.userInfoEndpoint((userinfo) -> userinfo
                        .userAuthoritiesMapper(this.userAuthoritiesMapper())))
                .oauth2ResourceServer((resourceServer) -> resourceServer.jwt((jwt) ->
                        jwt.jwtAuthenticationConverter(jwtConverter())))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(apiKeyFilter, AnonymousAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkUri = toolsProperties.getSsoBaseUrl() + "/oauth/jwk";
        return NimbusJwtDecoder.withJwkSetUri(jwkUri)
                .build();
    }

    private JwtAuthenticationConverter jwtConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return converter;
    }

    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OAuth2UserAuthority) {
                    Map<String, Object> attributes = ((OAuth2UserAuthority) authority).getAttributes();
                    List<Object> auths = (List<Object>) attributes.get("authorities");
                    for (Object auth : auths) {
                        Map<String, String> authMap = (Map)auth;
                        mappedAuthorities.add(new SimpleGrantedAuthority(authMap.get("authority")));
                    }
                }
            });

            return mappedAuthorities;
        };
    }
}
