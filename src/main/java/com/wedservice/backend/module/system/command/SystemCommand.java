package com.wedservice.backend.module.system.command;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SystemCommand {

    public Map<String, Object> ping() {
        return Map.of("pong", true);
    }
}
