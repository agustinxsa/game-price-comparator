package com.gameprice.comparator.repository;

import com.gameprice.comparator.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByPriceIdOrderByRecordedAtDesc(Long priceId);

    List<PriceHistory> findByPriceIdInOrderByRecordedAtDesc(List<Long> priceIds);
}