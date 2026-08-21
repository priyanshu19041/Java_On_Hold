package com.omnicharge.recharge.service;

import com.omnicharge.recharge.config.RabbitMQConfig;
import com.omnicharge.recharge.entity.RechargeRequest;
import com.omnicharge.recharge.repository.RechargeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RechargeConsumer {

    private final RechargeRepository repository;

    @RabbitListener(queues = RabbitMQConfig.RECHARGE_QUEUE)
    public void processRecharge(RechargeRequest message) {
        log.info("Received recharge process message for request ID: {}", message.getId());

        RechargeRequest request = repository.findById(message.getId())
                .orElseThrow(() -> new RuntimeException("Recharge request not found: " + message.getId()));

        // Simulate processing payment & activation
        if ("9999999999".equals(request.getMobileNumber()) || (request.getUserId() != null && request.getUserId() == 999L)) {
            log.error("Simulated processing failure for request ID: {}. Sending to DLQ.", request.getId());
            throw new RuntimeException("Simulated processing failure");
        }

        request.setStatus("SUCCESS");
        request.setPaymentTransactionId(UUID.randomUUID().toString());
        repository.save(request);

        log.info("Recharge processed successfully for request ID: {}", request.getId());
    }
}
