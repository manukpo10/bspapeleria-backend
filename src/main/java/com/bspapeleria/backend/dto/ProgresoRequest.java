package com.bspapeleria.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProgresoRequest {
    private List<Long> leccionesCompletadas;
    private Integer leccionActualId;
}