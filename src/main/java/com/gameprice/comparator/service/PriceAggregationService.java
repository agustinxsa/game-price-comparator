package com.gameprice.comparator.service;

import com.gameprice.comparator.collector.StoreCollector;
import com.gameprice.comparator.dto.*;
import com.gameprice.comparator.entity.Store;
import com.gameprice.comparator.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAggregationService {

    private final List<StoreCollector> collectors;
    private final StoreRepository storeRepository;
    private final ICacheService cacheService;

    private static final String PRICE_CACHE_PREFIX = "price:";
    private static final String SEARCH_CACHE_PREFIX = "search:";

    public List<PriceComparisonResponse> searchWithPrices(String query) {
        String cacheKey = SEARCH_CACHE_PREFIX + query.toLowerCase();

        return cacheService.get(cacheKey, PriceComparisonResponse[].class)
            .map(List::of)
            .orElseGet(() -> {
                List<PriceComparisonResponse> results = performSearch(query);
                cacheService.cache(cacheKey, results);
                return results;
            });
    }

    public PriceComparisonResponse getPriceComparison(Long gameId) {
        String cacheKey = PRICE_CACHE_PREFIX + gameId;

        return cacheService.get(cacheKey, PriceComparisonResponse.class)
            .orElseGet(() -> {
                PriceComparisonResponse response = fetchAndAggregatePrices(gameId);
                cacheService.cache(cacheKey, response);
                return response;
            });
    }

    private List<PriceComparisonResponse> performSearch(String query) {
        log.info("Performing search for query: {}", query);

        Map<String, StoreCollector> collectorMap = collectors.stream()
            .collect(Collectors.toMap(StoreCollector::getStoreCode, Function.identity()));

        List<Store> stores = storeRepository.findByIsActiveTrueOrderByPriorityDesc();

        return stores.stream()
            .map(store -> {
                StoreCollector collector = collectorMap.get(store.getCode());
                if (collector == null) {
                    log.warn("No collector found for store: {}", store.getCode());
                    return null;
                }

                List<StorePriceResult> results = collector.searchGames(query);
                return results.stream()
                    .map(r -> toPriceComparisonResponse(r, store))
                    .collect(Collectors.toList());
            })
            .filter(list -> list != null)
            .flatMap(List::stream)
            .collect(Collectors.groupingBy(PriceComparisonResponse::getGame))
            .values()
            .stream()
            .map(this::mergePriceResults)
            .collect(Collectors.toList());
    }

    private PriceComparisonResponse fetchAndAggregatePrices(Long gameId) {
        log.info("Fetching prices for game ID: {}", gameId);

        Map<String, StoreCollector> collectorMap = collectors.stream()
            .collect(Collectors.toMap(StoreCollector::getStoreCode, Function.identity()));

        List<Store> stores = storeRepository.findByIsActiveTrueOrderByPriorityDesc();

        List<StorePriceInfo> priceInfos = stores.stream()
            .map(store -> {
                StoreCollector collector = collectorMap.get(store.getCode());
                if (collector == null) {
                    return null;
                }

                StorePriceResult result = collector.fetchPrices("game_" + gameId);
                return toStorePriceInfo(result, store);
            })
            .filter(info -> info != null)
            .collect(Collectors.toList());

        StorePriceInfo bestPrice = priceInfos.stream()
            .filter(info -> Boolean.TRUE.equals(info.getIsAvailable()))
            .min((a, b) -> a.getAmount().compareTo(b.getAmount()))
            .orElse(null);

        return PriceComparisonResponse.builder()
            .prices(priceInfos)
            .bestPrice(bestPrice != null ? BestPriceInfo.builder()
                .store(bestPrice.getStore().getName())
                .amount(bestPrice.getAmount())
                .currency(bestPrice.getCurrency())
                .build() : null)
            .build();
    }

    private PriceComparisonResponse toPriceComparisonResponse(StorePriceResult result, Store store) {
        return PriceComparisonResponse.builder()
            .game(GameSearchResponse.builder()
                .name(result.getName())
                .description(result.getDescription())
                .imageUrl(result.getImageUrl())
                .platforms(result.getPlatforms())
                .build())
            .prices(List.of(toStorePriceInfo(result, store)))
            .bestPrice(BestPriceInfo.builder()
                .store(store.getName())
                .amount(result.getAmount())
                .currency(result.getCurrency())
                .build())
            .build();
    }

    private StorePriceInfo toStorePriceInfo(StorePriceResult result, Store store) {
        return StorePriceInfo.builder()
            .store(StoreInfo.builder()
                .id(store.getId())
                .name(store.getName())
                .code(store.getCode())
                .build())
            .amount(result.getAmount())
            .currency(result.getCurrency())
            .originalAmount(result.getOriginalAmount())
            .discountPercent(result.getDiscountPercent())
            .url(result.getUrl())
            .isAvailable(result.getIsAvailable())
            .collectedAt(result.getCollectedAt())
            .build();
    }

    private PriceComparisonResponse mergePriceResults(List<PriceComparisonResponse> results) {
        GameSearchResponse game = results.get(0).getGame();

        List<StorePriceInfo> allPrices = results.stream()
            .flatMap(r -> r.getPrices().stream())
            .collect(Collectors.toList());

        Optional<StorePriceInfo> bestPrice = allPrices.stream()
            .filter(info -> Boolean.TRUE.equals(info.getIsAvailable()))
            .min((a, b) -> a.getAmount().compareTo(b.getAmount()));

        return PriceComparisonResponse.builder()
            .game(game)
            .prices(allPrices)
            .bestPrice(bestPrice.map(bp -> BestPriceInfo.builder()
                .store(bp.getStore().getName())
                .amount(bp.getAmount())
                .currency(bp.getCurrency())
                .build()).orElse(null))
            .build();
    }

    public void evictCache(Long gameId) {
        cacheService.evict(PRICE_CACHE_PREFIX + gameId);
    }

    public void evictSearchCache() {
        cacheService.evictPattern(SEARCH_CACHE_PREFIX + "*");
    }
}