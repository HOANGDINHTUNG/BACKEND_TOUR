# Lint Errors & Warnings Report - Phase 5

This document lists the remaining lint warnings and compilation-related issues found in the project as of Phase 5 completion.

## 1. Compilation Status

- **Result**: SUCCESS
- **Verification**: `mvn compile` executed successfully.

## 2. General Warnings (JVM/Framework)

- **Lombok Unsafe Access**:
  - `WARNING: A terminally deprecated method in sun.misc.Unsafe has been called by lombok.permit.Permit`
  - _Context_: This is a known issue with Lombok on Java 17+ (this project uses Java 21). It does not affect application functionality but may require a Lombok update in the future.
- **Mockito Dynamic Agent**:
  - `WARNING: Mockito is currently self-attaching to enable the inline-mock-maker...`
  - _Context_: Standard Mockito warning when using the inline-mock-maker on newer JVMs.

## 3. Specific File Warnings (Yellow Lines)

### Controllers

- **AdminScheduleChatController.java**:
  - _Status_: FIXED. Unused `java.util.List` import removed.
- **UserScheduleChatController.java**:
  - _Status_: FIXED. Unused `java.util.List` import removed.

### Services

- **AuditLogService.java**:
  - _Status_: FIXED. Deprecated `JsonNodeFactory.textNode(String)` replaced with `textNode(value)`.
- **UserSupportService.java**:
  - _Status_: FIXED. Missing `RateSupportSessionRequest` import added.

### Tests

- **AdminAuditLogQueryServiceTest.java**:
  - _Status_: FIXED. Unchecked conversion for `Specification<AuditLog>` resolved by providing a typed mock specification.
- **AdminSupportServiceTest.java**:
  - _Status_: FIXED. `InternalNotificationService` import added and constructor mismatch resolved.
- **ScheduleChatServiceTest.java**:
  - _Status_: FIXED. `InternalNotificationService` import added and constructor mismatch resolved.

## 4. Known Semantic Issues (Lints)

- **SupportSession.java**: New fields `rating` and `feedback` were added. Ensure Flyway migrations are updated or run manually if the schema doesn't match the entity yet. (Note: These were added to the Entity but database sync is managed by Flyway).

## Summary

As of now, there are **no blocking red lines (errors)** in the core service, facade, or controller layers for Phase 5. All reported yellow lines related to unused imports or deprecated methods in the affected modules have been addressed.
