package com.omnicharge.recharge.service;

import com.omnicharge.recharge.entity.RechargeRequest;
import com.omnicharge.recharge.repository.RechargeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RechargeConsumerTest {

    @Mock
    private RechargeRepository repository;

    @InjectMocks
    private RechargeConsumer consumer;

    @Test
    void shouldProcessRechargeSuccessfully() {
        RechargeRequest request = new RechargeRequest();
        request.setId(1L);
        request.setMobileNumber("9876543210");
        request.setStatus("PENDING");

        when(repository.findById(1L)).thenReturn(Optional.of(request));
        
        consumer.processRecharge(request);

        assertEquals("SUCCESS", request.getStatus());
        assertNotNull(request.getPaymentTransactionId());
        verify(repository).save(request);
    }

    @Test
    void shouldThrowExceptionForDeadLetterSimulation() {
        RechargeRequest request = new RechargeRequest();
        request.setId(1L);
        request.setMobileNumber("9999999999"); // DLQ trigger
        request.setStatus("PENDING");

        when(repository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class, () -> consumer.processRecharge(request));
        
        // Should not save success status
        verify(repository, never()).save(any(RechargeRequest.class));
    }
}
