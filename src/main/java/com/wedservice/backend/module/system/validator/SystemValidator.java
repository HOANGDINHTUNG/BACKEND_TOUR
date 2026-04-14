package com.wedservice.backend.module.system.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SystemValidator {

    public String normalizeApplicationName(String applicationName) {
        if (!StringUtils.hasText(applicationName)) {
            return "wedservice-backend";
        }
        return applicationName.trim();
    }
}
