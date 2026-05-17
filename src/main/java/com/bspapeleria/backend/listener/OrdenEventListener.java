package com.bspapeleria.backend.listener;

import com.bspapeleria.backend.entity.Orden;
import com.bspapeleria.backend.entity.OrdenDetalle;
import com.bspapeleria.backend.event.OrdenEstadoActualizadoEvent;
import com.bspapeleria.backend.repository.OrdenRepository;
import com.bspapeleria.backend.service.ProgresoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrdenEventListener {

    private final ProgresoService progresoService;
    private final OrdenRepository ordenRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrdenEstadoActualizado(OrdenEstadoActualizadoEvent event) {
        Long ordenId = event.getOrdenId();
        log.info("OrdenEventListener: Processing ordenId={}, estadoAnterior={}", ordenId, event.getEstadoAnterior());

        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + ordenId));

        log.info("OrdenEventListener: Orden {} fetched, estado={}, numDetalles={}",
                ordenId, orden.getEstado(), orden.getDetalles().size());

        Orden.Estado nuevoEstado = orden.getEstado();

        if (nuevoEstado == Orden.Estado.CONFIRMADA || nuevoEstado == Orden.Estado.ENTREGADA) {
            for (OrdenDetalle detalle : orden.getDetalles()) {
                log.info("OrdenEventListener: Procesando detalle tipo={}, itemId={}",
                        detalle.getTipoItem(), detalle.getItemId());
                if (detalle.getTipoItem() == OrdenDetalle.TipoItem.CURSO) {
                    try {
                        Long usuarioId = orden.getUsuario().getId();
                        Long cursoId = Long.parseLong(detalle.getItemId());
                        log.info("OrdenEventListener: Inscribiendo usuario {} en curso {}", usuarioId, cursoId);
                        progresoService.inscribirUsuario(usuarioId, cursoId);
                        log.info("Usuario {} auto-inscripto en curso {}", usuarioId, cursoId);
                    } catch (Exception e) {
                        log.error("Error auto-inscribiendo usuario en curso: {}", e.getMessage(), e);
                    }
                }
            }
        } else {
            log.info("OrdenEventListener: Estado {} no requiere inscripcion automatica", nuevoEstado);
        }
    }
}