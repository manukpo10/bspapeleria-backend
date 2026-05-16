package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.MercadoPagoPreferenceRequest;
import com.bspapeleria.backend.dto.PreferenceItem;
import com.bspapeleria.backend.dto.PreferenceResponse;
import com.bspapeleria.backend.entity.Orden;
import com.bspapeleria.backend.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Value("${mercadopago.public-key:}")
    private String publicKey;

    @Value("${mercadopago.sandbox:true}")
    private boolean sandbox;

    private final OrdenRepository ordenRepository;
    private final OrdenService ordenService;
    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isConfigured() {
        return accessToken != null && !accessToken.isEmpty() && !accessToken.startsWith("TEST-xxxx");
    }

    @Transactional
    public PreferenceResponse crearPreference(MercadoPagoPreferenceRequest request) {
        if (!isConfigured()) {
            log.warn("Mercado Pago no configurado. Retornando preference de prueba.");
            return PreferenceResponse.builder()
                    .preferenceId("MOCK-PREFERENCE-123")
                    .sandboxInitPoint("https://www.mercadopago.com.ar/checkout/start?pref_id=MOCK-PREFERENCE-123")
                    .initPoint("https://www.mercadopago.com.ar/checkout/start?pref_id=MOCK-PREFERENCE-123")
                    .build();
        }

        try {
            String url = "https://api.mercadopago.com/checkout/preferences";

            Map<String, Object> preferenceBody = new HashMap<>();
            preferenceBody.put("items", request.getItems().stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("title", item.getTitle());
                itemMap.put("description", item.getDescription() != null ? item.getDescription() : "");
                itemMap.put("picture_url", item.getPictureUrl() != null ? item.getPictureUrl() : "");
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unit_price", item.getUnitPrice());
                itemMap.put("currency_id", item.getCurrencyId() != null ? item.getCurrencyId() : "ARS");
                return itemMap;
            }).toList());

            preferenceBody.put("payer", Map.of("email", request.getPayerEmail()));
            preferenceBody.put("external_reference", request.getOrdenId());
            preferenceBody.put("notification_url", getNotificationUrl());

            if (sandbox) {
                preferenceBody.put("sandbox_mode", true);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(preferenceBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            Map body = response.getBody();

            if (body == null) {
                throw new RuntimeException("Mercado Pago returned null response");
            }

            String preferenceId = (String) body.get("id");

            if (request.getOrdenId() != null) {
                Orden orden = ordenRepository.findByNumeroOrden(request.getOrdenId()).orElse(null);
                if (orden != null) {
                    orden.setMercadoPagoPreferenceId(preferenceId);
                    ordenRepository.save(orden);
                }
            }

            String sandboxInitPoint = (String) body.get("sandbox_init_point");
            String initPoint = (String) body.get("init_point");

            return PreferenceResponse.builder()
                    .preferenceId(preferenceId)
                    .sandboxInitPoint(sandboxInitPoint)
                    .initPoint(initPoint)
                    .build();

        } catch (Exception e) {
            log.error("Error creando preference de Mercado Pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear el pago: " + e.getMessage());
        }
    }

    @Transactional
    public void procesarWebhook(String topic, String id) {
        if ("payment".equals(topic) && id != null) {
            log.info("Webhook recibido: topic={}, id={}", topic, id);
            try {
                String url = "https://api.mercadopago.com/v1/payments/" + id;
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + accessToken);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                Map payment = response.getBody();
                if (payment != null) {
                    String status = (String) payment.get("status");
                    String externalRef = (String) payment.get("external_reference");
                    if (externalRef != null) {
                        ordenService.actualizarEstadoPagoMercadoPago(externalRef, id, status);
                    }
                }
            } catch (Exception e) {
                log.error("Error procesando webhook: {}", e.getMessage(), e);
            }
        }
    }

    private String getNotificationUrl() {
        return "http://localhost:8080/api/pagos/webhook";
    }
}