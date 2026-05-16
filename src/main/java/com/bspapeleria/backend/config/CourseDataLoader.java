package com.bspapeleria.backend.config;

import com.bspapeleria.backend.entity.Curso;
import com.bspapeleria.backend.entity.Leccion;
import com.bspapeleria.backend.repository.CursoRepository;
import com.bspapeleria.backend.entity.Curso.Nivel;
import com.bspapeleria.backend.entity.Curso.Modalidad;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseDataLoader implements CommandLineRunner {

    private final CursoRepository cursoRepository;

    @Override
    public void run(String... args) {
        if (cursoRepository.count() > 0) {
            return;
        }

        List<Curso> cursos = List.of(
            crearCurso(
                "sublimacion-desde-cero",
                "Sublimación desde Cero",
                "Aprendé todo sobre sublimación: desde la elección de productos hasta la impresión y transfer. Vas a crear remeras, tazas, placas y más.",
                7500.0,
                Nivel.principiante,
                Modalidad.mixto,
                "María García",
                12,
                List.of("sublimación", "merchandising", "remeras", "tazas", "regalos"),
                List.of(
                    crearLeccion("¿Qué es la sublimación?", "La sublimación es un proceso de transferencia de tinta a través del calor. Explicamos los fundamentos químicos y por qué funciona en productos coated.", "https://www.youtube.com/watch?v=ejemplo1", 8, true),
                    crearLeccion("Equipos necesarios", "Listado completo de equipamiento: sublimadora, papel sublimación, productos base, prensa y accesorios.", null, 12, true),
                    crearLeccion("Materiales y proveedores", "Dónde comprar productos en Argentina. Comparativa de proveedores confiables y tips para ahorrar.", null, 15, false),
                    crearLeccion("Configuración de archivos", "Photoshop, Illustrator y Canva: ajustes de resolución, color profile (sRGB vs CMYK) y formatos de exportación.", "https://www.youtube.com/watch?v=ejemplo2", 18, false),
                    crearLeccion("Sublimando remeras", "Tutorial paso a paso: posición de la tela, temperatura, tiempo y presión. Errores comunes y cómo evitarlos.", "https://www.youtube.com/watch?v=ejemplo3", 20, false),
                    crearLeccion("Sublimando tazas", "Técnica específica para tazas cerámicas y plástico. Diferencias de temperatura y tiempos según material.", "https://www.youtube.com/watch?v=ejemplo4", 15, false),
                    crearLeccion("Placas de aluminio", "Trabajo con placas sublimables: configuración de prensa, presión uniforme y finish final.", null, 12, false),
                    crearLeccion("Diseño de pedidos personalizados", "Cómo hacer presupuestos, mockups profesionales y comunicación con clientes.", null, 10, false),
                    crearLeccion("Crear una marca", "Estrategias de branding para sublimadores: nombre, logo, redes sociales y packaging.", null, 14, false),
                    crearLeccion("Calculadora de costos", "Hoja de cálculo para pricing: materiales, tiempo, costos fijos y margen de ganancia.", null, 8, false),
                    crearLeccion("Marketing en redes", "Instagram, TikTok y WhatsApp para vender sublimación. Posts, reels y atención al cliente.", "https://www.youtube.com/watch?v=ejemplo5", 16, false),
                    crearLeccion("Proyecto final: kit completo", "Armamos un kit de regalo desde cero: remera + taza + placa + packaging.", "https://www.youtube.com/watch?v=ejemplo6", 25, true)
                )
            ),
            crearCurso(
                "lettering-basico",
                "Lettering para Principiantes",
                "Domina la caligrafía manual y lettering desde cero. Aprendé alphabet, estilos, espaciado y composición para crear diseños hermosos.",
                5500.0,
                Nivel.principiante,
                Modalidad.mixto,
                "Sofía Martínez",
                10,
                List.of("lettering", "caligrafía", "handlettering", "diseño", "-tipografia"),
                List.of(
                    crearLeccion("Introducción al lettering", "Qué es el lettering, diferencia con caligrafía y tipografía. Instrumentos básicos: pinceles, rotuladores, plumas.", "https://www.youtube.com/watch?v=ejemplo10", 10, true),
                    crearLeccion("Alfabeto básico en brush lettering", "Practica el alphabet con rotulador brush. Trazos ascendentes, descendentes y conexión de letras.", "https://www.youtube.com/watch?v=ejemplo11", 18, false),
                    crearLeccion("Espaciado y baselines", "Cómo medir espacios uniformes entre letras. Importancia del baseline y x-height en compositions.", null, 12, false),
                    crearLeccion("Letras mayúsculas ornamentadas", "Estilo decorativo para mayúsculas con stroke ancho. Flourishes y adornos básicos.", "https://www.youtube.com/watch?v=ejemplo12", 15, false),
                    crearLeccion("Kerning y tracking", "Técnicas de ajuste visual entre letras. Cómo hacer que el texto fluya naturalmente.", null, 10, false),
                    crearLeccion("Composición de frases", "Armar palabras y frases completas. Ejercicios con quotes cortos y medianas longitudes.", "https://www.youtube.com/watch?v=ejemplo13", 20, false),
                    crearLeccion("Estilo boho chic", "Aprendé el estilo libre y orgánico. Flourishes, hojas y elementos naturales en compositions.", null, 14, false),
                    crearLeccion("Digitalizar lettering", "Cómo escanear o fotografiar trabajos اليدوي. Vectorización básica en Illustrator o Vectorizer.", null, 12, false),
                    crearLeccion("Crear una piece decorativa", "Proyecto final: diseño de piece completo con quotes, flourishes y elementos decorativos.", "https://www.youtube.com/watch?v=ejemplo14", 22, true),
                    crearLeccion("Recursos y práctica continua", "Dónde encontrar más inspiración, libros y ejercicios para seguir mejorando.", null, 8, false)
                )
            ),
            crearCurso(
                "diseno-grafico-para-no-disenhadores",
                "Diseño Gráfico para No Diseñadores",
                "Creá gráficos profesionales para redes sociales, packaging ycartelería sin ser diseñador. Aprendé a usar Canva y principios de diseño.",
                6000.0,
                Nivel.principiante,
                Modalidad.video,
                "Carlos Pérez",
                8,
                List.of("diseño", "canva", "gráfico", "redes sociales", "packaging", "carteleria"),
                List.of(
                    crearLeccion("Principios de diseño visual", "Contraste, jerarquía, alineación, repetición y proximidad. Cómo aplicarlos en cualquier pieza.", "https://www.youtube.com/watch?v=ejemplo20", 12, true),
                    crearLeccion("Canva profesional: tips avanzados", "Funciones menos conocidas de Canva:，品牌 colours, templates personalizados y elementos vectoriales.", "https://www.youtube.com/watch?v=ejemplo21", 18, false),
                    crearLeccion("Palette de colores", "Cómo crear paletas de colores armónicas. Teoría del color y herramientas útiles.", null, 10, false),
                    crearLeccion("Tipografía para no diseñadores", "Combinar fuentes sin romper el diseño. Google Fonts, pairing y tamaño correcto.", "https://www.youtube.com/watch?v=ejemplo22", 14, false),
                    crearLeccion("Stories de Instagram", "Plantillas para stories: portadas, countdowns, carruseles educativos y promos.", "https://www.youtube.com/watch?v=ejemplo23", 15, false),
                    crearLeccion("Packaging básico", "Cómo diseñar etiquetas, cajas y packaging simple para productos artesanales.", null, 12, false),
                    crearLeccion("Cartelería para eventos", "Diseñar entradas, números de mesa y banners para bodas y cumpleaños.", "https://www.youtube.com/watch?v=ejemplo24", 16, false),
                    crearLeccion("Portfolio visual", "Cómo armar un portfolio digital atractivo para mostrar tu trabajo. Proyecto final.", "https://www.youtube.com/watch?v=ejemplo25", 20, true)
                )
            )
        );

        for (Curso curso : cursos) {
            for (Leccion leccion : curso.getLecciones()) {
                leccion.setCurso(curso);
            }
        }
        cursoRepository.saveAll(cursos);
        System.out.println("=== " + cursos.size() + " cursos cargados con " + cursos.stream().mapToInt(c -> c.getLecciones().size()).sum() + " lecciones ===");
    }

    private Curso crearCurso(String slug, String titulo, String descripcion, Double precio, Nivel nivel, Modalidad modalidad, String instructor, Integer duracionHoras, List<String> tags, List<Leccion> lecciones) {
        return Curso.builder()
                .slug(slug)
                .titulo(titulo)
                .descripcion(descripcion)
                .imagenUrl("https://picsum.photos/seed/" + slug + "/600/400")
                .precio(precio)
                .precioComparacion(precio * 1.2)
                .nivel(nivel)
                .modalidad(modalidad)
                .instructor(instructor)
                .duracionHoras(duracionHoras)
                .tags(tags)
                .lecciones(lecciones)
                .estudiantesCount((int) (Math.random() * 100))
                .rating(3.5 + Math.round(Math.random() * 15) / 10.0)
                .build();
    }

    private Leccion crearLeccion(String titulo, String contenido, String urlVideo, Integer duracionMinutos, Boolean esPreview) {
        return Leccion.builder()
                .titulo(titulo)
                .contenido(contenido)
                .urlVideo(urlVideo)
                .duracionMinutos(duracionMinutos)
                .esPreview(esPreview)
                .build();
    }
}