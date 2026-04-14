package com.wedservice.backend.module.system.query;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class SystemQuery {

    public Map<String, Object> health(String applicationName) {
        return Map.of(
                "service", applicationName,
                "status", "OK",
                "time", LocalDateTime.now().toString()
        );
    }
}
