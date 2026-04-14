package com.wedservice.backend.module.system.facade;

import com.wedservice.backend.module.system.command.SystemCommand;
import com.wedservice.backend.module.system.query.SystemQuery;
import com.wedservice.backend.module.system.validator.SystemValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SystemFacade {

    private final SystemQuery systemQuery;
    private final SystemCommand systemCommand;
    private final SystemValidator systemValidator;

    public Map<String, Object> health(String applicationName) {
        String normalizedName = systemValidator.normalizeApplicationName(applicationName);
        Map<String, Object> health = new HashMap<>(systemQuery.health(normalizedName));
        health.putAll(systemCommand.ping());
        return health;
    }
}
