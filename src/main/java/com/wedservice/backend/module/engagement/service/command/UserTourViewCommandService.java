package com.wedservice.backend.module.engagement.service.command;

public interface UserTourViewCommandService {
    void recordCurrentUserTourViewIfAuthenticated(Long tourId);
}
