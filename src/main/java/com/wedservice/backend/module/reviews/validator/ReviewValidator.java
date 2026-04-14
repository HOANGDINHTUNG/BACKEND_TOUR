package com.wedservice.backend.module.reviews.validator;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.module.reviews.entity.ReviewSentiment;
import org.springframework.stereotype.Component;

@Component
public class ReviewValidator {

    public void validateRating(Integer rating, String fieldName) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BadRequestException(fieldName + " must be between 1 and 5");
        }
    }

    public String normalizeSentiment(String sentiment) {
        try {
            return ReviewSentiment.fromValue(sentiment).getValue();
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid sentiment");
        }
    }
}
