package com.gameprice.comparator.collector;

import com.gameprice.comparator.dto.StorePriceResult;
import com.gameprice.comparator.enums.Currency;
import com.gameprice.comparator.enums.Platform;
import com.gameprice.comparator.service.ICacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SteamCollector implements StoreCollector {

    private final WebClient webClient;
    private final ICacheService cacheService;

    private static final String STORE_CODE = "steam";
    private static final String SEARCH_CACHE_PREFIX = "steam:search:";
    private static final String PRICE_CACHE_PREFIX = "steam:price:";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_SEARCH_RESULTS = 10;

    public SteamCollector(
            @Qualifier("steamWebClientBuilder") WebClient.Builder webClientBuilder,
            ICacheService cacheService) {
        this.webClient = webClientBuilder.build();
        this.cacheService = cacheService;
    }

    @Override
    public String getStoreCode() {
        return STORE_CODE;
    }

    @Override
    public List<StorePriceResult> searchGames(String query) {
        String cacheKey = SEARCH_CACHE_PREFIX + query.toLowerCase().trim();

        return cacheService.get(cacheKey, SteamSearchResponse.class)
            .map(response -> mapSearchResults(response, query))
            .orElseGet(() -> {
                List<StorePriceResult> results = fetchSearchResults(query);
                if (!results.isEmpty()) {
                    SteamSearchResponse cachedResponse = createSearchResponse(results);
                    cacheService.cache(cacheKey, cachedResponse);
                }
                return results;
            });
    }

    @Override
    public StorePriceResult fetchPrices(String externalId) {
        String appId = extractAppId(externalId);
        String cacheKey = PRICE_CACHE_PREFIX + appId;

        return cacheService.get(cacheKey, StorePriceResult.class)
            .orElseGet(() -> {
                StorePriceResult result = fetchPriceDetails(appId);
                if (result != null) {
                    cacheService.cache(cacheKey, result);
                }
                return result;
            });
    }

    private List<StorePriceResult> fetchSearchResults(String query) {
        try {
            String uri = "/api/storesearch/?term={term}&l=english&cc=US";
            
            SteamSearchResponse response = webClient.get()
                .uri(uri, query)
                .retrieve()
                .bodyToMono(SteamSearchResponse.class)
                .timeout(TIMEOUT)
                .onErrorResume(e -> {
                    log.error("Steam search API error for query '{}': {}", query, e.getMessage());
                    return Mono.empty();
                })
                .block();

            if (response == null || response.getItems() == null) {
                log.warn("Empty response from Steam search API for query: {}", query);
                return Collections.emptyList();
            }

            return response.getItems().stream()
                .limit(MAX_SEARCH_RESULTS)
                .map(this::mapSearchItem)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to fetch search results from Steam for query '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    private StorePriceResult fetchPriceDetails(String appId) {
        try {
            String uri = "/api/appdetails?appids={appId}";
            
            Mono<Object> responseMono = webClient.get()
                .uri(uri, appId)
                .retrieve()
                .bodyToMono(Object.class)
                .timeout(TIMEOUT)
                .onErrorResume(e -> {
                    log.error("Steam appdetails API error for appId '{}': {}", appId, e.getMessage());
                    return Mono.empty();
                });

            Object response = responseMono.block();
            
            if (response == null) {
                return null;
            }

            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> responseMap = (java.util.Map<String, Object>) response;
            java.util.Map<String, Object> appData = (java.util.Map<String, Object>) responseMap.get(appId);
            
            if (appData == null || !Boolean.TRUE.equals(appData.get("success"))) {
                log.warn("Steam appdetails returned no data for appId: {}", appId);
                return null;
            }

            return mapAppDetails(appData, appId);

        } catch (Exception e) {
            log.error("Failed to fetch price details from Steam for appId '{}': {}", appId, e.getMessage());
            return null;
        }
    }

    private StorePriceResult mapSearchItem(SteamSearchItem item) {
        return StorePriceResult.builder()
            .externalId(STORE_CODE + "_" + item.getId())
            .name(item.getName())
            .imageUrl(item.getThumb())
            .url("https://store.steampowered.com/app/" + item.getId())
            .platforms(Set.of(Platform.PC))
            .storeCode(STORE_CODE)
            .isAvailable(true)
            .collectedAt(LocalDateTime.now())
            .build();
    }

    private StorePriceResult mapAppDetails(java.util.Map<String, Object> data, String appId) {
        String name = (String) data.get("name");
        String thumb = (String) data.get("thumb");
        
        Object priceObj = data.get("price_overview");
        BigDecimal amount = null;
        BigDecimal originalAmount = null;
        Integer discountPercent = null;
        Currency currency = Currency.USD;
        
        if (priceObj instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> priceMap = (java.util.Map<String, Object>) priceObj;
            
            String finalPrice = (String) priceMap.get("final_formatted");
            String originalPrice = (String) priceMap.get("initial_formatted");
            String currencyCode = (String) priceMap.get("currency");
            
            if (finalPrice != null) {
                amount = parsePrice(finalPrice);
            }
            if (originalPrice != null) {
                originalAmount = parsePrice(originalPrice);
            }
            
            Integer discount = (Integer) priceMap.get("discount_percent");
            if (discount != null) {
                discountPercent = discount;
            }
            
            if (currencyCode != null) {
                currency = mapCurrency(currencyCode);
            }
        }

        return StorePriceResult.builder()
            .externalId(STORE_CODE + "_" + appId)
            .name(name)
            .imageUrl(thumb)
            .url("https://store.steampowered.com/app/" + appId)
            .platforms(Set.of(Platform.PC))
            .amount(amount)
            .currency(currency)
            .originalAmount(originalAmount)
            .discountPercent(discountPercent)
            .isAvailable(amount != null)
            .collectedAt(LocalDateTime.now())
            .storeCode(STORE_CODE)
            .build();
    }

    private BigDecimal parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return null;
        }
        String cleaned = priceStr.replaceAll("[^0-9.,]", "").replace(",", ".");
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse price: {}", priceStr);
            return null;
        }
    }

    private Currency mapCurrency(String currencyCode) {
        if (currencyCode == null) {
            return Currency.USD;
        }
        try {
            return Currency.valueOf(currencyCode);
        } catch (IllegalArgumentException e) {
            return Currency.USD;
        }
    }

    private String extractAppId(String externalId) {
        if (externalId == null) {
            return null;
        }
        if (externalId.startsWith(STORE_CODE + "_")) {
            return externalId.substring(STORE_CODE.length() + 1);
        }
        return externalId;
    }

    private List<StorePriceResult> mapSearchResults(SteamSearchResponse response, String query) {
        if (response == null || response.getItems() == null) {
            return Collections.emptyList();
        }
        return response.getItems().stream()
            .limit(MAX_SEARCH_RESULTS)
            .map(this::mapSearchItem)
            .collect(Collectors.toList());
    }

    private SteamSearchResponse createSearchResponse(List<StorePriceResult> results) {
        SteamSearchResponse response = new SteamSearchResponse();
        response.setItems(results.stream()
            .map(r -> {
                SteamSearchItem item = new SteamSearchItem();
                String appId = extractAppId(r.getExternalId());
                item.setId(appId);
                item.setName(r.getName());
                item.setThumb(r.getImageUrl());
                return item;
            })
            .collect(Collectors.toList()));
        return response;
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }

    static class SteamSearchResponse {
        private List<SteamSearchItem> items;

        public List<SteamSearchItem> getItems() {
            return items;
        }

        public void setItems(List<SteamSearchItem> items) {
            this.items = items;
        }
    }

    static class SteamSearchItem {
        private String id;
        private String name;
        private String thumb;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getThumb() {
            return thumb;
        }

        public void setThumb(String thumb) {
            this.thumb = thumb;
        }
    }
}