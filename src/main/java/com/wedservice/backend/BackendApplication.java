package com.wedservice.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

// @SpringBootApplication tác dụng của nó sẽ tự quét các package con của com.wedservice.backend
// Nên controller, service, repository, config và component trong dự án đều được đăng ký tự động.
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
