// Sau này tìm tour không thấy, user không thấy, booking không thấy thì dùng exception này.
package com.wedservice.backend.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}