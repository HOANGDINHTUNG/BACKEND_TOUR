package com.wedservice.backend.module.destinations.controller;

import com.wedservice.backend.module.users.entity.Role;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.User;
import com.wedservice.backend.module.users.repository.UserRepository;
import com.wedservice.backend.support.TestAuthenticationFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DestinationProposalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = User.builder()
                .id(UUID.randomUUID())
                .fullName("Test User")
                .email("test@example.com")
                .passwordHash("password")
                .role(Role.CUSTOMER)
                .status(Status.ACTIVE)
                .build();
        userRepository.save(testUser);
    }

    @Test
    void reproduceProposalFailure() throws Exception {
        String json = """
        {
          "code": "HN-001",
          "name": "Hồ Hoàn Kiếm",
          "slug": "ho-hoan-kiem",
          "countryCode": "VN",
          "province": "Hà Nội",
          "district": "Hoàn Kiếm",
          "region": "Miền Bắc",
          "address": "Đinh Tiên Hoàng, Hoàn Kiếm, Hà Nội",
          "latitude": 21.0285,
          "longitude": 105.8542,
          "shortDescription": "Hồ Hoàn Kiếm là trái tim của Hà Nội",
          "description": "Hồ Hoàn Kiếm (Hồ Gươm) là hồ nước ngọt nằm giữa lòng Hà Nội...",
          "bestTimeFromMonth": 3,
          "bestTimeToMonth": 5,
          "crowdLevelDefault": "HIGH",
          "isFeatured": true,
          "isActive": true,
          "isOfficial": false,
          "mediaList": [
            {
              "mediaUrl": "https://example.com/hoan-kiem.jpg",
              "mediaType": "IMAGE",
              "caption": "Hồ Hoàn Kiếm nhìn từ trên cao",
              "isPrimary": true
            }
          ],
          "foods": [
            {
              "foodName": "Phở Hà Nội",
              "description": "Phở bò truyền thống Hà Nội",
              "priceRange": "30.000-60.000 VNĐ",
              "imageUrl": "https://example.com/pho.jpg"
            }
          ],
          "activities": [
            {
              "activityName": "Dạo quanh hồ",
              "description": "Tản bộ quanh Hồ Gươm vào buổi sáng",
              "durationHours": 1,
              "isFree": true
            }
          ],
          "tips": [
            {
              "tipContent": "Nên đến vào buổi sáng sớm để tránh đông đúc",
              "category": "TIME",
              "tipTitle": "Thời điểm đẹp"
            }
          ],
          "events": [
            {
              "eventName": "Lễ hội phố đi bộ",
              "description": "Phố đi bộ Hoàn Kiếm mở cuối tuần",
              "month": 12
            }
          ]
        }
        """;

        mockMvc.perform(post("/destinations/propose")
                        .with(TestAuthenticationFactory.customUser(testUser.getId(), testUser.getEmail(), Role.CUSTOMER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }
}
