package com.wedservice.backend.module.engagement.service.impl;

import com.wedservice.backend.common.security.AuthenticatedUserProvider;
import com.wedservice.backend.common.util.DataNormalizer;
import com.wedservice.backend.module.engagement.dto.request.GenerateTourRecommendationRequest;
import com.wedservice.backend.module.engagement.dto.response.RecommendationLogResponse;
import com.wedservice.backend.module.engagement.dto.response.RecommendedTourResponse;
import com.wedservice.backend.module.engagement.entity.RecommendationLog;
import com.wedservice.backend.module.engagement.entity.UserTourView;
import com.wedservice.backend.module.engagement.entity.WishlistTour;
import com.wedservice.backend.module.engagement.repository.RecommendationLogRepository;
import com.wedservice.backend.module.engagement.repository.UserTourViewRepository;
import com.wedservice.backend.module.engagement.repository.WishlistTourRepository;
import com.wedservice.backend.module.engagement.service.command.UserRecommendationCommandService;
import com.wedservice.backend.module.engagement.service.query.UserRecommendationQueryService;
import com.wedservice.backend.module.tours.entity.Tag;
import com.wedservice.backend.module.tours.entity.Tour;
import com.wedservice.backend.module.tours.entity.TourSeasonality;
import com.wedservice.backend.module.tours.entity.TourStatus;
import com.wedservice.backend.module.tours.repository.TagRepository;
import com.wedservice.backend.module.tours.repository.TourRepository;
import com.wedservice.backend.module.tours.repository.TourSeasonalityRepository;
import com.wedservice.backend.module.tours.repository.TourTagRepository;
import com.wedservice.backend.module.users.entity.BudgetLevel;
import com.wedservice.backend.module.users.entity.PreferredTripMode;
import com.wedservice.backend.module.users.entity.UserPreference;
import com.wedservice.backend.module.users.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRecommendationServiceImpl implements UserRecommendationCommandService, UserRecommendationQueryService {

    private static final TypeReference<List<RecommendedTourResponse>> RECOMMENDATION_LIST_TYPE = new TypeReference<>() {
    };

    private final RecommendationLogRepository recommendationLogRepository;
    private final TourRepository tourRepository;
    private final TourTagRepository tourTagRepository;
    private final TagRepository tagRepository;
    private final TourSeasonalityRepository tourSeasonalityRepository;
    private final WishlistTourRepository wishlistTourRepository;
    private final UserTourViewRepository userTourViewRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Override
    @Transactional
    public RecommendationLogResponse generateMyTourRecommendations(GenerateTourRecommendationRequest request) {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        UserPreference userPreference = userPreferenceRepository.findByUserId(userId).orElse(null);
        ResolvedRecommendationCriteria criteria = resolveCriteria(request, userPreference);

        List<Tour> activeTours = tourRepository.findAll().stream()
                .filter(this::isRecommendationEligibleTour)
                .toList();

        Map<Long, Set<String>> tourTagTextMap = loadTourTagTextMap(activeTours);
        Map<Long, List<TourSeasonality>> tourSeasonalityMap = loadTourSeasonalityMap(activeTours);
        Set<Long> affinityDestinationIds = loadAffinityDestinationIds(userId);

        List<TourRecommendationScoreCard> scoreCards = activeTours.stream()
                .map(tour -> scoreTour(tour, criteria, userPreference, tourTagTextMap, tourSeasonalityMap, affinityDestinationIds))
                .sorted(Comparator
                        .comparing(TourRecommendationScoreCard::score).reversed()
                        .thenComparing((TourRecommendationScoreCard card) -> nullSafe(card.tour().getAverageRating()), Comparator.reverseOrder())
                        .thenComparing((TourRecommendationScoreCard card) -> nullSafe(card.tour().getTotalBookings()), Comparator.reverseOrder())
                        .thenComparing(card -> card.tour().getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(criteria.size())
                .toList();

        List<RecommendedTourResponse> recommendations = scoreCards.stream()
                .map(this::toRecommendedTourResponse)
                .toList();

        RecommendationLog saved = recommendationLogRepository.save(RecommendationLog.builder()
                .userId(userId)
                .requestedTag(criteria.requestedTag())
                .requestedBudget(criteria.requestedBudget())
                .requestedTripMode(criteria.requestedTripMode())
                .requestedPeopleCount(criteria.requestedPeopleCount())
                .requestedDepartureAt(criteria.requestedDepartureAt())
                .generatedResult(writeJson(recommendations))
                .scoringDetail(writeJson(buildScoringDetail(criteria, scoreCards)))
                .build());

        return RecommendationLogResponse.builder()
                .logId(saved.getId())
                .requestedTag(saved.getRequestedTag())
                .requestedBudget(saved.getRequestedBudget())
                .requestedTripMode(saved.getRequestedTripMode())
                .requestedPeopleCount(saved.getRequestedPeopleCount())
                .requestedDepartureAt(saved.getRequestedDepartureAt())
                .recommendations(recommendations)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationLogResponse> getMyRecommendationLogs() {
        UUID userId = authenticatedUserProvider.getRequiredCurrentUserId();
        return recommendationLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toRecommendationLogResponse)
                .toList();
    }

    private RecommendationLogResponse toRecommendationLogResponse(RecommendationLog log) {
        return RecommendationLogResponse.builder()
                .logId(log.getId())
                .requestedTag(log.getRequestedTag())
                .requestedBudget(log.getRequestedBudget())
                .requestedTripMode(log.getRequestedTripMode())
                .requestedPeopleCount(log.getRequestedPeopleCount())
                .requestedDepartureAt(log.getRequestedDepartureAt())
                .recommendations(readRecommendationList(log.getGeneratedResult()))
                .createdAt(log.getCreatedAt())
                .build();
    }

    private List<RecommendedTourResponse> readRecommendationList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, RECOMMENDATION_LIST_TYPE);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private Map<String, Object> buildScoringDetail(
            ResolvedRecommendationCriteria criteria,
            List<TourRecommendationScoreCard> scoreCards
    ) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("strategy", "phase4-personalization-v1");
        detail.put("requestedTag", criteria.requestedTag());
        detail.put("requestedBudget", criteria.requestedBudget() != null ? criteria.requestedBudget().getValue() : null);
        detail.put("requestedTripMode", criteria.requestedTripMode() != null ? criteria.requestedTripMode().getValue() : null);
        detail.put("requestedPeopleCount", criteria.requestedPeopleCount());
        detail.put("requestedDepartureAt", criteria.requestedDepartureAt());
        detail.put("recommendations", scoreCards.stream()
                .map(card -> Map.of(
                        "tourId", card.tour().getId(),
                        "score", normalizeScore(card.score()),
                        "reasons", card.reasons()
                ))
                .toList());
        return detail;
    }

    private TourRecommendationScoreCard scoreTour(
            Tour tour,
            ResolvedRecommendationCriteria criteria,
            UserPreference userPreference,
            Map<Long, Set<String>> tourTagTextMap,
            Map<Long, List<TourSeasonality>> tourSeasonalityMap,
            Set<Long> affinityDestinationIds
    ) {
        double score = 0.0d;
        List<String> reasons = new ArrayList<>();
        Set<String> tagTexts = tourTagTextMap.getOrDefault(tour.getId(), Set.of());

        if (matchesRequestedTag(criteria.requestedTagNormalized(), tagTexts)) {
            score += 45;
            reasons.add("requested_tag_match");
        }
        if (matchesFavoriteTags(criteria.favoriteTagTexts(), tagTexts)) {
            score += 18;
            reasons.add("favorite_tag_match");
        }
        if (matchesTripMode(criteria.requestedTripMode(), tour.getTripMode())) {
            score += 16;
            reasons.add("trip_mode_match");
        }
        if (matchesBudget(criteria.requestedBudget(), tour.getBasePrice())) {
            score += 14;
            reasons.add("budget_match");
        }
        if (criteria.requestedPeopleCount() != null) {
            if (fitsGroupSize(criteria.requestedPeopleCount(), tour)) {
                score += 10;
                reasons.add("group_size_match");
            } else {
                score -= 12;
            }
        }
        if (matchesDepartureMonth(criteria.requestedDepartureAt(), tourSeasonalityMap.getOrDefault(tour.getId(), List.of()))) {
            score += 12;
            reasons.add("seasonality_match");
        }
        if (Boolean.TRUE.equals(userPreference != null ? userPreference.getPrefersFamilyFriendly() : null)
                && Boolean.TRUE.equals(tour.getIsFamilyFriendly())) {
            score += 10;
            reasons.add("family_friendly_preference");
        }
        if (Boolean.TRUE.equals(userPreference != null ? userPreference.getPrefersStudentBudget() : null)
                && Boolean.TRUE.equals(tour.getIsStudentFriendly())) {
            score += 10;
            reasons.add("student_budget_preference");
        }
        if (Boolean.TRUE.equals(userPreference != null ? userPreference.getPrefersLowMobility() : null)
                && isLowMobilityFriendly(tour)) {
            score += 8;
            reasons.add("low_mobility_preference");
        }
        if (tour.getDestination() != null && affinityDestinationIds.contains(tour.getDestination().getId())) {
            score += 9;
            reasons.add("destination_affinity");
        }
        if (Boolean.TRUE.equals(tour.getIsFeatured())) {
            score += 4;
            reasons.add("featured");
        }

        score += nullSafe(tour.getAverageRating()).doubleValue() * 2.0d;
        score += Math.min(nullSafe(tour.getTotalBookings()), 100) / 20.0d;

        return new TourRecommendationScoreCard(tour, score, reasons);
    }

    private boolean matchesRequestedTag(String requestedTag, Set<String> tagTexts) {
        if (!StringUtils.hasText(requestedTag) || tagTexts.isEmpty()) {
            return false;
        }
        return tagTexts.stream().anyMatch(tag -> tag.equals(requestedTag) || tag.contains(requestedTag));
    }

    private boolean matchesFavoriteTags(Set<String> favoriteTags, Set<String> tagTexts) {
        if (favoriteTags == null || favoriteTags.isEmpty() || tagTexts.isEmpty()) {
            return false;
        }
        return favoriteTags.stream().anyMatch(favorite -> tagTexts.stream()
                .anyMatch(tag -> tag.equals(favorite) || tag.contains(favorite) || favorite.contains(tag)));
    }

    private boolean matchesTripMode(PreferredTripMode requestedTripMode, String tourTripMode) {
        return requestedTripMode != null
                && StringUtils.hasText(tourTripMode)
                && requestedTripMode.getValue().equalsIgnoreCase(tourTripMode.trim());
    }

    private boolean matchesBudget(BudgetLevel budgetLevel, BigDecimal basePrice) {
        if (budgetLevel == null || basePrice == null) {
            return false;
        }
        return switch (budgetLevel) {
            case LOW -> basePrice.compareTo(new BigDecimal("1500000")) <= 0;
            case MEDIUM -> basePrice.compareTo(new BigDecimal("1500000")) > 0
                    && basePrice.compareTo(new BigDecimal("3000000")) <= 0;
            case HIGH -> basePrice.compareTo(new BigDecimal("3000000")) > 0
                    && basePrice.compareTo(new BigDecimal("7000000")) <= 0;
            case LUXURY -> basePrice.compareTo(new BigDecimal("7000000")) > 0;
        };
    }

    private boolean fitsGroupSize(Integer requestedPeopleCount, Tour tour) {
        int minGroupSize = nullSafe(tour.getMinGroupSize());
        int maxGroupSize = nullSafe(tour.getMaxGroupSize());
        return requestedPeopleCount >= minGroupSize && requestedPeopleCount <= maxGroupSize;
    }

    private boolean matchesDepartureMonth(LocalDateTime requestedDepartureAt, List<TourSeasonality> seasons) {
        if (requestedDepartureAt == null || seasons == null || seasons.isEmpty()) {
            return false;
        }
        int month = requestedDepartureAt.getMonthValue();
        return seasons.stream()
                .filter(season -> season.getDeletedAt() == null)
                .anyMatch(season -> season.getMonthFrom() != null
                        && season.getMonthTo() != null
                        && month >= season.getMonthFrom()
                        && month <= season.getMonthTo());
    }

    private boolean isLowMobilityFriendly(Tour tour) {
        return nullSafe(tour.getDifficultyLevel()) <= 2 && nullSafe(tour.getActivityLevel()) <= 2;
    }

    private Set<Long> loadAffinityDestinationIds(UUID userId) {
        Set<Long> sourceTourIds = new LinkedHashSet<>();
        sourceTourIds.addAll(wishlistTourRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(WishlistTour::getTourId)
                .toList());
        sourceTourIds.addAll(userTourViewRepository.findByUserIdOrderByViewedAtDesc(userId).stream()
                .map(UserTourView::getTourId)
                .toList());
        if (sourceTourIds.isEmpty()) {
            return Set.of();
        }
        return tourRepository.findAllById(sourceTourIds).stream()
                .filter(this::isRecommendationEligibleTour)
                .map(Tour::getDestination)
                .filter(java.util.Objects::nonNull)
                .map(destination -> destination.getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<Long, Set<String>> loadTourTagTextMap(List<Tour> tours) {
        if (tours.isEmpty()) {
            return Map.of();
        }
        List<Long> tourIds = tours.stream().map(Tour::getId).toList();
        List<com.wedservice.backend.module.tours.entity.TourTag> tourTags = tourTagRepository.findByIdTourIdIn(tourIds);
        if (tourTags.isEmpty()) {
            return Map.of();
        }
        Set<Long> tagIds = tourTags.stream()
                .map(tourTag -> tourTag.getId().getTagId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, Tag> tagMap = tagRepository.findByIdInAndIsActiveTrue(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, tag -> tag));

        Map<Long, Set<String>> result = new HashMap<>();
        for (com.wedservice.backend.module.tours.entity.TourTag tourTag : tourTags) {
            Tag tag = tagMap.get(tourTag.getId().getTagId());
            if (tag == null) {
                continue;
            }
            result.computeIfAbsent(tourTag.getId().getTourId(), key -> new LinkedHashSet<>())
                    .addAll(normalizeTagTexts(tag));
        }
        return result;
    }

    private Map<Long, List<TourSeasonality>> loadTourSeasonalityMap(List<Tour> tours) {
        if (tours.isEmpty()) {
            return Map.of();
        }
        return tourSeasonalityRepository.findByTourIdIn(tours.stream().map(Tour::getId).toList()).stream()
                .filter(seasonality -> seasonality.getDeletedAt() == null)
                .collect(Collectors.groupingBy(TourSeasonality::getTourId));
    }

    private Set<String> normalizeTagTexts(Tag tag) {
        Set<String> texts = new LinkedHashSet<>();
        addNormalizedText(texts, tag.getCode());
        addNormalizedText(texts, tag.getName());
        addNormalizedText(texts, tag.getTagGroup());
        return texts;
    }

    private void addNormalizedText(Collection<String> target, String value) {
        String normalized = normalizeText(value);
        if (normalized != null) {
            target.add(normalized);
        }
    }

    private ResolvedRecommendationCriteria resolveCriteria(
            GenerateTourRecommendationRequest request,
            UserPreference userPreference
    ) {
        String requestedTag = normalizeText(request != null ? request.getRequestedTag() : null);
        Set<String> favoriteTags = normalizeTexts(userPreference != null ? userPreference.getFavoriteTags() : List.of());
        if (!StringUtils.hasText(requestedTag) && !favoriteTags.isEmpty()) {
            requestedTag = favoriteTags.iterator().next();
        }

        BudgetLevel requestedBudget = request != null && request.getRequestedBudget() != null
                ? request.getRequestedBudget()
                : userPreference != null ? userPreference.getBudgetLevel() : null;

        PreferredTripMode requestedTripMode = request != null && request.getRequestedTripMode() != null
                ? request.getRequestedTripMode()
                : userPreference != null ? userPreference.getPreferredTripMode() : null;

        Integer requestedPeopleCount = request != null ? request.getRequestedPeopleCount() : null;
        LocalDateTime requestedDepartureAt = request != null ? request.getRequestedDepartureAt() : null;
        int size = request != null && request.getSize() != null ? request.getSize() : 10;

        return new ResolvedRecommendationCriteria(
                requestedTag,
                requestedTag,
                requestedBudget,
                requestedTripMode,
                requestedPeopleCount,
                requestedDepartureAt,
                favoriteTags,
                size
        );
    }

    private Set<String> normalizeTexts(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        return values.stream()
                .map(this::normalizeText)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeText(String value) {
        String normalized = DataNormalizer.normalize(value);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private boolean isRecommendationEligibleTour(Tour tour) {
        return tour != null
                && tour.getDeletedAt() == null
                && tour.getStatus() == TourStatus.ACTIVE;
    }

    private RecommendedTourResponse toRecommendedTourResponse(TourRecommendationScoreCard card) {
        Tour tour = card.tour();
        return RecommendedTourResponse.builder()
                .tourId(tour.getId())
                .tourCode(tour.getCode())
                .tourName(tour.getName())
                .tourSlug(tour.getSlug())
                .destinationId(tour.getDestination() != null ? tour.getDestination().getId() : null)
                .basePrice(tour.getBasePrice())
                .currency(tour.getCurrency())
                .durationDays(tour.getDurationDays())
                .durationNights(tour.getDurationNights())
                .shortDescription(tour.getShortDescription())
                .isFeatured(tour.getIsFeatured())
                .averageRating(tour.getAverageRating())
                .totalReviews(tour.getTotalReviews())
                .totalBookings(tour.getTotalBookings())
                .recommendationScore(normalizeScore(card.score()))
                .scoringReasons(card.reasons())
                .build();
    }

    private BigDecimal normalizeScore(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private int nullSafe(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private record ResolvedRecommendationCriteria(
            String requestedTag,
            String requestedTagNormalized,
            BudgetLevel requestedBudget,
            PreferredTripMode requestedTripMode,
            Integer requestedPeopleCount,
            LocalDateTime requestedDepartureAt,
            Set<String> favoriteTagTexts,
            Integer size
    ) {
    }

    private record TourRecommendationScoreCard(
            Tour tour,
            double score,
            List<String> reasons
    ) {
    }
}
