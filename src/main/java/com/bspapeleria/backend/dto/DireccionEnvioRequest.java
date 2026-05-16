package com.bspapeleria.backend.dto;

import lombok.Data;

@Data
public class DireccionEnvioRequest {
    private String calle;
    private String numero;
    private String piso;
    private String departamento;
    private String ciudad;
    private String provincia;
    private String codigoPostal;
    private String telefono;
    private String instrucciones;
}