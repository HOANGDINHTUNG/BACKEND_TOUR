package com.wedservice.backend.module.system.controller;

import com.wedservice.backend.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/*
    Đây là một REST Controller rất đơn giản dùng để tạo ra API kiểm
    tra xem ứng dụng còn sống hay không, thường gọi là health check.

    Nó thường được dùng để:
        kiểm tra backend có đang hoạt động không
        frontend gọi thử để biết server còn sống không
        deploy xong test nhanh
        monitoring / load balancer / devops kiểm tra service
 */

/*
    Nó là annotation nói với Spring rằng:
        đây là 1 bean do Spring quản lý
        class này nhận HTTP request
        các method bên trong thường trả JSON/XML chứ không trả file HTML

    Nó là sự kết hợp giữa: @Controller, @ResponseBody
 */
@RestController
public class SystemController {

    // khi client gọi đường dẫn này thì nó sẽ mặc định gọi thằng health()
    @GetMapping("/system/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Application is running")
                .data(Map.of(
                        "service", "wedservice-backend",
                        "status", "OK",
                        "time", LocalDateTime.now()
                ))
                .build();
    }
}
