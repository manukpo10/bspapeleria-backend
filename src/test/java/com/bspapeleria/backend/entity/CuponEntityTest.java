package com.bspapeleria.backend.entity;

import org.hibernate.annotations.DynamicUpdate;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class CuponEntityTest {

    @Test
    void updatesOnlyTheColumnsThatChanged_soAnAdminEditCannotWriteBackAStaleUsosCount() {
        // Orders bump usos_count with a bulk UPDATE. Without @DynamicUpdate, an admin edit that loaded the coupon
        // before that bump would rewrite every column, including the old usos_count.
        assertThat(Cupon.class.isAnnotationPresent(DynamicUpdate.class)).isTrue();
    }

    @Test
    void persistentFieldsAreExactlyTheOnesTheProductionTableHas() {
        // Guards the "no schema change" rule: ddl-auto is validate and there is no migration tool.
        assertThat(Arrays.stream(Cupon.class.getDeclaredFields()).map(f -> f.getName()))
                .containsExactlyInAnyOrder("id", "codigo", "tipoDescuento", "valorDescuento", "activo",
                        "fechaCreacion", "fechaVencimiento", "maxUsos", "usosCount", "descuentoMinimo",
                        "descuentoMaximo");
    }
}
