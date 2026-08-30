package com.odmip.user.service;

import com.odmip.common.exception.BusinessRuleException;
import com.odmip.user.dto.PolicyPatchRequest;
import com.odmip.user.entity.Policy;
import com.odmip.user.entity.PolicyStatus;
import com.odmip.user.repository.PolicyPremiumHistoryRepository;
import com.odmip.user.repository.PolicyRepository;
import com.odmip.user.repository.PolicyTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyTemplateRepository templateRepository;

    @Mock
    private PolicyPremiumHistoryRepository premiumHistoryRepository;

    @InjectMocks
    private PolicyService policyService;

    private Policy draftPolicy;
    private Policy activePolicy;
    private Policy expiredPolicy;

    @BeforeEach
    void setUp() {
        draftPolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-DRAFT")
                .userId(100L)
                .status(PolicyStatus.DRAFT)
                .coverageAmount(new BigDecimal("5000"))
                .premiumAmount(new BigDecimal("4.99"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        activePolicy = Policy.builder()
                .id(2L)
                .policyNumber("POL-ACTIVE")
                .userId(100L)
                .status(PolicyStatus.ACTIVE)
                .coverageAmount(new BigDecimal("5000"))
                .premiumAmount(new BigDecimal("4.99"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        expiredPolicy = Policy.builder()
                .id(3L)
                .policyNumber("POL-EXPIRED")
                .userId(100L)
                .status(PolicyStatus.EXPIRED)
                .coverageAmount(new BigDecimal("5000"))
                .premiumAmount(new BigDecimal("4.99"))
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void testDraftToActiveTransition_Success() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(draftPolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyPatchRequest patchRequest = new PolicyPatchRequest(null, PolicyStatus.ACTIVE);
        Policy updated = policyService.patchPolicy(1L, patchRequest);

        assertEquals(PolicyStatus.ACTIVE, updated.getStatus());
        verify(policyRepository, times(1)).save(draftPolicy);
    }

    @Test
    void testActiveToCancelledTransition_Success() {
        when(policyRepository.findById(2L)).thenReturn(Optional.of(activePolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyPatchRequest patchRequest = new PolicyPatchRequest(null, PolicyStatus.CANCELLED);
        Policy updated = policyService.patchPolicy(2L, patchRequest);

        assertEquals(PolicyStatus.CANCELLED, updated.getStatus());
        verify(policyRepository, times(1)).save(activePolicy);
    }

    @Test
    void testActiveToExpiredTransition_SuccessWhenOverdue() {
        activePolicy.setEndDate(LocalDateTime.now().minusMinutes(1));

        when(policyRepository.findById(2L)).thenReturn(Optional.of(activePolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyPatchRequest patchRequest = new PolicyPatchRequest(null, PolicyStatus.EXPIRED);
        Policy updated = policyService.patchPolicy(2L, patchRequest);

        assertEquals(PolicyStatus.EXPIRED, updated.getStatus());
        verify(policyRepository, times(1)).save(activePolicy);
    }

    @Test
    void testActiveToExpiredTransition_FailureWhenNotOverdue() {
        activePolicy.setEndDate(LocalDateTime.now().plusHours(12));

        when(policyRepository.findById(2L)).thenReturn(Optional.of(activePolicy));

        PolicyPatchRequest patchRequest = new PolicyPatchRequest(null, PolicyStatus.EXPIRED);
        assertThrows(BusinessRuleException.class, () -> {
            policyService.patchPolicy(2L, patchRequest);
        });
        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    void testExpiredToCancelledTransition_Failure() {
        when(policyRepository.findById(3L)).thenReturn(Optional.of(expiredPolicy));

        PolicyPatchRequest patchRequest = new PolicyPatchRequest(null, PolicyStatus.CANCELLED);
        assertThrows(BusinessRuleException.class, () -> {
            policyService.patchPolicy(3L, patchRequest);
        });
        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    void testExpiredToActiveTransition_Failure() {
        when(policyRepository.findById(3L)).thenReturn(Optional.of(expiredPolicy));

        PolicyPatchRequest patchRequest = new PolicyPatchRequest(null, PolicyStatus.ACTIVE);
        assertThrows(BusinessRuleException.class, () -> {
            policyService.patchPolicy(3L, patchRequest);
        });
        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    void testPremiumUpdate_Success() {
        when(policyRepository.findById(2L)).thenReturn(Optional.of(activePolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal newPremium = new BigDecimal("9.99");
        PolicyPatchRequest patchRequest = new PolicyPatchRequest(newPremium, null);
        Policy updated = policyService.patchPolicy(2L, patchRequest);

        assertEquals(newPremium, updated.getPremiumAmount());
        verify(policyRepository, times(1)).save(activePolicy);
    }
}
