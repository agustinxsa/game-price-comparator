package com.gameprice.comparator.dto;

import com.gameprice.comparator.enums.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorePriceInfo {
    private StoreInfo store;
    private BigDecimal amount;
    private Currency currency;
    private BigDecimal originalAmount;
    private Integer discountPercent;
    private String url;
    private Boolean isAvailable;
    private LocalDateTime collectedAt;
}