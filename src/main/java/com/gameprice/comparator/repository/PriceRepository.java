package com.gameprice.comparator.repository;

import com.gameprice.comparator.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    List<Price> findByGameId(Long gameId);

    Optional<Price> findByGameIdAndStoreId(Long gameId, Long storeId);

    @Query("SELECT p FROM Price p WHERE p.game.id = :gameId AND p.isAvailable = true ORDER BY p.amount ASC")
    List<Price> findBestPricesForGame(@Param("gameId") Long gameId);

    @Query("SELECT p FROM Price p WHERE p.game.id IN :gameIds AND p.isAvailable = true")
    List<Price> findPricesByGameIds(@Param("gameIds") List<Long> gameIds);

    void deleteByGameId(Long gameId);
}