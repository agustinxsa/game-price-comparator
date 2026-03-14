package com.gameprice.comparator.collector;

import com.gameprice.comparator.dto.StorePriceResult;

import java.util.List;

/**
 * Interface for store collectors.
 * Implement this interface to add support for new game stores.
 * New collectors are automatically discovered via Spring's component scanning.
 */
public interface StoreCollector {

    /**
     * Returns the store code this collector handles.
     * Must match a Store.code in the database.
     */
    String getStoreCode();

    /**
     * Search for games by name in this store.
     *
     * @param query Search term
     * @return List of price results containing game info and price
     */
    List<StorePriceResult> searchGames(String query);

    /**
     * Fetch current price for a specific game by external ID.
     *
     * @param externalId The store-specific game ID
     * @return Price result with current price, or null if not found
     */
    StorePriceResult fetchPrices(String externalId);

    /**
     * Check if the collector is available and can make requests.
     *
     * @return true if collector can operate
     */
    default boolean isAvailable() {
        return true;
    }
}