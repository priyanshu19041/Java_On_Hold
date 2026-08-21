package com.omnicharge.recharge.repository;

import com.omnicharge.recharge.entity.RechargeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class RechargeRepositoryTest {

    @Autowired
    private RechargeRepository repository;

    @Test
    void shouldSaveAndRetrieveRechargeRequest() {
        RechargeRequest request = new RechargeRequest();
        request.setUserId(1L);
        request.setMobileNumber("9876543210");
        request.setOperatorId(1L);
        request.setPlanId(1L);
        request.setAmount(new BigDecimal("199.00"));
        request.setStatus("PENDING");
        request.setRequestDate(LocalDateTime.now());

        RechargeRequest savedRequest = repository.save(request);

        assertNotNull(savedRequest.getId());

        RechargeRequest retrievedRequest = repository.findById(savedRequest.getId()).orElse(null);
        assertNotNull(retrievedRequest);
        assertEquals("9876543210", retrievedRequest.getMobileNumber());
        assertEquals("PENDING", retrievedRequest.getStatus());
    }
}
