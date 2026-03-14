package com.gameprice.comparator.dto;

import com.gameprice.comparator.enums.Currency;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BestPriceInfo {
    private String store;
    private BigDecimal amount;
    private Currency currency;
}