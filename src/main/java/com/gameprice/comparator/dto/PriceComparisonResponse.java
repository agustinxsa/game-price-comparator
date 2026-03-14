package com.gameprice.comparator.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceComparisonResponse {
    private GameSearchResponse game;
    private List<StorePriceInfo> prices;
    private BestPriceInfo bestPrice;
}