package com.wedservice.backend.module.destinations.controller;

import com.wedservice.backend.module.destinations.entity.CrowdLevel;
import com.wedservice.backend.module.destinations.entity.Destination;
import com.wedservice.backend.module.destinations.entity.DestinationStatus;
import com.wedservice.backend.module.destinations.repository.DestinationRepository;
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

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminDestinationUpdateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    private User adminUser;
    private Destination testDestination;

    @BeforeEach
    void setUp() {
        // Clear repositories
        destinationRepository.deleteAll();
        userRepository.deleteAll();

        // Setup Admin User
        adminUser = User.builder()
                .id(UUID.randomUUID())
                .fullName("Admin User")
                .email("admin@example.com")
                .passwordHash("password")
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();
        userRepository.save(adminUser);

        // Setup Test Destination
        testDestination = Destination.builder()
                .uuid(UUID.randomUUID())
                .code("TEST-001")
                .name("Old Name")
                .slug("old-name")
                .countryCode("VN")
                .province("Old Province")
                .district("Old District")
                .region("Old Region")
                .address("Old Address")
                .shortDescription("Old Short Description")
                .description("Old Description")
                .bestTimeFromMonth(1)
                .bestTimeToMonth(3)
                .crowdLevelDefault(CrowdLevel.LOW)
                .isFeatured(false)
                .isActive(true)
                .isOfficial(false)
                .status(DestinationStatus.APPROVED)
                .mediaList(new ArrayList<>())
                .foods(new ArrayList<>())
                .specialties(new ArrayList<>())
                .activities(new ArrayList<>())
                .tips(new ArrayList<>())
                .events(new ArrayList<>())
                .build();
        destinationRepository.save(testDestination);
    }

    @Test
    void updateDestination_FullUpdate_Success() throws Exception {
        String json = """
        {
          "code": "TEST-NEW",
          "name": "New Name",
          "slug": "new-name",
          "countryCode": "VN",
          "province": "New Province",
          "district": "New District",
          "region": "New Region",
          "address": "New Address",
          "latitude": 10.0,
          "longitude": 100.0,
          "shortDescription": "New Short Description",
          "description": "New Description",
          "bestTimeFromMonth": 6,
          "bestTimeToMonth": 8,
          "crowdLevelDefault": "HIGH",
          "isFeatured": true,
          "isActive": true,
          "isOfficial": true
        }
        """;

        mockMvc.perform(put("/admin/destinations/" + testDestination.getUuid())
                        .with(TestAuthenticationFactory.customUser(adminUser.getId(), adminUser.getEmail(), Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Name"))
                .andExpect(jsonPath("$.data.code").value("TEST-NEW"))
                .andExpect(jsonPath("$.data.crowdLevelDefault").value("HIGH"));

        // Verify DB
        Destination updated = destinationRepository.findByUuid(testDestination.getUuid()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getCode()).isEqualTo("TEST-NEW");
        assertThat(updated.getCrowdLevelDefault()).isEqualTo(CrowdLevel.HIGH);
        assertThat(updated.getIsFeatured()).isTrue();
    }

    @Test
    void updateDestination_PartialUpdate_Success() throws Exception {
        String json = """
        {
          "name": "Updated Name Only"
        }
        """;

        mockMvc.perform(put("/admin/destinations/" + testDestination.getUuid())
                        .with(TestAuthenticationFactory.customUser(adminUser.getId(), adminUser.getEmail(), Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Name Only"))
                .andExpect(jsonPath("$.data.code").value("TEST-001")); // Should be preserved

        // Verify DB
        Destination updated = destinationRepository.findByUuid(testDestination.getUuid()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name Only");
        assertThat(updated.getCode()).isEqualTo("TEST-001");
        assertThat(updated.getProvince()).isEqualTo("Old Province");
    }

    @Test
    void updateDestination_DuplicateCode_BadRequest() throws Exception {
        // Create another destination
        Destination other = Destination.builder()
                .uuid(UUID.randomUUID())
                .code("DUP-001")
                .name("Other")
                .slug("other")
                .status(DestinationStatus.APPROVED)
                .build();
        destinationRepository.save(other);

        String json = """
        {
          "code": "DUP-001",
          "name": "New Name"
        }
        """;

        mockMvc.perform(put("/admin/destinations/" + testDestination.getUuid())
                        .with(TestAuthenticationFactory.customUser(adminUser.getId(), adminUser.getEmail(), Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
