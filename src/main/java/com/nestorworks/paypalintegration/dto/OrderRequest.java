package com.nestorworks.paypalintegration.dto;

public record OrderRequest(
        Double price,
        String currency,
        String method,
        String intent,
        String description
) {
}
