package com.gameprice.comparator.dto;

import com.gameprice.comparator.enums.Currency;
import com.gameprice.comparator.enums.Platform;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorePriceResult {

    private String externalId;
    private String name;
    private String description;
    private String imageUrl;
    private String url;
    private Set<Platform> platforms;
    private BigDecimal amount;
    private Currency currency;
    private BigDecimal originalAmount;
    private Integer discountPercent;
    private Boolean isAvailable;
    private LocalDateTime collectedAt;
    private String storeCode;
}