package io.busata.fourleft.infrastructure.common;

import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class TransactionHandler {

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void runInTransaction(Runnable runnable) {
        runnable.run();
    }
}
