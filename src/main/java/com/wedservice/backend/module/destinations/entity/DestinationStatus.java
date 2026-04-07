package com.wedservice.backend.module.destinations.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DestinationStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String value;
}
