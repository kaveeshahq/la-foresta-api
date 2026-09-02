package com.laforesta.api.notification.event;

import java.util.UUID;

public record GuestTicketEmailEvent(

        UUID orderId,
        String email,
        String fullName,
        String accessToken

) {
}