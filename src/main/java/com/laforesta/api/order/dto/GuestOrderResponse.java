package com.laforesta.api.order.dto;

public record GuestOrderResponse(

        OrderResponse order,

        String guestAccessToken

) {
}