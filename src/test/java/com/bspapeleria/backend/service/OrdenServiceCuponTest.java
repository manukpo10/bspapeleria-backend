package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.OrdenItemRequest;
import com.bspapeleria.backend.dto.OrdenRequest;
import com.bspapeleria.backend.dto.OrdenResponse;
import com.bspapeleria.backend.entity.Cupon;
import com.bspapeleria.backend.entity.Curso;
import com.bspapeleria.backend.entity.Orden;
import com.bspapeleria.backend.entity.Producto;
import com.bspapeleria.backend.entity.Usuario;
import com.bspapeleria.backend.exception.BadRequestException;
import com.bspapeleria.backend.exception.ResourceNotFoundException;
import com.bspapeleria.backend.repository.CuponRepository;
import com.bspapeleria.backend.repository.CursoRepository;
import com.bspapeleria.backend.repository.OrdenRepository;
import com.bspapeleria.backend.repository.ProductoRepository;
import com.bspapeleria.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the coupon step of {@link OrdenService#createOrden}. No Spring context and no database:
 * the repositories are mocks and the real {@link CuponService} runs on top of a mocked {@link CuponRepository},
 * so these tests also prove that the order flow reaches the atomic usage counter.
 */
@ExtendWith(MockitoExtension.class)
class OrdenServiceCuponTest {

    private static final long USUARIO_ID = 3L;
    private static final long PRODUCTO_ID = 11L;
    private static final long CURSO_ID = 21L;
    private static final double ENVIO_PRODUCTOS_FISICOS = 1500.0;

    @Mock OrdenRepository ordenRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ProductoRepository productoRepository;
    @Mock CursoRepository cursoRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock ProgresoService progresoService;
    @Mock CuponRepository cuponRepository;

    OrdenService ordenService;

    @BeforeEach
    void setUp() {
        ordenService = new OrdenService(
                ordenRepository,
                usuarioRepository,
                productoRepository,
                cursoRepository,
                eventPublisher,
                progresoService,
                new CuponService(cuponRepository));
    }

    // ---------------------------------------------------------------- helpers

    /** A product whose {@code esDigital} is not set (null), which the shipping rule treats as physical. */
    private void givenUserAndProductPricedAt(double precio) {
        givenUser();
        givenProduct(PRODUCTO_ID, precio, null);
    }

    private void givenUser() {
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(
                Usuario.builder().id(USUARIO_ID).email("cliente@example.com").nombre("Cliente").build()));
    }

    private void givenProduct(long id, double precio, Boolean esDigital) {
        when(productoRepository.findById(id)).thenReturn(Optional.of(
                Producto.builder().id(id).nombre("Producto " + id).precio(precio).esDigital(esDigital).build()));
    }

    private static OrdenItemRequest itemOf(String tipo, long itemId, int cantidad) {
        OrdenItemRequest item = new OrdenItemRequest();
        item.setTipo(tipo);
        item.setItemId(String.valueOf(itemId));
        item.setCantidad(cantidad);
        // A tampered client price must be ignored: the database price is the only one that counts.
        item.setPrecioUnitario(1.0);
        return item;
    }

    private static OrdenRequest orderWith(List<OrdenItemRequest> items) {
        OrdenRequest request = new OrdenRequest();
        request.setItems(items);
        request.setMetodoPago("MERCADO_PAGO");
        return request;
    }

    private OrdenRequest orderOfTwoProducts(String codigoCupon) {
        return orderOf("PRODUCTO", PRODUCTO_ID, 2, codigoCupon);
    }

    private OrdenRequest orderOf(String tipo, long itemId, int cantidad, String codigoCupon) {
        OrdenRequest request = orderWith(List.of(itemOf(tipo, itemId, cantidad)));
        request.setCodigoCupon(codigoCupon);
        return request;
    }

    private void givenUserAndCoursePricedAt(double precio) {
        givenUser();
        when(cursoRepository.findById(CURSO_ID)).thenReturn(Optional.of(
                Curso.builder().id(CURSO_ID).titulo("Curso de lettering").precio(precio).build()));
    }

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

    private Orden savedOrder() {
        ArgumentCaptor<Orden> captor = ArgumentCaptor.forClass(Orden.class);
        verify(ordenRepository).save(captor.capture());
        return captor.getValue();
    }

    // ------------------------------------------------------------------ tests

    @Test
    void withoutCouponCode_keepsTodaysBehaviour_noDiscountAndTheCounterIsUntouched() {
        givenUserAndProductPricedAt(1000.0);

        OrdenResponse response = ordenService.createOrden(USUARIO_ID, orderOfTwoProducts(null));

        Orden orden = savedOrder();
        assertThat(orden.getSubtotal()).isEqualTo(2000.0);
        assertThat(orden.getDescuento()).isZero();
        assertThat(orden.getCostoEnvio()).isEqualTo(ENVIO_PRODUCTOS_FISICOS);
        assertThat(orden.getTotal()).isEqualTo(3500.0);
        assertThat(response.getDescuento()).isZero();
        verifyNoInteractions(cuponRepository);
    }

    @Test
    void withBlankCouponCode_isTreatedAsNoCoupon() {
        givenUserAndProductPricedAt(1000.0);

        ordenService.createOrden(USUARIO_ID, orderOfTwoProducts("   "));

        assertThat(savedOrder().getDescuento()).isZero();
        verifyNoInteractions(cuponRepository);
    }

    @Test
    void unknownCoupon_throwsNotFound_andNoOrderIsSaved() {
        givenUserAndProductPricedAt(1000.0);
        when(cuponRepository.findByCodigoIgnoreCase("NOEXISTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.createOrden(USUARIO_ID, orderOfTwoProducts("NOEXISTE")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(ordenRepository, never()).save(any());
    }

    @Test
    void invalidCoupon_throwsBadRequest_andNoOrderIsSaved() {
        givenUserAndProductPricedAt(1000.0);
        when(cuponRepository.findByCodigoIgnoreCase("VERANO10"))
                .thenReturn(Optional.of(cuponBase().activo(false).build()));

        assertThatThrownBy(() -> ordenService.createOrden(USUARIO_ID, orderOfTwoProducts("VERANO10")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inactivo");

        verify(ordenRepository, never()).save(any());
        verify(cuponRepository, never()).incrementarUsosSiDisponible(any());
    }

    @Test
    void minimumPurchaseIsCheckedAgainstTheItemsSubtotal_notTheShippingInclusiveTotal() {
        // Items: 2 x 1000 = 2000. With shipping the order would be 3500, which would wrongly pass a 3000 minimum.
        givenUserAndProductPricedAt(1000.0);
        when(cuponRepository.findByCodigoIgnoreCase("VERANO10"))
                .thenReturn(Optional.of(cuponBase().descuentoMinimo(3000.0).build()));

        assertThatThrownBy(() -> ordenService.createOrden(USUARIO_ID, orderOfTwoProducts("VERANO10")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("3000.00");

        verify(ordenRepository, never()).save(any());
    }

    @Test
    void validCoupon_storesTheDiscount_computesTheTotal_andCountsTheUse() {
        givenUserAndProductPricedAt(1000.0);
        when(cuponRepository.findByCodigoIgnoreCase("VERANO10"))
                .thenReturn(Optional.of(cuponBase().build()));
        when(cuponRepository.incrementarUsosSiDisponible(7L)).thenReturn(1);

        OrdenResponse response = ordenService.createOrden(USUARIO_ID, orderOfTwoProducts("VERANO10"));

        Orden orden = savedOrder();
        assertThat(orden.getSubtotal()).isEqualTo(2000.0);
        assertThat(orden.getDescuento()).isEqualTo(200.0);
        assertThat(orden.getCostoEnvio()).isEqualTo(ENVIO_PRODUCTOS_FISICOS);
        assertThat(orden.getTotal()).isEqualTo(3300.0); // 2000 + 1500 - 200
        assertThat(response.getDescuento()).isEqualTo(200.0);
        assertThat(response.getTotal()).isEqualTo(3300.0);
        verify(cuponRepository).incrementarUsosSiDisponible(7L);
    }

    @Test
    void validCoupon_respectsTheCapOnPercentageDiscounts() {
        givenUserAndProductPricedAt(1000.0);
        Cupon conTope = cuponBase().valorDescuento(50.0).descuentoMaximo(300.0).build();
        when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(conTope));
        when(cuponRepository.incrementarUsosSiDisponible(7L)).thenReturn(1);

        ordenService.createOrden(USUARIO_ID, orderOfTwoProducts("VERANO10"));

        Orden orden = savedOrder();
        assertThat(orden.getDescuento()).isEqualTo(300.0);
        assertThat(orden.getTotal()).isEqualTo(3200.0); // 2000 + 1500 - 300
    }

    @Test
    void fixedCouponLargerThanTheSubtotal_neverMakesTheTotalNegative() {
        givenUserAndProductPricedAt(1000.0);
        Cupon enorme = cuponBase().tipoDescuento(Cupon.TipoDescuento.MONTO_FIJO).valorDescuento(999_999.0).build();
        when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(enorme));
        when(cuponRepository.incrementarUsosSiDisponible(7L)).thenReturn(1);

        ordenService.createOrden(USUARIO_ID, orderOfTwoProducts("VERANO10"));

        Orden orden = savedOrder();
        assertThat(orden.getDescuento()).isEqualTo(2000.0);
        assertThat(orden.getTotal()).isEqualTo(ENVIO_PRODUCTOS_FISICOS);
    }

    @Test
    void couponThatYieldsZeroBecauseTheItemsSubtotalIsZero_doesNotConsumeAUse() {
        givenUserAndProductPricedAt(0.0); // a free item: the only way left to reach a 0 subtotal through the API
        when(cuponRepository.findByCodigoIgnoreCase("VERANO10"))
                .thenReturn(Optional.of(cuponBase().build()));

        ordenService.createOrden(USUARIO_ID, orderOfTwoProducts("VERANO10"));

        Orden orden = savedOrder();
        assertThat(orden.getSubtotal()).isZero();
        assertThat(orden.getDescuento()).isZero();
        assertThat(orden.getTotal()).isEqualTo(ENVIO_PRODUCTOS_FISICOS);
        verify(cuponRepository, never()).incrementarUsosSiDisponible(any());
    }

    // ------------------------------------------------------ money arithmetic

    @Test
    void fullDiscountOnADigitalOrder_storesATotalOfExactlyZero_neverATinyNegative() {
        // 3 x 0.15 is 0.44999999999999996 in a double; minus a 0.45 discount used to store -5.55e-17.
        givenUserAndCoursePricedAt(0.15);
        Cupon completo = cuponBase().valorDescuento(100.0).build();
        when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(completo));
        when(cuponRepository.incrementarUsosSiDisponible(7L)).thenReturn(1);

        ordenService.createOrden(USUARIO_ID, orderOf("CURSO", CURSO_ID, 3, "VERANO10"));

        Orden orden = savedOrder();
        assertThat(orden.getSubtotal()).isEqualTo(0.45);
        assertThat(orden.getDescuento()).isEqualTo(0.45);
        assertThat(orden.getCostoEnvio()).isZero();
        assertThat(orden.getTotal()).isEqualTo(0.0); // equals() on Double also rules out -0.0
    }

    @Test
    void orderWithoutCoupon_keepsItsAmounts_butLosesTheFloatNoise() {
        // 3 x 1299.99 is 3899.9700000000003 in a double.
        givenUserAndProductPricedAt(1299.99);

        ordenService.createOrden(USUARIO_ID, orderOf("PRODUCTO", PRODUCTO_ID, 3, null));

        Orden orden = savedOrder();
        assertThat(orden.getSubtotal()).isEqualTo(3899.97);
        assertThat(orden.getDescuento()).isZero();
        assertThat(orden.getCostoEnvio()).isEqualTo(ENVIO_PRODUCTOS_FISICOS);
        assertThat(orden.getTotal()).isEqualTo(5399.97);
        assertThat(orden.getDetalles()).singleElement().satisfies(detalle -> {
            assertThat(detalle.getPrecioUnitario()).isEqualTo(1299.99);
            assertThat(detalle.getSubtotal()).isEqualTo(3899.97);
        });
        verifyNoInteractions(cuponRepository);
    }

    @Test
    void digitalOrderWithoutCoupon_hasNoShipping_andTheTotalIsTheCleanSubtotal() {
        // 3 x 0.1 is 0.30000000000000004 in a double.
        givenUserAndCoursePricedAt(0.1);

        ordenService.createOrden(USUARIO_ID, orderOf("CURSO", CURSO_ID, 3, null));

        Orden orden = savedOrder();
        assertThat(orden.getSubtotal()).isEqualTo(0.3);
        assertThat(orden.getCostoEnvio()).isZero();
        assertThat(orden.getTotal()).isEqualTo(0.3);
    }

    // ------------------------------------------------------- shipping rule
    // The fixed shipping is charged only when at least one PRODUCTO line is a physical product. Digital products
    // are downloads and courses are online. esDigital is read from the stored product, never from the request.

    @Test
    void digitalOnlyProducts_haveNoShipping() {
        givenUser();
        givenProduct(PRODUCTO_ID, 1000.0, true);
        givenProduct(12L, 500.0, true);

        ordenService.createOrden(USUARIO_ID, orderWith(List.of(
                itemOf("PRODUCTO", PRODUCTO_ID, 1), itemOf("PRODUCTO", 12L, 2))));

        Orden orden = savedOrder();
        assertThat(orden.getSubtotal()).isEqualTo(2000.0);
        assertThat(orden.getCostoEnvio()).isZero();
        assertThat(orden.getTotal()).isEqualTo(2000.0);
    }

    @Test
    void courseOnly_hasNoShipping() {
        givenUserAndCoursePricedAt(8000.0);

        ordenService.createOrden(USUARIO_ID, orderOf("CURSO", CURSO_ID, 1, null));

        Orden orden = savedOrder();
        assertThat(orden.getCostoEnvio()).isZero();
        assertThat(orden.getTotal()).isEqualTo(8000.0);
    }

    @Test
    void courseAndDigitalProduct_haveNoShipping() {
        givenUserAndCoursePricedAt(8000.0);
        givenProduct(PRODUCTO_ID, 1000.0, true);

        ordenService.createOrden(USUARIO_ID, orderWith(List.of(
                itemOf("CURSO", CURSO_ID, 1), itemOf("PRODUCTO", PRODUCTO_ID, 1))));

        assertThat(savedOrder().getCostoEnvio()).isZero();
    }

    @Test
    void onePhysicalProductAmongDigitalOnes_chargesTheFixedShippingOnce() {
        givenUser();
        givenProduct(PRODUCTO_ID, 1000.0, true);
        givenProduct(12L, 500.0, false);
        givenProduct(13L, 200.0, false);

        ordenService.createOrden(USUARIO_ID, orderWith(List.of(
                itemOf("PRODUCTO", PRODUCTO_ID, 1), itemOf("PRODUCTO", 12L, 1), itemOf("PRODUCTO", 13L, 3))));

        Orden orden = savedOrder();
        assertThat(orden.getSubtotal()).isEqualTo(2100.0);
        assertThat(orden.getCostoEnvio()).isEqualTo(ENVIO_PRODUCTOS_FISICOS);
        assertThat(orden.getTotal()).isEqualTo(3600.0);
    }

    @Test
    void productWithUnknownDigitalFlag_isTreatedAsPhysical() {
        givenUser();
        givenProduct(PRODUCTO_ID, 1000.0, null);

        ordenService.createOrden(USUARIO_ID, orderWith(List.of(itemOf("PRODUCTO", PRODUCTO_ID, 1))));

        assertThat(savedOrder().getCostoEnvio()).isEqualTo(ENVIO_PRODUCTOS_FISICOS);
    }

    @Test
    void shippingIsNotPartOfTheCouponSubtotal_andANoShippingOrderStillGetsItsDiscount() {
        givenUser();
        givenProduct(PRODUCTO_ID, 1000.0, true);
        when(cuponRepository.findByCodigoIgnoreCase("VERANO10")).thenReturn(Optional.of(cuponBase().build()));
        when(cuponRepository.incrementarUsosSiDisponible(7L)).thenReturn(1);
        OrdenRequest request = orderWith(List.of(itemOf("PRODUCTO", PRODUCTO_ID, 2)));
        request.setCodigoCupon("VERANO10");

        ordenService.createOrden(USUARIO_ID, request);

        Orden orden = savedOrder();
        assertThat(orden.getDescuento()).isEqualTo(200.0);
        assertThat(orden.getCostoEnvio()).isZero();
        assertThat(orden.getTotal()).isEqualTo(1800.0);
    }

    @Test
    void whenTheAtomicIncrementUpdatesNoRows_throws_andNoOrderIsSaved() {
        // Two buyers validated the last remaining use at the same time; the loser gets 0 updated rows.
        givenUserAndProductPricedAt(1000.0);
        when(cuponRepository.findByCodigoIgnoreCase("VERANO10"))
                .thenReturn(Optional.of(cuponBase().maxUsos(1).usosCount(0).build()));
        when(cuponRepository.incrementarUsosSiDisponible(7L)).thenReturn(0);

        assertThatThrownBy(() -> ordenService.createOrden(USUARIO_ID, orderOfTwoProducts("VERANO10")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El cupón ya alcanzó su límite de usos");

        verify(ordenRepository, never()).save(any());
    }
}
