package com.bspapeleria.backend.config;

import com.bspapeleria.backend.entity.Producto;
import com.bspapeleria.backend.repository.ProductoRepository;
import com.bspapeleria.backend.entity.Producto.Categoria;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProductDataLoader implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    @Override
    public void run(String... args) {
        if (productoRepository.count() > 0) {
            return;
        }

        List<Producto> productos = List.of(
            // =====================
            // ARCHIVOS DIGITALES
            // =====================
            crearProducto("banners", "Banners", "Archivos digitales para imprimir banners de alta resolución. Formato PDF y PNG.", 1500.0, 10, true, Categoria.archivos_digitales),
            crearProducto("kit-agendas", "Kit de Agendas", "Pack completo con tapa, contratapa, hojas internas y separadores. Formato A5.", 2500.0, 25, true, Categoria.archivos_digitales),
            crearProducto("san-valentin", "San Valentín", "Diseños románticos para San Valentín. Invitaciones, marcos y banners.", 1800.0, 15, true, Categoria.archivos_digitales),
            crearProducto("candybar", "Candybar", "Kit completo para candybar temático. Etiquetas, banners y toppers.", 2200.0, 12, false, Categoria.archivos_digitales),
            crearProducto("fechas-especiales", "Fechas Especiales", "Diseños para navidad, año nuevo, pascuas y más fechas especiales.", 2000.0, 20, false, Categoria.archivos_digitales),

            // =====================
            // PRODUCTOS PERSONALIZADOS
            // =====================
            crearProducto("agendas", "Agendas", "Agendas personalizadas con nombre y diseño exclusivo. Tapa dura resistente.", 4500.0, 8, true, Categoria.personalizados),
            crearProducto("cuadernos", "Cuadernos", "Cuadernos personalizados con el diseño que elijas. Tapa blanda o dura.", 3500.0, 15, true, Categoria.personalizados),
            crearProducto("libretas", "Libretas", "Libretas de lujo con tapas de cuero sintético. espiral doble.", 4000.0, 10, true, Categoria.personalizados),
            crearProducto("blocks-arranques", "Blocks de Arranques", "Blocks de 50 hojas para registrar ingresos y gastos.", 1200.0, 30, false, Categoria.personalizados),
            crearProducto("blocks-recetarios", "Blocks de Recetarios", "Blocks de recetas con diseño culinario. 30 hojas.", 1000.0, 25, false, Categoria.personalizados),
            crearProducto("blocks-talonarios", "Talonarios", "Talonarios numerados para facturas, recibos y remitos.", 800.0, 40, false, Categoria.personalizados),
            crearProducto("blocks-facturadores", "Facturadores", "Blocks de facturación con diseño profesional.", 900.0, 35, false, Categoria.personalizados),
            crearProducto("cuaderno-pediatrico", "Cuaderno Pediátrico", "Cuaderno de control de salud para bebés y niños. 40 páginas.", 2800.0, 12, false, Categoria.personalizados),
            crearProducto("album-fotos", "Álbum de Fotos", "Álbum de fotos creativo con 20 hojas adhesivas.", 5200.0, 6, false, Categoria.personalizados),
            crearProducto("tarjetas-personales", "Tarjetas Personales", "Tarjetas de visita impresas en cartulina reciclada. Pack 100 unidades.", 1500.0, 50, true, Categoria.personalizados),
            crearProducto("almanaques", "Almanaques", "Almanaques de pared 2025 con diseño artístico. Formato A3.", 1800.0, 20, false, Categoria.personalizados),

            // =====================
            // SUBLIMABLES
            // =====================
            crearProducto("remeras", "Remeras Sublimables", "Remeras de algodón 100% para sublimar. Talles S a XXL.", 5500.0, 15, true, Categoria.sublimables),
            crearProducto("bolsas-griselina", "Bolsas de Griselina", "Bolsas de tela griselina para sublimar. Pack 10 unidades.", 3200.0, 20, true, Categoria.sublimables),
            crearProducto("placas-aluminio", "Placas de Aluminio Sublimable", "Placas de aluminio 20x30cm para fotografías y decoración.", 4800.0, 10, false, Categoria.sublimables),
            crearProducto("tazas-ceramica", "Tazas de Cerámica", "Tazas de cerámica blanca para sublimar. Capacidad 350ml.", 2800.0, 25, true, Categoria.sublimables),
            crearProducto("tazas-plastico", "Tazas de Plástico", "Tazas de plástico rígido para sublimar. Pack 6 unidades.", 2200.0, 18, false, Categoria.sublimables),

            // =====================
            // FIESTAS
            // =====================
            crearProducto("invitaciones-digitales", "Invitaciones Digitales", "Invitaciones editables en PDF. Pack 10 unidades.", 1200.0, 40, true, Categoria.fiestas),
            crearProducto("pulseras", "Pulseras de Fête", "Pulseras de silicona personalizadas. Pack 50 unidades.", 2000.0, 30, false, Categoria.fiestas),
            crearProducto("bolsitas", "Bolsitas Decorativas", "Bolsitas de organza para souvenirs. Pack 20 unidades.", 1500.0, 25, false, Categoria.fiestas),
            crearProducto("banderines", "Banderines", "Banderines de papel kraft para decorar fiestas. Pack 20 unidades.", 1000.0, 35, true, Categoria.fiestas),
            crearProducto("pinatas", "Piñatas", "Piñatas personalizadas en forma de números o personajes.", 4500.0, 5, false, Categoria.fiestas),
            crearProducto("tatuajes-temporales", "Tatuajes Temporales", "Tatuajes de vinilo adhesivo personalizados. Pack 30 unidades.", 1800.0, 22, false, Categoria.fiestas),
            crearProducto("toppers-tortas", "Toppers de Tortas", "Toppers de madera o acrylic para tortas. Pack 5 unidades.", 2500.0, 15, false, Categoria.fiestas),
            crearProducto("tripticos", "Trípticos", "Trípticos informativos para eventos. Pack 10 unidades.", 2200.0, 20, false, Categoria.fiestas),
            crearProducto("pulseras-vip", "Pulseras VIP", "Pulseras de tela para eventos. Pack 100 unidades.", 1800.0, 40, false, Categoria.fiestas),
            crearProducto("invitacion-papel", "Invitación en Papel", "Invitaciones impresas en papel glossy. Pack 10 unidades.", 2800.0, 18, false, Categoria.fiestas),
            crearProducto("tarjetas-bautismo", "Tarjetas para Bautismo/Comunión", "Tarjetas elegantes para bautismo y comunión. Pack 10 unidades.", 3200.0, 15, false, Categoria.fiestas),

            // =====================
            // CARTELERÍA
            // =====================
            crearProducto("banners-carteleria", "Banners Publicitarios", "Banners de lona resistente para publicidad exterior. 3x1 metros.", 8500.0, 5, true, Categoria.carteleria),
            crearProducto("vinilos", "Vinilos Adhesivos", "Vinilos de corte para señalización y decoración. Metro lineal.", 1200.0, 30, false, Categoria.carteleria),
            crearProducto("vinilos-microperforados", "Vinilos Microperforados", "Vinilos microperforados para vidrieras. Metro lineal.", 1500.0, 25, false, Categoria.carteleria),
            crearProducto("agendas-carteleria", "Agendas de Escritorio", "Agendas de escritorio tamaño A5. Pasta dura resistente.", 5200.0, 8, false, Categoria.carteleria),
            crearProducto("tarjetas-personales-carteleria", "Tarjetas de Visita Premium", "Tarjetas de visita en cartulina premium 350g. Pack 100.", 2500.0, 35, true, Categoria.carteleria),
            crearProducto("tazas-ceramica-carteleria", "Tazas de Cerámica Premium", "Tazas de cerámica blancas 450ml. Sublimación de alta calidad.", 3500.0, 20, true, Categoria.carteleria),
            crearProducto("tazas-plastico-carteleria", "Tazas Plástico Resistentes", "Tazas de plástico irrompible para eventos. Pack 12.", 2800.0, 22, false, Categoria.carteleria)
        );

        productoRepository.saveAll(productos);
        System.out.println("=== " + productos.size() + " productos cargados ===");
    }

    private Producto crearProducto(String slug, String nombre, String descripcion, Double precio, Integer stock, Boolean destacado, Categoria categoria) {
        return Producto.builder()
                .slug(slug)
                .nombre(nombre)
                .descripcion(descripcion)
                .descripcionCorta(descripcion.length() > 100 ? descripcion.substring(0, 100) + "..." : descripcion)
                .precio(precio)
                .precioComparacion(precio * 1.2)
                .imagenes(Set.of("https://picsum.photos/seed/" + slug + "/400/400"))
                .categoria(categoria)
                .tags(Set.of(nombre.toLowerCase(), "papeleria"))
                .stock(stock)
                .esDigital(false)
                .activo(true)
                .destacado(destacado)
                .rating(Math.round(Math.random() * 20) / 10.0)
                .reviewsCount((int) (Math.random() * 50))
                .build();
    }
}