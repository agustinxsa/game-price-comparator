package com.gameprice.comparator.entity;

import com.gameprice.comparator.enums.Currency;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "prices", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"game_id", "store_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(name = "original_amount", precision = 10, scale = 2)
    private BigDecimal originalAmount;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    private String url;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @PrePersist
    protected void onCreate() {
        if (collectedAt == null) {
            collectedAt = LocalDateTime.now();
        }
    }
}