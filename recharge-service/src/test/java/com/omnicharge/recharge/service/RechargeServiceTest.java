package com.omnicharge.recharge.service;

import com.omnicharge.recharge.client.OperatorClient;
import com.omnicharge.recharge.client.UserClient;
import com.omnicharge.recharge.config.RabbitMQConfig;
import com.omnicharge.recharge.dto.RechargePlanDto;
import com.omnicharge.recharge.dto.UserDto;
import com.omnicharge.recharge.entity.RechargeRequest;
import com.omnicharge.recharge.repository.RechargeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RechargeServiceTest {

    @Mock
    private RechargeRepository repository;

    @Mock
    private OperatorClient operatorClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private RechargeService rechargeService;

    private RechargeRequest dummyRequest;
    private RechargePlanDto dummyPlan;
    private UserDto dummyUser;

    @BeforeEach
    void setUp() {
        dummyUser = new UserDto();
        dummyUser.setId(1L);
        dummyUser.setPhoneNumber("9876543210");

        dummyRequest = new RechargeRequest();
        dummyRequest.setUserId(1L);
        dummyRequest.setMobileNumber("9876543210");
        dummyRequest.setOperatorId(1L);
        dummyRequest.setPlanId(1L);

        dummyPlan = new RechargePlanDto();
        dummyPlan.setId(1L);
        dummyPlan.setPrice(new BigDecimal("199.00"));
        dummyPlan.setValidityDays(28);
    }

    @Test
    void shouldFailWhenPlanIsInvalid() {
        when(userClient.getUserById(1L)).thenReturn(dummyUser);
        when(operatorClient.getPlanById(1L)).thenThrow(new RuntimeException("Operator service down"));
        when(repository.save(any(RechargeRequest.class))).thenAnswer(i -> i.getArguments()[0]);

        RechargeRequest response = rechargeService.initiateRecharge(dummyRequest);

        assertEquals("FAILED", response.getStatus());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void shouldPublishPendingRecharge() {
        when(userClient.getUserById(1L)).thenReturn(dummyUser);
        when(operatorClient.getPlanById(1L)).thenReturn(dummyPlan);
        when(repository.save(any(RechargeRequest.class))).thenAnswer(i -> {
            RechargeRequest r = (RechargeRequest) i.getArguments()[0];
            r.setId(100L); // simulate save
            return r;
        });

        RechargeRequest response = rechargeService.initiateRecharge(dummyRequest);

        assertEquals("PENDING", response.getStatus());
        assertEquals(new BigDecimal("199.00"), response.getAmount());

        verify(operatorClient).getPlanById(1L);
        verify(repository, times(1)).save(any(RechargeRequest.class));
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.RECHARGE_EXCHANGE), eq(RabbitMQConfig.RECHARGE_ROUTING_KEY), any(RechargeRequest.class));
    }
}
