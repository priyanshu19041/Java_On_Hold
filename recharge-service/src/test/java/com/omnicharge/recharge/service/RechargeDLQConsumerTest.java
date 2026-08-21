package com.omnicharge.recharge.service;

import com.omnicharge.recharge.entity.RechargeRequest;
import com.omnicharge.recharge.repository.RechargeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RechargeDLQConsumerTest {

    @Mock
    private RechargeRepository repository;

    @InjectMocks
    private RechargeDLQConsumer dlqConsumer;

    @Test
    void shouldMarkRechargeAsFailed() {
        RechargeRequest request = new RechargeRequest();
        request.setId(1L);
        request.setStatus("PENDING");

        when(repository.findById(1L)).thenReturn(Optional.of(request));

        dlqConsumer.processDeadLetter(request);

        assertEquals("FAILED", request.getStatus());
        verify(repository).save(request);
    }
}
