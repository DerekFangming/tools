package com.fmning.tools;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity
@ConditionalOnProperty("tools.production")
public class MethodSecurityConfiguration {
}
