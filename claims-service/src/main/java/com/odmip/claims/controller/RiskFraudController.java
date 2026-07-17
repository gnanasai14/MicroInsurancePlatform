package com.odmip.claims.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.claims.entity.FraudFlag;
import com.odmip.claims.entity.RiskScore;
import com.odmip.claims.repository.FraudFlagRepository;
import com.odmip.claims.repository.RiskScoreRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk")
@Tag(name = "Risk & Fraud", description = "Read-only lookup of risk scores and fraud flags per claim")
public class RiskFraudController {

    private final RiskScoreRepository riskScoreRepository;
    private final FraudFlagRepository fraudFlagRepository;

    public RiskFraudController(RiskScoreRepository riskScoreRepository, FraudFlagRepository fraudFlagRepository) {
        this.riskScoreRepository = riskScoreRepository;
        this.fraudFlagRepository = fraudFlagRepository;
    }

    @GetMapping("/claims/{claimId}/score")
    public ApiResponse<RiskScore> score(@PathVariable Long claimId) {
        return ApiResponse.ok(riskScoreRepository.findByClaimId(claimId)
                .orElseThrow(() -> new com.odmip.common.exception.ResourceNotFoundException(
                        "No risk score for claim " + claimId)));
    }

    @GetMapping("/claims/{claimId}/flags")
    public ApiResponse<List<FraudFlag>> flags(@PathVariable Long claimId) {
        return ApiResponse.ok(fraudFlagRepository.findByClaimId(claimId));
    }
}
