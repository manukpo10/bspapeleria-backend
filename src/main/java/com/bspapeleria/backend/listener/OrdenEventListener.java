package com.bspapeleria.backend.listener;

import com.bspapeleria.backend.entity.Orden;
import com.bspapeleria.backend.entity.OrdenDetalle;
import com.bspapeleria.backend.event.OrdenEstadoActualizadoEvent;
import com.bspapeleria.backend.service.ProgresoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrdenEventListener {

    private final ProgresoService progresoService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void handleOrdenEstadoActualizado(OrdenEstadoActualizadoEvent event) {
        Orden orden = event.getOrden();
        Orden.Estado nuevoEstado = orden.getEstado();

        if (nuevoEstado == Orden.Estado.CONFIRMADA || nuevoEstado == Orden.Estado.ENTREGADA) {
            for (OrdenDetalle detalle : orden.getDetalles()) {
                if (detalle.getTipoItem() == OrdenDetalle.TipoItem.CURSO) {
                    try {
                        Long usuarioId = orden.getUsuario().getId();
                        Long cursoId = Long.parseLong(detalle.getItemId());
                        progresoService.inscribirUsuario(usuarioId, cursoId);
                        log.info("Usuario {} auto-inscripto en curso {}", usuarioId, cursoId);
                    } catch (Exception e) {
                        log.error("Error auto-inscribiendo usuario en curso: {}", e.getMessage());
                    }
                }
            }
        }
    }
}