package com.gameprice.comparator.repository;

import com.gameprice.comparator.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Page<Game> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT g FROM Game g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Game> searchByName(@Param("query") String query, Pageable pageable);

    @Query("SELECT g FROM Game g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%')) AND g.id IN " +
           "(SELECT p.game.id FROM Price p WHERE p.store.id = :storeId)")
    Page<Game> searchByNameWithPricesFromStore(@Param("query") String query, @Param("storeId") Long storeId, Pageable pageable);
}