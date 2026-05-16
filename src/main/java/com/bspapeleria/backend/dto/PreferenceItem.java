package com.bspapeleria.backend.dto;

import lombok.Data;

@Data
public class PreferenceItem {
    private String title;
    private String description;
    private String pictureUrl;
    private Long quantity;
    private Double unitPrice;
    private String currencyId = "ARS";
}