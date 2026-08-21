package com.omnicharge.recharge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
public class RechargeIntegrationTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context loads successfully.
    }
}
