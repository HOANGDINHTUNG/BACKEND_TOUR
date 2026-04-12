package com.wedservice.backend.module.users.event.listener;

import com.wedservice.backend.module.users.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventListener {

    /**
     * Handles UserRegisteredEvent asynchronously.
     * Simulates sending a welcome email.
     */
    @Async
    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Starting async post-registration processing for user: {}", event.email());
        
        try {
            // Simulate time-consuming task (e.g., sending email)
            Thread.sleep(2000);
            log.info("Welcome email sent to: {} ({})", event.fullName(), event.email());
            
            // Further actions could be added here:
            // - Initializing user settings
            // - Sending analytics events
            // - Preparing user dashboard data
            
        } catch (InterruptedException e) {
            log.error("Post-registration processing interrupted for user: {}", event.email(), e);
            Thread.currentThread().interrupt();
        }
        
        log.info("Async post-registration processing completed for user: {}", event.email());
    }
}
