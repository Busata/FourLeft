package io.busata.fourleft.api.models;



public record WRCTickerUpdateTo(
        String title,
        String tickerEventKey,
        Long dateTime,
        String text,
        String imageUrl
) {
}
