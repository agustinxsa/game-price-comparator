package com.gameprice.comparator.controller;

import com.gameprice.comparator.dto.PriceComparisonResponse;
import com.gameprice.comparator.service.PriceAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceAggregationService priceAggregationService;

    @GetMapping("/compare/{gameId}")
    public ResponseEntity<PriceComparisonResponse> getPriceComparison(@PathVariable Long gameId) {
        return ResponseEntity.ok(priceAggregationService.getPriceComparison(gameId));
    }

    @GetMapping("/search/compare")
    public ResponseEntity<List<PriceComparisonResponse>> searchWithPrices(@RequestParam String query) {
        return ResponseEntity.ok(priceAggregationService.searchWithPrices(query));
    }
}