package com.wedservice.backend.module.users.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.users.dto.request.UpdateMyProfileRequest;
import com.wedservice.backend.module.users.dto.request.UserAddressRequest;
import com.wedservice.backend.module.users.dto.request.UserDeviceRequest;
import com.wedservice.backend.module.users.dto.request.UserPreferenceRequest;
import com.wedservice.backend.module.users.dto.response.UserAddressResponse;
import com.wedservice.backend.module.users.dto.response.UserDeviceResponse;
import com.wedservice.backend.module.users.dto.response.UserPreferenceResponse;
import com.wedservice.backend.module.users.dto.response.UserResponse;
import com.wedservice.backend.module.users.entity.BudgetLevel;
import com.wedservice.backend.module.users.entity.PreferredTripMode;
import com.wedservice.backend.module.users.entity.Status;
import com.wedservice.backend.module.users.entity.TravelStyle;
import com.wedservice.backend.module.users.facade.UserProfileFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

    @Mock
    private UserProfileFacade userProfileFacade;

    private MockMvc mockMvc;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserProfileController(userProfileFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getMyProfile_returnsWrappedApiResponse() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .fullName("Customer One")
                .email("customer@example.com")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role("CUSTOMER")
                .build();

        when(userProfileFacade.getMyProfile()).thenReturn(response);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Current user fetched successfully"))
                .andExpect(jsonPath("$.data.email").value("customer@example.com"));
    }

    @Test
    void updateMyProfile_returnsWrappedApiResponse() throws Exception {
        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setFullName("Customer Updated");
        request.setEmail("customer.updated@example.com");
        request.setPhone("0987654321");

        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .fullName("Customer Updated")
                .email("customer.updated@example.com")
                .phone("0987654321")
                .status(Status.ACTIVE)
                .role("CUSTOMER")
                .build();

        when(userProfileFacade.updateMyProfile(any(UpdateMyProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                .andExpect(jsonPath("$.data.fullName").value("Customer Updated"));
    }

    @Test
    void updateMyProfile_returnsValidationErrors_whenBodyIsInvalid() throws Exception {
        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setFullName("");
        request.setEmail("invalid-email");
        request.setPhone("");

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.phone").exists());
    }

    @Test
    void getMyAddresses_returnsWrappedApiResponse() throws Exception {
        UserAddressResponse primaryAddress = UserAddressResponse.builder()
                .id(1L)
                .contactName("Nguyen Van A")
                .contactPhone("0901222333")
                .province("Da Nang")
                .district("Hai Chau")
                .ward("Thach Thang")
                .addressLine("123 Tran Phu")
                .isDefault(true)
                .build();

        when(userProfileFacade.getMyAddresses()).thenReturn(List.of(primaryAddress));

        mockMvc.perform(get("/users/me/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User addresses fetched successfully"))
                .andExpect(jsonPath("$.data[0].contactName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.data[0].isDefault").value(true));
    }

    @Test
    void getMyPreferences_returnsWrappedApiResponse() throws Exception {
        UserPreferenceResponse response = UserPreferenceResponse.builder()
                .budgetLevel(BudgetLevel.MEDIUM)
                .preferredTripMode(PreferredTripMode.GROUP)
                .travelStyle(TravelStyle.CULTURE)
                .preferredDepartureCity("Da Nang")
                .favoriteRegions(List.of("Central"))
                .favoriteTags(List.of("culture"))
                .favoriteDestinations(List.of("Hue"))
                .prefersLowMobility(false)
                .prefersFamilyFriendly(true)
                .prefersStudentBudget(false)
                .prefersWeatherAlert(true)
                .prefersPromotionAlert(true)
                .build();

        when(userProfileFacade.getMyPreferences()).thenReturn(response);

        mockMvc.perform(get("/users/me/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User preferences fetched successfully"))
                .andExpect(jsonPath("$.data.budgetLevel").value("medium"))
                .andExpect(jsonPath("$.data.favoriteRegions[0]").value("Central"));
    }

    @Test
    void updateMyPreferences_returnsWrappedApiResponse() throws Exception {
        UserPreferenceRequest request = UserPreferenceRequest.builder()
                .budgetLevel(BudgetLevel.HIGH)
                .preferredTripMode(PreferredTripMode.PRIVATE)
                .travelStyle(TravelStyle.FOOD)
                .preferredDepartureCity("Ho Chi Minh City")
                .favoriteRegions(List.of("South"))
                .favoriteTags(List.of("food"))
                .favoriteDestinations(List.of("Can Tho"))
                .prefersLowMobility(false)
                .prefersFamilyFriendly(true)
                .prefersStudentBudget(false)
                .prefersWeatherAlert(true)
                .prefersPromotionAlert(false)
                .build();
        UserPreferenceResponse response = UserPreferenceResponse.builder()
                .budgetLevel(BudgetLevel.HIGH)
                .preferredTripMode(PreferredTripMode.PRIVATE)
                .travelStyle(TravelStyle.FOOD)
                .preferredDepartureCity("Ho Chi Minh City")
                .favoriteRegions(List.of("South"))
                .favoriteTags(List.of("food"))
                .favoriteDestinations(List.of("Can Tho"))
                .prefersLowMobility(false)
                .prefersFamilyFriendly(true)
                .prefersStudentBudget(false)
                .prefersWeatherAlert(true)
                .prefersPromotionAlert(false)
                .build();

        when(userProfileFacade.updateMyPreferences(any(UserPreferenceRequest.class))).thenReturn(response);

        mockMvc.perform(put("/users/me/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User preferences updated successfully"))
                .andExpect(jsonPath("$.data.preferredTripMode").value("private"))
                .andExpect(jsonPath("$.data.prefersPromotionAlert").value(false));
    }

    @Test
    void getMyDevices_returnsWrappedApiResponse() throws Exception {
        UserDeviceResponse response = UserDeviceResponse.builder()
                .id(10L)
                .platform("android")
                .deviceName("Pixel 8")
                .pushToken("token-1")
                .appVersion("1.2.0")
                .isActive(true)
                .build();

        when(userProfileFacade.getMyDevices()).thenReturn(List.of(response));

        mockMvc.perform(get("/users/me/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User devices fetched successfully"))
                .andExpect(jsonPath("$.data[0].platform").value("android"));
    }

    @Test
    void registerMyDevice_returnsCreatedResponse() throws Exception {
        UserDeviceRequest request = UserDeviceRequest.builder()
                .platform("android")
                .deviceName("Pixel 8")
                .pushToken("token-1")
                .appVersion("1.2.0")
                .build();
        UserDeviceResponse response = UserDeviceResponse.builder()
                .id(10L)
                .platform("android")
                .deviceName("Pixel 8")
                .pushToken("token-1")
                .appVersion("1.2.0")
                .isActive(true)
                .build();

        when(userProfileFacade.registerMyDevice(any(UserDeviceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users/me/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Device registered successfully"))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    void createMyAddress_returnsCreatedResponse() throws Exception {
        UserAddressRequest request = UserAddressRequest.builder()
                .contactName("Nguyen Van A")
                .contactPhone("0901222333")
                .province("Da Nang")
                .district("Hai Chau")
                .ward("Thach Thang")
                .addressLine("123 Tran Phu")
                .isDefault(true)
                .build();
        UserAddressResponse response = UserAddressResponse.builder()
                .id(1L)
                .contactName("Nguyen Van A")
                .contactPhone("0901222333")
                .province("Da Nang")
                .district("Hai Chau")
                .ward("Thach Thang")
                .addressLine("123 Tran Phu")
                .isDefault(true)
                .build();

        when(userProfileFacade.createMyAddress(any(UserAddressRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users/me/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Address created successfully"))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    void setMyDefaultAddress_returnsWrappedApiResponse() throws Exception {
        UserAddressResponse response = UserAddressResponse.builder()
                .id(2L)
                .contactName("Office")
                .contactPhone("0901888999")
                .addressLine("456 Bach Dang")
                .isDefault(true)
                .build();

        when(userProfileFacade.setMyDefaultAddress(2L)).thenReturn(response);

        mockMvc.perform(patch("/users/me/addresses/2/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Default address updated successfully"))
                .andExpect(jsonPath("$.data.isDefault").value(true));
    }

    @Test
    void deleteMyAddress_returnsWrappedApiResponse() throws Exception {
        mockMvc.perform(delete("/users/me/addresses/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Address deleted successfully"));
    }

    @Test
    void deleteMyDevice_returnsWrappedApiResponse() throws Exception {
        mockMvc.perform(delete("/users/me/devices/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Device removed successfully"));
    }
}
