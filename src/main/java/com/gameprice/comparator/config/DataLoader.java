package com.gameprice.comparator.config;

import com.gameprice.comparator.entity.Game;
import com.gameprice.comparator.entity.Price;
import com.gameprice.comparator.entity.Store;
import com.gameprice.comparator.enums.Currency;
import com.gameprice.comparator.enums.Platform;
import com.gameprice.comparator.repository.GameRepository;
import com.gameprice.comparator.repository.PriceRepository;
import com.gameprice.comparator.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("local")
public class DataLoader implements CommandLineRunner {

    private final StoreRepository storeRepository;
    private final GameRepository gameRepository;
    private final PriceRepository priceRepository;

    @Override
    public void run(String... args) {
        if (storeRepository.count() > 0) {
            log.info("Data already exists, skipping seed");
            return;
        }

        log.info("Loading seed data...");

        Store steam = storeRepository.save(Store.builder()
                .name("Steam")
                .code("steam")
                .baseUrl("https://store.steampowered.com")
                .isActive(true)
                .priority(1)
                .build());

        Store playstation = storeRepository.save(Store.builder()
                .name("PlayStation Store")
                .code("playstation")
                .baseUrl("https://store.playstation.com")
                .isActive(true)
                .priority(2)
                .build());

        Store xbox = storeRepository.save(Store.builder()
                .name("Xbox Store")
                .code("xbox")
                .baseUrl("https://www.xbox.com/es-ar/store")
                .isActive(true)
                .priority(3)
                .build());

        Game zelda = gameRepository.save(Game.builder()
                .externalId("zelda-totk")
                .name("The Legend of Zelda: Tears of the Kingdom")
                .description("Embark on a journey through the vast lands of Hyrule.")
                .imageUrl("https://example.com/zelda.jpg")
                .releaseDate(LocalDate.of(2023, 5, 12))
                .platforms(Set.of(Platform.NINTENDO))
                .build());

        Game mario = gameRepository.save(Game.builder()
                .externalId("mario-odyssey")
                .name("Super Mario Odyssey")
                .description("Join Mario on a massive, globe-trotting 3D adventure.")
                .imageUrl("https://example.com/mario.jpg")
                .releaseDate(LocalDate.of(2017, 10, 27))
                .platforms(Set.of(Platform.NINTENDO))
                .build());

        Game godOfWar = gameRepository.save(Game.builder()
                .externalId("god-of-war-ragnarok")
                .name("God of War Ragnarök")
                .description("A new beginning for Kratos.")
                .imageUrl("https://example.com/gow.jpg")
                .releaseDate(LocalDate.of(2022, 11, 9))
                .platforms(Set.of(Platform.PC, Platform.PLAYSTATION))
                .build());

        Game cyberpunk = gameRepository.save(Game.builder()
                .externalId("cyberpunk-2077")
                .name("Cyberpunk 2077")
                .description("An open-world action-adventure story.")
                .imageUrl("https://example.com/cyberpunk.jpg")
                .releaseDate(LocalDate.of(2020, 12, 10))
                .platforms(Set.of(Platform.PC, Platform.PLAYSTATION, Platform.XBOX))
                .build());

        Game minecraft = gameRepository.save(Game.builder()
                .externalId("minecraft")
                .name("Minecraft")
                .description("Create, explore and survive!")
                .imageUrl("https://example.com/minecraft.jpg")
                .releaseDate(LocalDate.of(2011, 11, 18))
                .platforms(Set.of(Platform.PC, Platform.PLAYSTATION, Platform.XBOX, Platform.NINTENDO))
                .build());

        priceRepository.save(Price.builder()
                .game(zelda)
                .store(steam)
                .amount(new BigDecimal("69.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("69.99"))
                .discountPercent(0)
                .url("https://store.steampowered.com/app/1234567")
                .isAvailable(true)
                .collectedAt(LocalDateTime.now())
                .build());

        priceRepository.save(Price.builder()
                .game(zelda)
                .store(playstation)
                .amount(new BigDecimal("59.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("69.99"))
                .discountPercent(14)
                .url("https://store.playstation.com/zelda")
                .isAvailable(true)
                .collectedAt(LocalDateTime.now())
                .build());

        priceRepository.save(Price.builder()
                .game(mario)
                .store(steam)
                .amount(new BigDecimal("59.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("59.99"))
                .discountPercent(0)
                .url("https://store.steampowered.com/mario")
                .isAvailable(false)
                .collectedAt(LocalDateTime.now())
                .build());

        priceRepository.save(Price.builder()
                .game(mario)
                .store(playstation)
                .amount(new BigDecimal("49.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("59.99"))
                .discountPercent(17)
                .url("https://store.playstation.com/mario")
                .isAvailable(true)
                .collectedAt(LocalDateTime.now())
                .build());

        priceRepository.save(Price.builder()
                .game(godOfWar)
                .store(steam)
                .amount(new BigDecimal("49.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("59.99"))
                .discountPercent(17)
                .url("https://store.steampowered.com/gow")
                .isAvailable(true)
                .collectedAt(LocalDateTime.now())
                .build());

        priceRepository.save(Price.builder()
                .game(godOfWar)
                .store(playstation)
                .amount(new BigDecimal("39.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("69.99"))
                .discountPercent(42)
                .url("https://store.playstation.com/gow")
                .isAvailable(true)
                .collectedAt(LocalDateTime.now())
                .build());

        priceRepository.save(Price.builder()
                .game(cyberpunk)
                .store(steam)
                .amount(new BigDecimal("29.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("59.99"))
                .discountPercent(50)
                .url("https://store.steampowered.com/cyberpunk")
                .isAvailable(true)
                .collectedAt(LocalDateTime.now())
                .build());

        priceRepository.save(Price.builder()
                .game(cyberpunk)
                .store(xbox)
                .amount(new BigDecimal("24.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("59.99"))
                .discountPercent(58)
                .url("https://xbox.com/cyberpunk")
                .isAvailable(true)
                .collectedAt(LocalDateTime.now())
                .build());

        priceRepository.save(Price.builder()
                .game(minecraft)
                .store(steam)
                .amount(new BigDecimal("29.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("29.99"))
                .discountPercent(0)
                .url("https://store.steampowered.com/minecraft")
                .isAvailable(true)
                .collectedAt(LocalDateTime.now())
                .build());

        priceRepository.save(Price.builder()
                .game(minecraft)
                .store(xbox)
                .amount(new BigDecimal("29.99"))
                .currency(Currency.USD)
                .originalAmount(new BigDecimal("29.99"))
                .discountPercent(0)
                .url("https://xbox.com/minecraft")
                .isAvailable(true)
                .collectedAt(LocalDateTime.now())
                .build());

        log.info("Seed data loaded: {} stores, {} games, {} prices",
                storeRepository.count(), gameRepository.count(), priceRepository.count());
    }
}
