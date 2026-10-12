package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.CuponRequest;
import com.bspapeleria.backend.dto.CuponResponse;
import com.bspapeleria.backend.entity.Cupon;
import com.bspapeleria.backend.exception.BadRequestException;
import com.bspapeleria.backend.exception.ResourceNotFoundException;
import com.bspapeleria.backend.repository.CuponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuponServiceTest {

    /** The shop's "now" in every test unless a test builds its own service: 2026-06-15 12:00 Argentina time. */
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 6, 15, 12, 0);

    @Mock
    CuponRepository cuponRepository;

    CuponService cuponService;

    @BeforeEach
    void setUp() {
        cuponService = serviceAt(AHORA);
    }

    /** A service whose clock reads {@code ahoraEnLaTienda} (Argentina local time), whatever the JVM time zone is. */
    private CuponService serviceAt(LocalDateTime ahoraEnLaTienda) {
        Clock fijo = Clock.fixed(ahoraEnLaTienda.atZone(CuponService.ZONA_TIENDA).toInstant(), CuponService.ZONA_TIENDA);
        return new CuponService(cuponRepository, fijo);
    }

    // ---------------------------------------------------------------- helpers

    private static Cupon.CuponBuilder cuponBase() {
        return Cupon.builder()
                .id(7L)
                .codigo("VERANO10")
                .tipoDescuento(Cupon.TipoDescuento.PORCENTAJE)
                .valorDescuento(10.0)
                .activo(true)
                .fechaCreacion(LocalDateTime.of(2026, 1, 1, 0, 0))
                .maxUsos(5)
                .usosCount(0);
    }

    private static Cupon porcentaje(double valor) {
        return cuponBase().tipoDescuento(Cupon.TipoDescuento.PORCENTAJE).valorDescuento(valor).build();
    }

    private static Cupon montoFijo(double valor) {
        return cuponBase().tipoDescuento(Cupon.TipoDescuento.MONTO_FIJO).valorDescuento(valor).build();
    }

    /** A request with the required fields filled in; {@code maxUsos} is required, so it defaults to 5. */
    private static CuponRequest request(String codigo, String tipo, Double valor) {
        CuponRequest request = new CuponRequest();
        request.setCodigo(codigo);
        request.setTipoDescuento(tipo);
        request.setValorDescuento(valor);
        request.setMaxUsos(5);
        return request;
    }

    @Test
    void springHasExactlyOneConstructorToUse_theOneThatNeedsOnlyTheRepository() {
        // CuponService has a second, package-private constructor for tests. If Spring cannot tell which one to
        // use, the application fails to start, and that cannot be tried here (no Spring context in these tests).
        var autowired = Arrays.stream(CuponService.class.getDeclaredConstructors())
                .filter(c -> c.isAnnotationPresent(Autowired.class))
                .toList();

        assertThat(autowired).hasSize(1);
        assertThat(autowired.get(0).getParameterTypes()).containsExactly(CuponRepository.class);
        assertThat(Modifier.isPublic(autowired.get(0).getModifiers())).isTrue();
    }

    // ------------------------------------------------------------ R1: the rule

    @Nested
    class CalcularDescuento {

        @Test
        void percentage_isSubtotalTimesValueOverHundred() {
            assertThat(cuponService.calcularDescuento(porcentaje(10), 1000.0)).isEqualTo(100.0);
        }

        @Test
        void percentage_isCappedByDescuentoMaximoWhenItWouldExceedIt() {
            Cupon cupon = cuponBase().valorDescuento(50.0).descuentoMaximo(300.0).build();

            assertThat(cuponService.calcularDescuento(cupon, 1000.0)).isEqualTo(300.0);
        }

        @Test
        void percentage_isNotAlteredByDescuentoMaximoWhenBelowTheCap() {
            Cupon cupon = cuponBase().valorDescuento(10.0).descuentoMaximo(300.0).build();

            assertThat(cuponService.calcularDescuento(cupon, 1000.0)).isEqualTo(100.0);
        }

        @Test
        void fixed_isTheConfiguredAmount() {
            assertThat(cuponService.calcularDescuento(montoFijo(200), 1000.0)).isEqualTo(200.0);
        }

        @Test
        void fixed_largerThanSubtotal_equalsSubtotal() {
            assertThat(cuponService.calcularDescuento(montoFijo(5000), 1200.0)).isEqualTo(1200.0);
        }

        @Test
        void percentage_aboveHundredInLegacyData_neverExceedsSubtotal() {
            assertThat(cuponService.calcularDescuento(porcentaje(150), 800.0)).isEqualTo(800.0);
        }

        @Test
        void negativeValueInLegacyData_neverProducesNegativeDiscount() {
            assertThat(cuponService.calcularDescuento(montoFijo(-50), 800.0)).isZero();
            assertThat(cuponService.calcularDescuento(porcentaje(-10), 800.0)).isZero();
        }

        @Test
        void result_isRoundedToTwoDecimalsHalfUp() {
            // 15% of 33.33 = 4.9995 -> 5.00
            assertThat(cuponService.calcularDescuento(porcentaje(15), 33.33)).isEqualTo(5.0);
            // 10% of 0.05 = 0.005 -> 0.01 (tie goes up)
            assertThat(cuponService.calcularDescuento(porcentaje(10), 0.05)).isEqualTo(0.01);
            // 0.5% of 99.99 = 0.49995 -> 0.50
            assertThat(cuponService.calcularDescuento(porcentaje(0.5), 99.99)).isEqualTo(0.5);
        }

        @Test
        void zeroSubtotal_givesZeroDiscount() {
            assertThat(cuponService.calcularDescuento(porcentaje(10), 0.0)).isZero();
            assertThat(cuponService.calcularDescuento(montoFijo(500), 0.0)).isZero();
        }
    }

    // ------------------------------------------------------- R2: validation

    @Nested
    class ValidarCupon {

        @Test
        void unknownCode_throwsNotFound() {
            when(cuponRepository.findByCodigoIgnoreCase("NOEXISTE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cuponService.validarCupon("NOEXISTE", 1000.0))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("NOEXISTE");
        }

        @Test
        void inactive_throwsBadRequest() {
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10"))
                    .thenReturn(Optional.of(cuponBase().activo(false).build()));

            assertThatThrownBy(() -> cuponService.validarCupon("VERANO10", 1000.0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("inactivo");
        }

        @Test
        void expired_throwsBadRequest() {
            Cupon vencido = cuponBase().fechaVencimiento(AHORA.minusDays(1)).build();
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(vencido));

            assertThatThrownBy(() -> cuponService.validarCupon("VERANO10", 1000.0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("vencido");
        }

        // fechaVencimiento is a zone-less LocalDateTime that the admin enters in Argentina time (the last day at
        // 23:59:59), and the server container runs in UTC: expiry must be judged against the shop's clock.

        @Test
        void couponExpiringTodayAtEndOfDay_isStillValidAt2200ShopTime() {
            // 22:00 in Buenos Aires is 01:00 UTC of the next day: a UTC-based check would already call it expired.
            Cupon venceHoy = cuponBase().fechaVencimiento(LocalDateTime.of(2026, 6, 15, 23, 59, 59)).build();
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(venceHoy));

            CuponResponse response = serviceAt(LocalDateTime.of(2026, 6, 15, 22, 0)).validarCupon("VERANO10", 1000.0);

            assertThat(response.getDescuentoAplicable()).isEqualTo(100.0);
        }

        @Test
        void couponExpiringTodayAtEndOfDay_isExpiredAtOneSecondPastMidnightShopTime() {
            Cupon venceHoy = cuponBase().fechaVencimiento(LocalDateTime.of(2026, 6, 15, 23, 59, 59)).build();
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(venceHoy));

            CuponService alDiaSiguiente = serviceAt(LocalDateTime.of(2026, 6, 16, 0, 0, 1));

            assertThatThrownBy(() -> alDiaSiguiente.validarCupon("VERANO10", 1000.0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("vencido");
        }

        @Test
        void orderCreationJudgesExpiryByTheShopClockToo() {
            Cupon venceHoy = cuponBase().fechaVencimiento(LocalDateTime.of(2026, 6, 15, 23, 59, 59)).build();
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(venceHoy));
            when(cuponRepository.incrementarUsosSiDisponible(7L)).thenReturn(1);

            double descuento = serviceAt(LocalDateTime.of(2026, 6, 15, 22, 0)).aplicarCupon("VERANO10", 1000.0);

            assertThat(descuento).isEqualTo(100.0);
        }

        @Test
        void productionWiring_readsTheClockInArgentinaTime_notTheServerZone() {
            CuponService enProduccion = new CuponService(cuponRepository);

            Clock reloj = (Clock) ReflectionTestUtils.getField(enProduccion, "clock");

            assertThat(CuponService.ZONA_TIENDA.getId()).isEqualTo("America/Argentina/Buenos_Aires");
            assertThat(reloj.getZone()).isEqualTo(CuponService.ZONA_TIENDA);
        }

        @Test
        void exhausted_throwsBadRequest() {
            Cupon agotado = cuponBase().maxUsos(5).usosCount(5).build();
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(agotado));

            assertThatThrownBy(() -> cuponService.validarCupon("VERANO10", 1000.0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("límite de usos");
        }

        @Test
        void storedRowWithNullMaxUsos_isToleratedAsUnlimited() {
            // The API never stores a null maxUsos (it is required), but validation must not blow up on one.
            Cupon ilimitado = cuponBase().maxUsos(null).usosCount(10_000).build();
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(ilimitado));

            CuponResponse response = cuponService.validarCupon("VERANO10", 1000.0);

            assertThat(response.getDescuentoAplicable()).isEqualTo(100.0);
        }

        @Test
        void belowMinimum_throwsBadRequestMentioningTheMinimum() {
            Cupon conMinimo = cuponBase().descuentoMinimo(1000.0).build();
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(conMinimo));

            assertThatThrownBy(() -> cuponService.validarCupon("VERANO10", 999.99))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("1000.00");
        }

        @Test
        void exactlyTheMinimum_isValid() {
            Cupon conMinimo = cuponBase().descuentoMinimo(1000.0).build();
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(conMinimo));

            CuponResponse response = cuponService.validarCupon("VERANO10", 1000.0);

            assertThat(response.getDescuentoAplicable()).isEqualTo(100.0);
        }

        @Test
        void valid_returnsCouponDataAndTheDiscountForThatAmount() {
            Cupon cupon = cuponBase().valorDescuento(20.0).descuentoMaximo(150.0)
                    .descuentoMinimo(500.0).usosCount(2).build();
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(cupon));

            CuponResponse response = cuponService.validarCupon("VERANO10", 1000.0);

            assertThat(response.getId()).isEqualTo(7L);
            assertThat(response.getCodigo()).isEqualTo("VERANO10");
            assertThat(response.getTipoDescuento()).isEqualTo("PORCENTAJE");
            assertThat(response.getValorDescuento()).isEqualTo(20.0);
            assertThat(response.getUsosCount()).isEqualTo(2);
            assertThat(response.getDescuentoMinimo()).isEqualTo(500.0);
            assertThat(response.getDescuentoMaximo()).isEqualTo(150.0);
            // 20% of 1000 = 200, capped at 150
            assertThat(response.getDescuentoAplicable()).isEqualTo(150.0);
        }

        @Test
        void codeLookup_ignoresSurroundingSpaces() {
            when(cuponRepository.findByCodigoIgnoreCase("verano10"))
                    .thenReturn(Optional.of(cuponBase().build()));

            CuponResponse response = cuponService.validarCupon("  verano10  ", 1000.0);

            assertThat(response.getCodigo()).isEqualTo("VERANO10");
            verify(cuponRepository).findByCodigoIgnoreCase("verano10");
        }

        @Test
        void checksRunInOrder_inactiveBeforeExpiredBeforeExhausted() {
            Cupon todoMal = cuponBase().activo(false)
                    .fechaVencimiento(AHORA.minusDays(1))
                    .maxUsos(1).usosCount(1).build();
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(todoMal));

            assertThatThrownBy(() -> cuponService.validarCupon("VERANO10", 1000.0))
                    .hasMessageContaining("inactivo");

            todoMal.setActivo(true);
            assertThatThrownBy(() -> cuponService.validarCupon("VERANO10", 1000.0))
                    .hasMessageContaining("vencido");

            todoMal.setFechaVencimiento(null);
            assertThatThrownBy(() -> cuponService.validarCupon("VERANO10", 1000.0))
                    .hasMessageContaining("límite de usos");
        }

        @Test
        void blankCode_throwsBadRequestWithoutHittingTheRepository() {
            assertThatThrownBy(() -> cuponService.validarCupon("   ", 1000.0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("código");

            verify(cuponRepository, never()).findByCodigoIgnoreCase(any());
        }

        @Test
        void invalidAmount_throwsBadRequest() {
            assertThatThrownBy(() -> cuponService.validarCupon("VERANO10", -5.0))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> cuponService.validarCupon("VERANO10", Double.NaN))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    // ------------------------------------------- R3 (service half): use it once

    @Nested
    class AplicarCupon {

        @Test
        void valid_returnsDiscountAndCountsTheUseAtomically() {
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10"))
                    .thenReturn(Optional.of(cuponBase().build()));
            when(cuponRepository.incrementarUsosSiDisponible(7L)).thenReturn(1);

            double descuento = cuponService.aplicarCupon("VERANO10", 2000.0);

            assertThat(descuento).isEqualTo(200.0);
            verify(cuponRepository).incrementarUsosSiDisponible(7L);
        }

        @Test
        void whenTheAtomicIncrementUpdatesNoRows_throwsAndTheUseIsNotGranted() {
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10"))
                    .thenReturn(Optional.of(cuponBase().build()));
            when(cuponRepository.incrementarUsosSiDisponible(7L)).thenReturn(0);

            assertThatThrownBy(() -> cuponService.aplicarCupon("VERANO10", 2000.0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El cupón ya alcanzó su límite de usos");
        }

        @Test
        void invalidCoupon_throwsAndNeverTouchesTheCounter() {
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10"))
                    .thenReturn(Optional.of(cuponBase().activo(false).build()));

            assertThatThrownBy(() -> cuponService.aplicarCupon("VERANO10", 2000.0))
                    .isInstanceOf(BadRequestException.class);

            verify(cuponRepository, never()).incrementarUsosSiDisponible(any());
        }

        @Test
        void zeroSubtotal_givesZeroDiscount_andDoesNotConsumeAUse() {
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10"))
                    .thenReturn(Optional.of(cuponBase().build()));

            double descuento = cuponService.aplicarCupon("VERANO10", 0.0);

            assertThat(descuento).isZero();
            verify(cuponRepository, never()).incrementarUsosSiDisponible(any());
        }

        @Test
        void anyOtherZeroDiscount_doesNotConsumeAUseEither() {
            // A cap of 0 can no longer be created through the API, but a legacy row may still have one.
            Cupon topeCero = cuponBase().valorDescuento(50.0).descuentoMaximo(0.0).build();
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(topeCero));

            double descuento = cuponService.aplicarCupon("VERANO10", 2000.0);

            assertThat(descuento).isZero();
            verify(cuponRepository, never()).incrementarUsosSiDisponible(any());
        }

        @Test
        void zeroSubtotal_stillFailsValidation_whenTheCouponIsNotUsable() {
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10"))
                    .thenReturn(Optional.of(cuponBase().activo(false).build()));

            assertThatThrownBy(() -> cuponService.aplicarCupon("VERANO10", 0.0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("inactivo");
        }

        @Test
        void subtotalBelowMinimum_throwsAndNeverTouchesTheCounter() {
            when(cuponRepository.findByCodigoIgnoreCase("VERANO10"))
                    .thenReturn(Optional.of(cuponBase().descuentoMinimo(2500.0).build()));

            assertThatThrownBy(() -> cuponService.aplicarCupon("VERANO10", 2000.0))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("2500.00");

            verify(cuponRepository, never()).incrementarUsosSiDisponible(any());
        }
    }

    // ------------------------------------------------- R4: create validation

    @Nested
    class CreateCupon {

        @Test
        void storesTheCodeTrimmedAndUpperCase() {
            when(cuponRepository.existsByCodigoIgnoreCase("VERANO10")).thenReturn(false);
            CuponRequest request = request("  verano10 ", "PORCENTAJE", 10.0);

            CuponResponse response = cuponService.createCupon(request);

            ArgumentCaptor<Cupon> captor = ArgumentCaptor.forClass(Cupon.class);
            verify(cuponRepository).save(captor.capture());
            assertThat(captor.getValue().getCodigo()).isEqualTo("VERANO10");
            assertThat(response.getCodigo()).isEqualTo("VERANO10");
            assertThat(response.getDescuentoAplicable()).isNull();
        }

        @Test
        void mapsAllFields_andDefaultsActivoToTrue() {
            when(cuponRepository.existsByCodigoIgnoreCase("PROMO")).thenReturn(false);
            CuponRequest request = request("promo", "monto-fijo", 500.0);
            request.setMaxUsos(3);
            request.setFechaVencimiento(LocalDateTime.of(2026, 12, 31, 23, 59, 59));
            request.setDescuentoMinimo(2000.0);
            request.setDescuentoMaximo(500.0);

            cuponService.createCupon(request);

            ArgumentCaptor<Cupon> captor = ArgumentCaptor.forClass(Cupon.class);
            verify(cuponRepository).save(captor.capture());
            Cupon saved = captor.getValue();
            assertThat(saved.getTipoDescuento()).isEqualTo(Cupon.TipoDescuento.MONTO_FIJO);
            assertThat(saved.getValorDescuento()).isEqualTo(500.0);
            assertThat(saved.getMaxUsos()).isEqualTo(3);
            assertThat(saved.getActivo()).isTrue();
            assertThat(saved.getFechaVencimiento()).isEqualTo(LocalDateTime.of(2026, 12, 31, 23, 59, 59));
            assertThat(saved.getDescuentoMinimo()).isEqualTo(2000.0);
            assertThat(saved.getDescuentoMaximo()).isEqualTo(500.0);
        }

        @Test
        void percentageAboveHundred_isRejected() {
            assertThatThrownBy(() -> cuponService.createCupon(request("PROMO", "PORCENTAJE", 100.01)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("100");

            verify(cuponRepository, never()).save(any());
        }

        @Test
        void percentageOfExactlyHundred_isAccepted() {
            when(cuponRepository.existsByCodigoIgnoreCase("PROMO")).thenReturn(false);

            cuponService.createCupon(request("PROMO", "PORCENTAJE", 100.0));

            verify(cuponRepository).save(any(Cupon.class));
        }

        @Test
        void nonPositiveValue_isRejectedForBothTypes() {
            assertThatThrownBy(() -> cuponService.createCupon(request("PROMO", "PORCENTAJE", 0.0)))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> cuponService.createCupon(request("PROMO", "MONTO_FIJO", 0.0)))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> cuponService.createCupon(request("PROMO", "MONTO_FIJO", -10.0)))
                    .isInstanceOf(BadRequestException.class);

            verify(cuponRepository, never()).save(any());
        }

        @Test
        void missingValue_isRejected() {
            assertThatThrownBy(() -> cuponService.createCupon(request("PROMO", "PORCENTAJE", null)))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        void fixedAmountAboveHundred_isAccepted() {
            when(cuponRepository.existsByCodigoIgnoreCase("PROMO")).thenReturn(false);

            cuponService.createCupon(request("PROMO", "MONTO_FIJO", 5000.0));

            verify(cuponRepository).save(any(Cupon.class));
        }

        @Test
        void unknownType_isRejectedInsteadOfBecomingAPercentage() {
            assertThatThrownBy(() -> cuponService.createCupon(request("PROMO", "REGALO", 10.0)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("REGALO");

            verify(cuponRepository, never()).save(any());
        }

        @Test
        void blankTypeOrCode_isRejected() {
            assertThatThrownBy(() -> cuponService.createCupon(request("PROMO", "  ", 10.0)))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> cuponService.createCupon(request("   ", "PORCENTAJE", 10.0)))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        void duplicateCode_isRejected() {
            when(cuponRepository.existsByCodigoIgnoreCase("VERANO10")).thenReturn(true);

            assertThatThrownBy(() -> cuponService.createCupon(request("verano10", "PORCENTAJE", 10.0)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Ya existe un cupón con ese código");

            verify(cuponRepository, never()).save(any());
        }

        @Test
        void negativeMinimum_isRejected_butZeroMinimumIsAllowed() {
            CuponRequest minimoNegativo = request("PROMO", "PORCENTAJE", 10.0);
            minimoNegativo.setDescuentoMinimo(-1.0);

            assertThatThrownBy(() -> cuponService.createCupon(minimoNegativo))
                    .isInstanceOf(BadRequestException.class).hasMessageContaining("mínimo");
            verify(cuponRepository, never()).save(any());

            when(cuponRepository.existsByCodigoIgnoreCase("PROMO")).thenReturn(false);
            CuponRequest minimoCero = request("PROMO", "PORCENTAJE", 10.0);
            minimoCero.setDescuentoMinimo(0.0);
            cuponService.createCupon(minimoCero);
            verify(cuponRepository).save(any(Cupon.class));
        }

        @Test
        void maximumDiscount_mustBeGreaterThanZeroWhenPresent() {
            // A cap of 0 would make a coupon that always discounts nothing.
            CuponRequest maximoNegativo = request("PROMO", "PORCENTAJE", 10.0);
            maximoNegativo.setDescuentoMaximo(-1.0);
            CuponRequest maximoCero = request("PROMO", "PORCENTAJE", 10.0);
            maximoCero.setDescuentoMaximo(0.0);

            assertThatThrownBy(() -> cuponService.createCupon(maximoNegativo))
                    .isInstanceOf(BadRequestException.class).hasMessage("El descuento máximo debe ser mayor a 0");
            assertThatThrownBy(() -> cuponService.createCupon(maximoCero))
                    .isInstanceOf(BadRequestException.class).hasMessage("El descuento máximo debe ser mayor a 0");
            verify(cuponRepository, never()).save(any());

            when(cuponRepository.existsByCodigoIgnoreCase("PROMO")).thenReturn(false);
            CuponRequest maximoPositivo = request("PROMO", "PORCENTAJE", 10.0);
            maximoPositivo.setDescuentoMaximo(0.01);
            cuponService.createCupon(maximoPositivo);
            verify(cuponRepository).save(any(Cupon.class));
        }

        @Test
        void codeLongerThanFiftyCharacters_isRejected_butFiftyIsAccepted() {
            assertThatThrownBy(() -> cuponService.createCupon(request("A".repeat(51), "PORCENTAJE", 10.0)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El código no puede tener más de 50 caracteres");
            verify(cuponRepository, never()).save(any());

            String cincuenta = "B".repeat(50);
            when(cuponRepository.existsByCodigoIgnoreCase(cincuenta)).thenReturn(false);
            cuponService.createCupon(request("  " + cincuenta + "  ", "PORCENTAJE", 10.0)); // length counts after trim
            verify(cuponRepository).save(any(Cupon.class));
        }

        @Test
        void maxUsosBelowOne_isRejected() {
            CuponRequest cero = request("PROMO", "PORCENTAJE", 10.0);
            cero.setMaxUsos(0);
            CuponRequest negativo = request("PROMO", "PORCENTAJE", 10.0);
            negativo.setMaxUsos(-3);

            assertThatThrownBy(() -> cuponService.createCupon(cero))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("al menos 1");
            assertThatThrownBy(() -> cuponService.createCupon(negativo))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("al menos 1");

            verify(cuponRepository, never()).save(any());
        }

        @Test
        void maxUsosOfExactlyOne_isAccepted() {
            when(cuponRepository.existsByCodigoIgnoreCase("PROMO")).thenReturn(false);
            CuponRequest uno = request("PROMO", "PORCENTAJE", 10.0);
            uno.setMaxUsos(1);

            cuponService.createCupon(uno);

            verify(cuponRepository).save(any(Cupon.class));
        }

        @Test
        void missingMaxUsos_isRejected_andNothingReachesTheDatabase() {
            // max_usos is NOT NULL in production and there is no "unlimited": it must be given explicitly.
            CuponRequest sinMaxUsos = request("PROMO", "PORCENTAJE", 10.0);
            sinMaxUsos.setMaxUsos(null);

            assertThatThrownBy(() -> cuponService.createCupon(sinMaxUsos))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Indicá la cantidad máxima de usos");

            verify(cuponRepository, never()).save(any());
        }
    }

    // ------------------------------------------- R4 + R5: update (replacement)

    @Nested
    class UpdateCupon {

        private Cupon existente() {
            return cuponBase()
                    .id(7L)
                    .codigo("VIEJO")
                    .tipoDescuento(Cupon.TipoDescuento.MONTO_FIJO)
                    .valorDescuento(300.0)
                    .activo(false)
                    .fechaVencimiento(LocalDateTime.of(2027, 1, 1, 0, 0))
                    .maxUsos(10)
                    .usosCount(4)
                    .descuentoMinimo(1000.0)
                    .descuentoMaximo(250.0)
                    .build();
        }

        @Test
        void unknownId_throwsNotFound() {
            when(cuponRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cuponService.updateCupon(99L, request("X", "PORCENTAJE", 10.0)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void isAFullReplacement_nullOptionalsAreCleared_andCountersUntouched() {
            Cupon cupon = existente();
            when(cuponRepository.findById(7L)).thenReturn(Optional.of(cupon));
            when(cuponRepository.existsByCodigoIgnoreCaseAndIdNot("NUEVO", 7L)).thenReturn(false);
            CuponRequest request = request(" nuevo ", "PORCENTAJE", 15.0);
            request.setMaxUsos(8);
            // fechaVencimiento, descuentoMinimo, descuentoMaximo are cleared; activo is omitted on purpose

            CuponResponse response = cuponService.updateCupon(7L, request);

            assertThat(cupon.getCodigo()).isEqualTo("NUEVO");
            assertThat(cupon.getTipoDescuento()).isEqualTo(Cupon.TipoDescuento.PORCENTAJE);
            assertThat(cupon.getValorDescuento()).isEqualTo(15.0);
            assertThat(cupon.getActivo()).isFalse(); // omitted: the stored value (false) is kept
            assertThat(cupon.getFechaVencimiento()).isNull();
            assertThat(cupon.getMaxUsos()).isEqualTo(8);
            assertThat(cupon.getDescuentoMinimo()).isNull();
            assertThat(cupon.getDescuentoMaximo()).isNull();
            assertThat(cupon.getUsosCount()).isEqualTo(4);
            assertThat(cupon.getFechaCreacion()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
            assertThat(response.getCodigo()).isEqualTo("NUEVO");
            assertThat(response.getDescuentoAplicable()).isNull();
        }

        @Test
        void omittedActivo_keepsTheStoredValue_soAClientThatOmitsItCannotReactivateACoupon() {
            Cupon inactivo = existente(); // stored activo = false
            Cupon activo = existente();
            activo.setActivo(true);
            when(cuponRepository.findById(7L)).thenReturn(Optional.of(inactivo), Optional.of(activo));
            when(cuponRepository.existsByCodigoIgnoreCaseAndIdNot("VIEJO", 7L)).thenReturn(false);

            cuponService.updateCupon(7L, request("VIEJO", "MONTO_FIJO", 300.0));
            cuponService.updateCupon(7L, request("VIEJO", "MONTO_FIJO", 300.0));

            assertThat(inactivo.getActivo()).isFalse();
            assertThat(activo.getActivo()).isTrue();
        }

        @Test
        void explicitActivo_replacesTheStoredValue() {
            Cupon cupon = existente(); // stored activo = false
            when(cuponRepository.findById(7L)).thenReturn(Optional.of(cupon));
            when(cuponRepository.existsByCodigoIgnoreCaseAndIdNot("VIEJO", 7L)).thenReturn(false);
            CuponRequest request = request("VIEJO", "MONTO_FIJO", 300.0);
            request.setActivo(true);

            cuponService.updateCupon(7L, request);
            assertThat(cupon.getActivo()).isTrue();

            request.setActivo(false);
            cuponService.updateCupon(7L, request);
            assertThat(cupon.getActivo()).isFalse();
        }

        @Test
        void codeLongerThanFiftyCharacters_isRejected_andTheStoredCouponIsLeftAlone() {
            Cupon cupon = existente();
            when(cuponRepository.findById(7L)).thenReturn(Optional.of(cupon));

            assertThatThrownBy(() -> cuponService.updateCupon(7L, request("A".repeat(51), "MONTO_FIJO", 300.0)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El código no puede tener más de 50 caracteres");

            assertThat(cupon.getCodigo()).isEqualTo("VIEJO");
            verify(cuponRepository, never()).save(any());
        }

        @Test
        void providedValues_replaceTheStoredOnes() {
            Cupon cupon = existente();
            when(cuponRepository.findById(7L)).thenReturn(Optional.of(cupon));
            when(cuponRepository.existsByCodigoIgnoreCaseAndIdNot("VIEJO", 7L)).thenReturn(false);
            CuponRequest request = request("viejo", "MONTO_FIJO", 400.0);
            request.setActivo(true);
            request.setMaxUsos(20);
            request.setFechaVencimiento(LocalDateTime.of(2028, 6, 30, 12, 0));
            request.setDescuentoMinimo(2000.0);
            request.setDescuentoMaximo(400.0);

            cuponService.updateCupon(7L, request);

            assertThat(cupon.getActivo()).isTrue();
            assertThat(cupon.getMaxUsos()).isEqualTo(20);
            assertThat(cupon.getFechaVencimiento()).isEqualTo(LocalDateTime.of(2028, 6, 30, 12, 0));
            assertThat(cupon.getDescuentoMinimo()).isEqualTo(2000.0);
            assertThat(cupon.getDescuentoMaximo()).isEqualTo(400.0);
        }

        @Test
        void missingMaxUsos_isRejected_andNothingIsWritten() {
            // A null here used to be flushed as NULL into a NOT NULL column (500). Now it is a clean 400.
            Cupon cupon = existente();
            when(cuponRepository.findById(7L)).thenReturn(Optional.of(cupon));
            CuponRequest sinMaxUsos = request("VIEJO", "MONTO_FIJO", 300.0);
            sinMaxUsos.setMaxUsos(null);

            assertThatThrownBy(() -> cuponService.updateCupon(7L, sinMaxUsos))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Indicá la cantidad máxima de usos");

            assertThat(cupon.getMaxUsos()).isEqualTo(10);
            verify(cuponRepository, never()).save(any());
        }

        @Test
        void maxUsosBelowOne_isRejected_andTheStoredValueIsKept() {
            Cupon cupon = existente();
            when(cuponRepository.findById(7L)).thenReturn(Optional.of(cupon));
            CuponRequest cero = request("VIEJO", "MONTO_FIJO", 300.0);
            cero.setMaxUsos(0);

            assertThatThrownBy(() -> cuponService.updateCupon(7L, cero))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("al menos 1");

            assertThat(cupon.getMaxUsos()).isEqualTo(10);
            verify(cuponRepository, never()).save(any());
        }

        @Test
        void codeUsedByAnotherCoupon_isRejected() {
            Cupon cupon = existente();
            when(cuponRepository.findById(7L)).thenReturn(Optional.of(cupon));
            when(cuponRepository.existsByCodigoIgnoreCaseAndIdNot("OTRO", 7L)).thenReturn(true);

            assertThatThrownBy(() -> cuponService.updateCupon(7L, request("otro", "PORCENTAJE", 10.0)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Ya existe un cupón con ese código");

            assertThat(cupon.getCodigo()).isEqualTo("VIEJO");
        }

        @Test
        void invalidRules_areRejectedAndTheStoredCouponIsLeftAlone() {
            Cupon cupon = existente();
            when(cuponRepository.findById(7L)).thenReturn(Optional.of(cupon));

            assertThatThrownBy(() -> cuponService.updateCupon(7L, request("VIEJO", "PORCENTAJE", 120.0)))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> cuponService.updateCupon(7L, request("VIEJO", "REGALO", 10.0)))
                    .isInstanceOf(BadRequestException.class);

            assertThat(cupon.getTipoDescuento()).isEqualTo(Cupon.TipoDescuento.MONTO_FIJO);
            assertThat(cupon.getValorDescuento()).isEqualTo(300.0);
            verify(cuponRepository, never()).save(any());
        }
    }
}
