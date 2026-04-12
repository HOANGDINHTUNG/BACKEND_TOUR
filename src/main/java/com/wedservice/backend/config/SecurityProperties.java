package com.wedservice.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for security-related properties, 
 * primarily used to externalize the public whitelist URLs.
 */
@Configuration
@ConfigurationProperties(prefix = "app.security")
@Getter
@Setter
public class SecurityProperties {

    /**
     * List of URLs that skip authentication filter.
     */
    private List<String> whitelist;
}
