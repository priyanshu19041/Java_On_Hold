package com.omnicharge.recharge.service;

import com.omnicharge.recharge.client.OperatorClient;
import com.omnicharge.recharge.client.UserClient;
import com.omnicharge.recharge.config.RabbitMQConfig;
import com.omnicharge.recharge.dto.RechargePlanDto;
import com.omnicharge.recharge.dto.UserDto;
import com.omnicharge.recharge.entity.RechargeRequest;
import com.omnicharge.recharge.repository.RechargeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RechargeService {

    private final RechargeRepository repository;
    private final OperatorClient operatorClient;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    public RechargeRequest initiateRecharge(RechargeRequest request) {
        request.setRequestDate(LocalDateTime.now());
        request.setStatus("PENDING");
        
        // 0. Validate Mobile Number vs Registered Account
        try {
            UserDto user = userClient.getUserById(request.getUserId());
            if (user.getPhoneNumber() == null || !user.getPhoneNumber().equals(request.getMobileNumber())) {
                throw new RuntimeException("Validation Failed: Provided mobile number does not match registered account number");
            }
        } catch (RuntimeException e) {
            log.error("Phone Validation Error", e);
            throw e; // Handled nicely by Global Exception Handler
        }
        
        // 1. Validate Plan with Operator Service
        RechargePlanDto plan;
        try {
            plan = operatorClient.getPlanById(request.getPlanId());
            if (plan == null) throw new RuntimeException("Invalid Plan ID");
        } catch (Exception e) {
            log.error("Failed to fetch plan from Operator Service", e);
            request.setStatus("FAILED");
            return repository.save(request);
        }
        
        request.setAmount(plan.getPrice());
        
        // 2. Save PENDING request and publish to RabbitMQ for asynchronous processing
        RechargeRequest savedRequest = repository.save(request);
        
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.RECHARGE_EXCHANGE, RabbitMQConfig.RECHARGE_ROUTING_KEY, savedRequest);
            log.info("Published recharge request ID: {} to RabbitMQ queue", savedRequest.getId());
        } catch (Exception e) {
            log.error("Failed to publish message to RabbitMQ", e);
            savedRequest.setStatus("FAILED");
            return repository.save(savedRequest);
        }
        
        return savedRequest;
    }
}
