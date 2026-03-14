package com.gameprice.comparator.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreInfo {
    private Long id;
    private String name;
    private String code;
}