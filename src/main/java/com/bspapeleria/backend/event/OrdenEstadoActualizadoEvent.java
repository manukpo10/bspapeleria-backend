package com.bspapeleria.backend.event;

import com.bspapeleria.backend.entity.Orden;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrdenEstadoActualizadoEvent extends ApplicationEvent {

    private final Orden orden;
    private final String estadoAnterior;

    public OrdenEstadoActualizadoEvent(Object source, Orden orden, String estadoAnterior) {
        super(source);
        this.orden = orden;
        this.estadoAnterior = estadoAnterior;
    }
}