package com.gameprice.comparator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamItem {
    private String type;
    private String name;
    private Long id;
    private SteamPrice price;
    
    @JsonProperty("tiny_image")
    private String tinyImage;
}
