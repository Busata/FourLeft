package io.busata.fourleft.common;

import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class TransactionHandler {

    @Transactional
    public void runInTransaction(Runnable runnable) {
        runnable.run();
    }
}
