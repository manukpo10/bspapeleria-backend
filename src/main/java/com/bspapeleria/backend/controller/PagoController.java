package com.bspapeleria.backend.controller;

import com.bspapeleria.backend.dto.MercadoPagoPreferenceRequest;
import com.bspapeleria.backend.dto.PreferenceResponse;
import com.bspapeleria.backend.service.OrdenService;
import com.bspapeleria.backend.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final OrdenService ordenService;

    @PostMapping("/mercado-pago/crear")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PreferenceResponse> crearPreference(
            @RequestBody MercadoPagoPreferenceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pagoService.crearPreference(request));
    }

    @PostMapping("/mercado-pago/webhook")
    public ResponseEntity<Void> webhook(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String id) {
        pagoService.procesarWebhook(topic, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/mercado-pago/config")
    public ResponseEntity<Map<String, Object>> getMercadoPagoConfig() {
        return ResponseEntity.ok(Map.of(
                "configured", pagoService.isConfigured(),
                "sandbox", true
        ));
    }
}