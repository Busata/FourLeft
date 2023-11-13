package io.busata.fourleft.backendeasportswrc.domain.models;

import lombok.Getter;

import java.util.Arrays;


public enum EventStatus {
    NOT_STARTED(0L),

    OPEN(1L),
    FINISHED(2L);

    @Getter
    private final Long value;

    EventStatus(Long value) {
        this.value = value;
    }

    public static EventStatus fromValue(Long value) {
        return Arrays.stream(EventStatus.values()).filter(v -> v.value.equals(value)).findFirst().orElseThrow();
    }
}
