package io.busata.fourleft.infrastructure.common;

public interface TimeSupplier<T> {

    String getTime(T entry);
}
