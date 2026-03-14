package com.gameprice.comparator.collector;

import com.gameprice.comparator.dto.StorePriceResult;
import com.gameprice.comparator.enums.Currency;
import com.gameprice.comparator.enums.Platform;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
public class XboxCollector implements StoreCollector {

    @Override
    public String getStoreCode() {
        return "xbox";
    }

    @Override
    public List<StorePriceResult> searchGames(String query) {
        return List.of(
            StorePriceResult.builder()
                .externalId("xbox_789")
                .name("Sample Game - " + query + " (Xbox)")
                .description("Available on Xbox Store")
                .imageUrl("https://store.xbox.com/image.jpg")
                .url("https://store.xbox.com/product/789")
                .platforms(Set.of(Platform.XBOX))
                .amount(new BigDecimal("24.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("59.99"))
                .discountPercent(58)
                .isAvailable(true)
                .collectedAt(LocalDateTime.now())
                .storeCode(getStoreCode())
                .build()
        );
    }

    @Override
    public StorePriceResult fetchPrices(String externalId) {
        return StorePriceResult.builder()
            .externalId(externalId)
            .name("Sample Game (Xbox)")
            .amount(new BigDecimal("24.99"))
            .currency(Currency.USD)
            .originalAmount(new BigDecimal("59.99"))
            .discountPercent(58)
            .isAvailable(true)
            .collectedAt(LocalDateTime.now())
            .storeCode(getStoreCode())
            .build();
    }
}