package com.omnicharge.recharge.service;

import com.omnicharge.recharge.client.OperatorClient;
import com.omnicharge.recharge.client.UserClient;
import com.omnicharge.recharge.dto.RechargePlanDto;
import com.omnicharge.recharge.dto.UserDto;
import com.omnicharge.recharge.entity.RechargeRequest;
import com.omnicharge.recharge.repository.RechargeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RechargeService {

    private final RechargeRepository repository;
    private final OperatorClient operatorClient;
    private final UserClient userClient;

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
        
        // 2. Process Recharge
        request.setStatus("SUCCESS");
        request.setPaymentTransactionId(UUID.randomUUID().toString()); // Simulate transaction ID
        
        RechargeRequest savedRequest = repository.save(request);
        log.info("Successfully processed recharge request ID: {}", savedRequest.getId());
        
        return savedRequest;
    }
}
