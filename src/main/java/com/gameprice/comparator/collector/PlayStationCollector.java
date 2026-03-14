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
public class PlayStationCollector implements StoreCollector {

    @Override
    public String getStoreCode() {
        return "playstation";
    }

    @Override
    public List<StorePriceResult> searchGames(String query) {
        return List.of(
            StorePriceResult.builder()
                .externalId("ps_456")
                .name("Sample Game - " + query + " (PS)")
                .description("Available on PlayStation Store")
                .imageUrl("https://store.playstation.com/image.jpg")
                .url("https://store.playstation.com/product/456")
                .platforms(Set.of(Platform.PLAYSTATION))
                .amount(new BigDecimal("34.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("69.99"))
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
            .name("Sample Game (PS)")
            .amount(new BigDecimal("34.99"))
            .currency(Currency.USD)
            .originalAmount(new BigDecimal("69.99"))
            .discountPercent(50)
            .isAvailable(true)
            .collectedAt(LocalDateTime.now())
            .storeCode(getStoreCode())
            .build();
    }
}