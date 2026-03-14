package com.gameprice.comparator.controller;

import com.gameprice.comparator.dto.GameSearchResponse;
import com.gameprice.comparator.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/search")
    public ResponseEntity<Page<GameSearchResponse>> searchGames(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(gameService.searchGames(query, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameSearchResponse> getGame(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGameById(id));
    }
}