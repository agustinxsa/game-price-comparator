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
public class SteamCollector implements StoreCollector {

    @Override
    public String getStoreCode() {
        return "steam";
    }

    @Override
    public List<StorePriceResult> searchGames(String query) {
        return List.of(
            StorePriceResult.builder()
                .externalId("steam_123")
                .name("Sample Game - " + query)
                .description("A great game available on Steam")
                .imageUrl("https://store.steampowered.com/image.jpg")
                .url("https://store.steampowered.com/app/123")
                .platforms(Set.of(Platform.PC))
                .amount(new BigDecimal("29.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("59.99"))
                .discountPercent(50)
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
            .name("Sample Game")
            .amount(new BigDecimal("29.99"))
            .currency(Currency.USD)
            .originalAmount(new BigDecimal("59.99"))
            .discountPercent(50)
            .isAvailable(true)
            .collectedAt(LocalDateTime.now())
            .storeCode(getStoreCode())
            .build();
    }
}