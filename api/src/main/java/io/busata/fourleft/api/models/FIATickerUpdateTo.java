package io.busata.fourleft.api.models;



public record FIATickerUpdateTo(
        String title,
        String tickerEventKey,
        Long dateTime,
        String text,
        String imageUrl
) {
}
