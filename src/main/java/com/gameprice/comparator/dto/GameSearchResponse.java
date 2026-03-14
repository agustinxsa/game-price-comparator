package com.gameprice.comparator.dto;

import com.gameprice.comparator.enums.Platform;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSearchResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private LocalDate releaseDate;
    private Set<Platform> platforms;
}