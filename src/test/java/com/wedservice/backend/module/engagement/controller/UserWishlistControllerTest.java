package com.wedservice.backend.module.engagement.controller;

import com.wedservice.backend.common.exception.GlobalExceptionHandler;
import com.wedservice.backend.module.engagement.dto.response.WishlistTourResponse;
import com.wedservice.backend.module.engagement.facade.UserWishlistFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserWishlistControllerTest {

    @Mock
    private UserWishlistFacade userWishlistFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JsonMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserWishlistController(userWishlistFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new org.springframework.http.converter.json.JacksonJsonHttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getMyWishlistTours_returnsWrappedApiResponse() throws Exception {
        WishlistTourResponse response = WishlistTourResponse.builder()
                .wishlistId(1L)
                .tourId(10L)
                .tourCode("TOUR-10")
                .tourName("Wishlist Tour")
                .tourSlug("wishlist-tour")
                .basePrice(new BigDecimal("1200000"))
                .currency("VND")
                .durationDays(3)
                .durationNights(2)
                .wishedAt(LocalDateTime.of(2026, 4, 17, 1, 30))
                .build();

        when(userWishlistFacade.getMyWishlistTours()).thenReturn(List.of(response));

        mockMvc.perform(get("/users/me/wishlist/tours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Wishlist tours fetched successfully"))
                .andExpect(jsonPath("$.data[0].tourId").value(10L));
    }

    @Test
    void addMyWishlistTour_returnsWrappedApiResponse() throws Exception {
        WishlistTourResponse response = WishlistTourResponse.builder()
                .wishlistId(2L)
                .tourId(10L)
                .tourCode("TOUR-10")
                .tourName("Wishlist Tour")
                .tourSlug("wishlist-tour")
                .basePrice(new BigDecimal("1200000"))
                .currency("VND")
                .durationDays(3)
                .durationNights(2)
                .wishedAt(LocalDateTime.of(2026, 4, 17, 1, 45))
                .build();

        when(userWishlistFacade.addMyWishlistTour(10L)).thenReturn(response);

        mockMvc.perform(post("/users/me/wishlist/tours/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tour added to wishlist successfully"))
                .andExpect(jsonPath("$.data.wishlistId").value(2L));
    }

    @Test
    void removeMyWishlistTour_returnsWrappedApiResponse() throws Exception {
        mockMvc.perform(delete("/users/me/wishlist/tours/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tour removed from wishlist successfully"));
    }
}
