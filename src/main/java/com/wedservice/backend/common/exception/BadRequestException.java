/*
Dùng khi request hợp lệ về mặt format nhưng sai về mặt nghiệp vụ.

Ví dụ:

email đã tồn tại
tour đã hết chỗ
booking không hợp lệ

Trường hợp create user, nếu email trùng thì dùng exception này.
 */

package com.wedservice.backend.common.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}