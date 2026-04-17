package com.wedservice.backend.module.promotions.dto.request;

import com.wedservice.backend.module.users.entity.MemberLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class PromotionCampaignSearchRequest {

    @Min(value = 0, message = "page must be >= 0")
    private int page = 0;

    @Min(value = 1, message = "size must be >= 1")
    @Max(value = 100, message = "size must be <= 100")
    private int size = 10;

    @Size(max = 100, message = "keyword must not exceed 100 characters")
    private String keyword;

    private Boolean isActive;

    private MemberLevel targetMemberLevel;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startsFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endsTo;

    @Pattern(
            regexp = "code|name|startAt|endAt|targetMemberLevel|isActive|createdAt|updatedAt",
            flags = Pattern.Flag.CASE_INSENSITIVE,
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
