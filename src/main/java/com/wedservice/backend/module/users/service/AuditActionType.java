package com.wedservice.backend.module.users.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditActionType {
    ROLE_CREATE("role.create", "roles"),
    ROLE_UPDATE("role.update", "roles"),
    PERMISSION_ASSIGN("permission.assign", "roles"),
    PROMOTION_CAMPAIGN_CREATE("promotion_campaign.create", "promotion_campaigns"),
    PROMOTION_CAMPAIGN_UPDATE("promotion_campaign.update", "promotion_campaigns"),
    PROMOTION_CAMPAIGN_STATUS_UPDATE("promotion_campaign.status.update", "promotion_campaigns"),
    VOUCHER_CREATE("voucher.create", "vouchers"),
    VOUCHER_UPDATE("voucher.update", "vouchers"),
    VOUCHER_STATUS_UPDATE("voucher.status.update", "vouchers"),
    PRODUCT_CREATE("product.create", "products"),
    PRODUCT_UPDATE("product.update", "products"),
    PRODUCT_STATUS_UPDATE("product.status.update", "products"),
    COMBO_PACKAGE_CREATE("combo_package.create", "combo_packages"),
    COMBO_PACKAGE_UPDATE("combo_package.update", "combo_packages"),
    COMBO_PACKAGE_STATUS_UPDATE("combo_package.status.update", "combo_packages"),
    NOTIFICATION_CREATE("notification.create", "notifications"),
    WEATHER_FORECAST_UPSERT("weather_forecast.upsert", "weather_forecasts"),
    WEATHER_ALERT_CREATE("weather_alert.create", "weather_alerts"),
    WEATHER_ALERT_UPDATE("weather_alert.update", "weather_alerts"),
    WEATHER_ALERT_STATUS_UPDATE("weather_alert.status.update", "weather_alerts"),
    CROWD_PREDICTION_UPSERT("crowd_prediction.upsert", "crowd_predictions"),
    ROUTE_ESTIMATE_CREATE("route_estimate.create", "route_estimates"),
    SCHEDULE_CHAT_ROOM_UPSERT("schedule_chat_room.upsert", "schedule_chat_rooms"),
    SCHEDULE_CHAT_MESSAGE_SEND("schedule_chat_message.send", "schedule_chat_messages"),
    SCHEDULE_CHAT_MEMBER_MUTE_UPDATE("schedule_chat_member.mute.update", "schedule_chat_room_members"),
    BADGE_CREATE("badge.create", "badge_definitions"),
    BADGE_UPDATE("badge.update", "badge_definitions"),
    BADGE_STATUS_UPDATE("badge.status.update", "badge_definitions"),
    PASSPORT_BADGE_GRANT("passport_badge.grant", "passport_badges"),
    USER_CHECKIN_CREATE("user_checkin.create", "user_checkins"),
    SUPPORT_SESSION_ASSIGN("support_session.assign", "support_sessions"),
    SUPPORT_SESSION_STATUS_UPDATE("support_session.status.update", "support_sessions"),
    SUPPORT_MESSAGE_REPLY("support_message.reply", "support_messages"),
    USER_CREATE("user.create", "users"),
    USER_UPDATE("user.update", "users"),
    USER_DEACTIVATE("user.deactivate", "users");

    private final String actionName;
    private final String entityName;
}
