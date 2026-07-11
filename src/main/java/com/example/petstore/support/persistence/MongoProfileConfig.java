package com.example.petstore.support.persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

/**
 * Transaction wiring for the {@code mongo} profile.
 *
 * <p>The application services are annotated {@code @Transactional} (essential for the JPA path's
 * multi-row order writes). On MongoDB every write here is a single self-contained document
 * (registration = one user doc, checkout = one order doc with embedded lines), so it is already
 * atomic and needs no multi-document transaction — which standalone/embedded MongoDB cannot
 * provide anyway (those require a replica set). We therefore supply a no-op transaction manager so
 * {@code @Transactional} is satisfied without changing the application layer. A real replica-set
 * deployment can swap in {@code MongoTransactionManager} with no other change.
 */
@Configuration
@Profile("mongo")
public class MongoProfileConfig {

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return new SimpleTransactionStatus();
            }

            @Override
            public void commit(TransactionStatus status) {
                // single-document writes are atomic; nothing to commit
            }

            @Override
            public void rollback(TransactionStatus status) {
                // no-op: see class javadoc
            }
        };
    }
}
