package com.wedservice.backend.module.tours.entity;

import com.wedservice.backend.common.entity.AuditableEntity;
import com.wedservice.backend.module.tours.entity.converter.TourScheduleStatusConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tour_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourSchedule extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_code", unique = true, length = 40)
    private String scheduleCode;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(name = "departure_at", nullable = false)
    private LocalDateTime departureAt;

    @Column(name = "return_at", nullable = false)
    private LocalDateTime returnAt;

    @Column(name = "booking_open_at")
    private LocalDateTime bookingOpenAt;

    @Column(name = "booking_close_at")
    private LocalDateTime bookingCloseAt;

    @Column(name = "meeting_at")
    private LocalDateTime meetingAt;

    @Column(name = "meeting_point_name", length = 200)
    private String meetingPointName;

    @Column(name = "meeting_address", columnDefinition = "TEXT")
    private String meetingAddress;

    @Column(name = "meeting_latitude", precision = 10, scale = 7)
    private BigDecimal meetingLatitude;

    @Column(name = "meeting_longitude", precision = 10, scale = 7)
    private BigDecimal meetingLongitude;

    @Column(name = "capacity_total", nullable = false)
    private Integer capacityTotal;

    @Column(name = "booked_seats", nullable = false)
    @Builder.Default
    private Integer bookedSeats = 0;

    @Column(name = "min_guests_to_operate", nullable = false)
    @Builder.Default
    private Integer minGuestsToOperate = 1;

    @Column(name = "adult_price", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal adultPrice = BigDecimal.ZERO;

    @Column(name = "child_price", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal childPrice = BigDecimal.ZERO;

    @Column(name = "infant_price", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal infantPrice = BigDecimal.ZERO;

    @Column(name = "senior_price", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal seniorPrice = BigDecimal.ZERO;

    @Column(name = "single_room_surcharge", precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal singleRoomSurcharge = BigDecimal.ZERO;

    @Column(name = "transport_detail", length = 255)
    private String transportDetail;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Convert(converter = TourScheduleStatusConverter.class)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private TourScheduleStatus status = TourScheduleStatus.DRAFT;

    @Column(name = "remaining_seats", insertable = false, updatable = false)
    private Integer remainingSeats;
}
