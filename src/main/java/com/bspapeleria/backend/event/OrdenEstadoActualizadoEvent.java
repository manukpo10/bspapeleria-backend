package com.bspapeleria.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrdenEstadoActualizadoEvent extends ApplicationEvent {

    private final Long ordenId;
    private final String estadoAnterior;

    public OrdenEstadoActualizadoEvent(Object source, Long ordenId, String estadoAnterior) {
        super(source);
        this.ordenId = ordenId;
        this.estadoAnterior = estadoAnterior;
    }
}