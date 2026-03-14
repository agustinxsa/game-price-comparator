package com.gameprice.comparator.service;

import com.gameprice.comparator.dto.GameSearchResponse;
import com.gameprice.comparator.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    public Page<GameSearchResponse> searchGames(String query, int page, int size) {
        return gameRepository.searchByName(query, PageRequest.of(page, size))
            .map(this::toResponse);
    }

    public GameSearchResponse getGameById(Long id) {
        return gameRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new RuntimeException("Game not found"));
    }

    private GameSearchResponse toResponse(com.gameprice.comparator.entity.Game game) {
        return GameSearchResponse.builder()
            .id(game.getId())
            .name(game.getName())
            .description(game.getDescription())
            .imageUrl(game.getImageUrl())
            .releaseDate(game.getReleaseDate())
            .platforms(game.getPlatforms())
            .build();
    }
}