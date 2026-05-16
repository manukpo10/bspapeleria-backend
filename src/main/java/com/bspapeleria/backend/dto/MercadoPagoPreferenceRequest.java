package com.bspapeleria.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class MercadoPagoPreferenceRequest {
    private List<PreferenceItem> items;
    private String payerEmail;
    private String ordenId;
}