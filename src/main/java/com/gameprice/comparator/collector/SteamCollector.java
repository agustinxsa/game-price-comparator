package com.gameprice.comparator.collector;

import com.gameprice.comparator.dto.StorePriceResult;
import com.gameprice.comparator.dto.SteamItem;
import com.gameprice.comparator.dto.SteamSearchResponse;
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
    private static final String STORE_NAME = "STEAM";
    private static final String SEARCH_CACHE_PREFIX = "steam:search:";
    private static final String PRICE_CACHE_PREFIX = "steam:price:";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
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
        if (appId == null) {
            return null;
        }
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
            log.info("Calling Steam API with query: {}", query);
            
            SteamSearchResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("storesearch")
                    .queryParam("term", query)
                    .queryParam("cc", "AR")
                    .queryParam("l", "spanish")
                    .build())
                .retrieve()
                .bodyToMono(SteamSearchResponse.class)
                .timeout(TIMEOUT)
                .block();

            log.info("Steam API response received: {}", response);
            
            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                log.debug("Empty response from Steam search API for query: {}", query);
                return Collections.emptyList();
            }

            log.info("Found {} items from Steam", response.getItems().size());
            
            return response.getItems().stream()
                .limit(MAX_SEARCH_RESULTS)
                .map(this::mapSearchItem)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Failed to fetch search results from Steam for query '{}': {}", query, e.getMessage());
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
                    log.warn("Steam appdetails API error for appId '{}': {}", appId, e.getMessage());
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
                log.debug("Steam appdetails returned no data for appId: {}", appId);
                return null;
            }

            return mapAppDetails(appData, appId);

        } catch (Exception e) {
            log.warn("Failed to fetch price details from Steam for appId '{}': {}", appId, e.getMessage());
            return null;
        }
    }

    private StorePriceResult mapSearchItem(SteamItem item) {
        BigDecimal price = null;
        if (item.getPrice() != null && item.getPrice().getFinalPrice() != null) {
            price = BigDecimal.valueOf(item.getPrice().getFinalPrice()).divide(BigDecimal.valueOf(100));
        }
        
        return StorePriceResult.builder()
            .externalId(String.valueOf(item.getId()))
            .name(item.getName())
            .imageUrl(item.getTinyImage())
            .url("https://store.steampowered.com/app/" + item.getId())
            .platforms(Set.of(Platform.PC))
            .storeCode(STORE_NAME)
            .amount(price)
            .currency(Currency.USD)
            .isAvailable(price != null)
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
        
        if (priceObj instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> priceMap = (java.util.Map<String, Object>) priceObj;
            
            String finalPrice = (String) priceMap.get("final_formatted");
            String originalPrice = (String) priceMap.get("initial_formatted");
            
            if (finalPrice != null) {
                amount = parsePrice(finalPrice);
            }
            if (originalPrice != null) {
                originalAmount = parsePrice(originalPrice);
            }
            
            discountPercent = (Integer) priceMap.get("discount_percent");
        }

        return StorePriceResult.builder()
            .externalId(appId)
            .name(name)
            .imageUrl(thumb)
            .url("https://store.steampowered.com/app/" + appId)
            .platforms(Set.of(Platform.PC))
            .amount(amount)
            .currency(Currency.ARS)
            .originalAmount(originalAmount)
            .discountPercent(discountPercent)
            .isAvailable(amount != null)
            .collectedAt(LocalDateTime.now())
            .storeCode(STORE_NAME)
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
            log.debug("Failed to parse price: {}", priceStr);
            return null;
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
                SteamItem item = new SteamItem();
                item.setId(Long.parseLong(r.getExternalId()));
                item.setName(r.getName());
                item.setTinyImage(r.getImageUrl());
                return item;
            })
            .collect(Collectors.toList()));
        return response;
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
}
