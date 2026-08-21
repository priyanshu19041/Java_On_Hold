package com.omnicharge.operator.service;

import com.omnicharge.operator.entity.RechargePlan;
import com.omnicharge.operator.entity.TelecomOperator;
import com.omnicharge.operator.repository.RechargePlanRepository;
import com.omnicharge.operator.repository.TelecomOperatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OperatorServiceTest {

    @Mock
    private TelecomOperatorRepository operatorRepository;

    @Mock
    private RechargePlanRepository planRepository;

    @InjectMocks
    private OperatorService operatorService;

    private TelecomOperator dummyOperator;
    private RechargePlan dummyPlan;

    @BeforeEach
    void setUp() {
        dummyOperator = new TelecomOperator();
        dummyOperator.setId(1L);
        dummyOperator.setName("TestOperator");

        dummyPlan = new RechargePlan();
        dummyPlan.setId(1L);
        dummyPlan.setOperator(dummyOperator);
        dummyPlan.setPrice(new BigDecimal("199.00"));
        dummyPlan.setValidityDays(28);
    }

    @Test
    void shouldGetAllOperators() {
        when(operatorRepository.findAll()).thenReturn(Arrays.asList(dummyOperator));

        List<TelecomOperator> operators = operatorService.getAllOperators();

        assertNotNull(operators);
        assertEquals(1, operators.size());
        assertEquals("TestOperator", operators.get(0).getName());
        verify(operatorRepository).findAll();
    }

    @Test
    void shouldGetPlansByOperatorId() {
        when(planRepository.findByOperatorId(1L)).thenReturn(Arrays.asList(dummyPlan));

        List<RechargePlan> plans = operatorService.getPlansByOperatorId(1L);

        assertNotNull(plans);
        assertEquals(1, plans.size());
        assertEquals(new BigDecimal("199.00"), plans.get(0).getPrice());
        verify(planRepository).findByOperatorId(1L);
    }

    @Test
    void shouldGetPlanByIdSuccessfully() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(dummyPlan));

        RechargePlan plan = operatorService.getPlanById(1L);

        assertNotNull(plan);
        assertEquals(1L, plan.getId());
        assertEquals(new BigDecimal("199.00"), plan.getPrice());
        verify(planRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenGetPlanByIdNotFound() {
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> operatorService.getPlanById(99L));
        assertEquals("Plan not found", exception.getMessage());
        verify(planRepository).findById(99L);
    }
}
