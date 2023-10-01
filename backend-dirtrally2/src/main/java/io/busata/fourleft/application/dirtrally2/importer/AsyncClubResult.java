package io.busata.fourleft.application.dirtrally2.importer;

import lombok.Value;

import java.util.function.Consumer;

@Value
public class AsyncClubResult<T> {
        T result;
        Consumer<T> postCompletion;


}
