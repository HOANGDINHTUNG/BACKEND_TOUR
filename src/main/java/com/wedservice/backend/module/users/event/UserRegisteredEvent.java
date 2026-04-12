package com.wedservice.backend.module.users.event;

import java.util.UUID;

/**
 * Event published when a new user is registered.
 */
public record UserRegisteredEvent(
    UUID userId,
    String email,
    String fullName
) {}
