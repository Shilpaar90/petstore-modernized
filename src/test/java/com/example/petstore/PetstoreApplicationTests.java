package com.example.petstore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: the Spring application context starts cleanly. This is the first quality gate in
 * the always-green pipeline — every phase must keep this passing.
 */
@SpringBootTest
class PetstoreApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty: fails if any bean wiring / auto-configuration breaks.
    }
}
