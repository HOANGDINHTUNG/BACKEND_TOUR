package com.wedservice.backend.module.user.dto.request;

import com.wedservice.backend.module.user.entity.MemberLevel;
import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.entity.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchRequest {

    @Min(value = 0, message = "Page must be greater than or equal to 0")
    private int page = 0;

    @Min(value = 1, message = "Size must be greater than or equal to 1")
    @Max(value = 100, message = "Size must be less than or equal to 100")
    private int size = 10;

    @Size(max = 100, message = "Keyword must not exceed 100 characters")
    private String keyword;

    private Status status;
    private Role role;
    private MemberLevel memberLevel;

    @Pattern(
            regexp = "id|fullName|displayName|email|phone|role|status|memberLevel|createdAt|updatedAt|lastLoginAt|deletedAt",
            message = "sortBy is invalid"
    )
    private String sortBy = "createdAt";

    @Pattern(
            regexp = "asc|desc",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "sortDir must be asc or desc"
    )
    private String sortDir = "desc";
}
