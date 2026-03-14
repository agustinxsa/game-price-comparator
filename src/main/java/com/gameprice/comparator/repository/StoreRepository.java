package com.gameprice.comparator.repository;

import com.gameprice.comparator.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByCode(String code);

    List<Store> findByIsActiveTrueOrderByPriorityDesc();

    boolean existsByCode(String code);
}