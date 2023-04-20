package io.busata.fourleft.common;

public interface TimeSupplier<T> {

    String getTime(T entry);
}
