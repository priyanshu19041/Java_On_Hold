package com.omnicharge.recharge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnicharge.recharge.entity.RechargeRequest;
import com.omnicharge.recharge.service.RechargeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RechargeController.class)
public class RechargeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RechargeService rechargeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldInitiateRechargeSuccessfully() throws Exception {
        RechargeRequest request = new RechargeRequest();
        request.setUserId(1L);
        request.setMobileNumber("9876543210");
        request.setOperatorId(1L);
        request.setPlanId(1L);

        RechargeRequest response = new RechargeRequest();
        response.setId(100L);
        response.setUserId(1L);
        response.setMobileNumber("9876543210");
        response.setStatus("SUCCESS");

        when(rechargeService.initiateRecharge(any(RechargeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/recharges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}
