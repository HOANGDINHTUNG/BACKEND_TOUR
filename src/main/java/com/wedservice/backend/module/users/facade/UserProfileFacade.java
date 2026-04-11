package com.wedservice.backend.module.users.facade;

import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.service.command.UserProfileCommandService;
import com.wedservice.backend.module.users.service.query.UserProfileQueryService;
import com.wedservice.backend.module.users.validator.UserProfileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileFacade {

    private final UserProfileCommandService commandService;
    private final UserProfileQueryService queryService;
    private final UserProfileValidator validator;

    public UserResponse getMyProfile() {
        return queryService.getMyProfile();
    }

    public UserResponse updateMyProfile(UpdateMyProfileRequest request) {
        String email = request.getEmail();
        String phone = request.getPhone();
        validator.validateRequiredContact(email, phone);
        validator.validateUniqueContacts(email, phone, null);
        return commandService.updateMyProfile(request);
    }
}
