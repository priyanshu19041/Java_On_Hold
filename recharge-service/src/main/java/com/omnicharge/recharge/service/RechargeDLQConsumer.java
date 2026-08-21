package com.omnicharge.recharge.service;

import com.omnicharge.recharge.config.RabbitMQConfig;
import com.omnicharge.recharge.entity.RechargeRequest;
import com.omnicharge.recharge.repository.RechargeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RechargeDLQConsumer {

    private final RechargeRepository repository;

    @RabbitListener(queues = RabbitMQConfig.RECHARGE_DLQ)
    public void processDeadLetter(RechargeRequest request) {
        log.warn("Received dead letter recharge request ID: {}", request.getId());
        
        repository.findById(request.getId()).ifPresent(req -> {
            req.setStatus("FAILED");
            repository.save(req);
            log.info("Updated status to FAILED in DLQ handler for request ID: {}", req.getId());
        });
    }
}
