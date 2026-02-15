package com.stadium.booking.controller.api;

import com.stadium.booking.common.result.Result;
import com.stadium.booking.dto.request.RecommendationRequest;
import com.stadium.booking.dto.response.RecommendationResponse;
import com.stadium.booking.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "智能推荐API", description = "预约推荐接口")
@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationApiController {
    private final RecommendationService recommendationService;

    @Operation(summary = "获取推荐方案")
    @PostMapping
    public Result<RecommendationResponse> getRecommendations(
            @RequestBody RecommendationRequest request) {
        return Result.success(recommendationService.getRecommendations(request));
    }
}
