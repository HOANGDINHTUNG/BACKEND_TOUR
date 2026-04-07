package com.wedservice.backend.common.exception;

// Exception tùy biến để biểu diễn nhóm lỗi `BadRequest` trong tầng nghiệp vụ.
// Class này kế thừa `RuntimeException` và chỉ giữ lại constructor nhận `message`. Nó không mang thêm state hay logic xử lý.
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
