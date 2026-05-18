package com.bspapeleria.backend.config;

import com.bspapeleria.backend.entity.Curso;
import com.bspapeleria.backend.entity.Leccion;
import com.bspapeleria.backend.repository.CursoRepository;
import com.bspapeleria.backend.entity.Curso.Nivel;
import com.bspapeleria.backend.entity.Curso.Modalidad;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourseDataLoader implements CommandLineRunner {

    private final CursoRepository cursoRepository;

    @Override
    public void run(String... args) {
        // Get existing slugs so we only add new courses, never overwrite
        Set<String> existingSlugs = cursoRepository.findAll().stream()
                .map(Curso::getSlug)
                .collect(Collectors.toSet());

        List<Curso> allCursos = List.of(
            crearCurso(
                "candy-bar-powerpoint",
                "Candy Bar en PowerPoint",
                "Aprendé a decorar eventos desde cero usando PowerPoint. Vamos a diseñar banderines, carteles, stickers, chipbags, milkbox, bolsitas y pochocleras.",
                25000.0,
                Nivel.principiante,
                Modalidad.video,
                "BS Papeleria",
                6,
                List.of("candybar", "powerpoint", "decoracion", "eventos", "diseño"),
                List.of(
                    crearLeccion("Presentación del curso", "Presentación general del curso Candy Bar en PowerPoint.", "https://drive.google.com/file/d/1-0_qd-BwAorNOIG29odQa7Q1dzJenFmw/preview", 5, true),
                    crearLeccion("Presentación de mesa de trabajo", "Conocé el entorno de PowerPoint y la mesa de trabajo.", "https://drive.google.com/file/d/1SczFViXWXkJXl26M0iv_aMudq7W4VNZH/preview", 8, false),
                    crearLeccion("Formas y Líneas", "Cómo usar formas y líneas en PowerPoint para crear piezas decorativas.", "https://drive.google.com/file/d/1TVDa_dE2QDe2JMd3k-B8GiAGTWcZHv9h/preview", 12, false),
                    crearLeccion("Máscara de Recorte y Relleno de Forma", "Técnicas de máscara de recorte y relleno de forma.", "https://drive.google.com/file/d/1cpLr3o6HmpYElxb5ZujqUCgSWcY6g-d7/preview", 15, false),
                    crearLeccion("Video Bonus - Descarga e Instalación de Fuentes", "Cómo descargar e instalar fuentes personalizadas en tu computadora.", "https://drive.google.com/file/d/1JGWd33kskT2wVvNrXeB8idgk6KdkeUQX/preview", 10, false)
                )
            ),
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
                    crearLeccion("Digitalizar lettering", "Cómo escanear o fotografiar trabajos manuales. Vectorización básica en Illustrator o Vectorizer.", null, 12, false),
                    crearLeccion("Crear una piece decorativa", "Proyecto final: diseño de piece completo con quotes, flourishes y elementos decorativos.", "https://www.youtube.com/watch?v=ejemplo14", 22, true),
                    crearLeccion("Recursos y práctica continua", "Dónde encontrar más inspiración, libros y ejercicios para seguir mejorando.", null, 8, false)
                )
            ),
            crearCurso(
                "diseno-grafico-para-no-disenhadores",
                "Diseño Gráfico para No Diseñadores",
                "Creá gráficos profesionales para redes sociales, packaging y cartelería sin ser diseñador. Aprendé a usar Canva y principios de diseño.",
                6000.0,
                Nivel.principiante,
                Modalidad.video,
                "Carlos Pérez",
                8,
                List.of("diseño", "canva", "gráfico", "redes sociales", "packaging", "carteleria"),
                List.of(
                    crearLeccion("Principios de diseño visual", "Contraste, jerarquía, alineación, repetición y proximidad. Cómo aplicarlos en cualquier pieza.", "https://www.youtube.com/watch?v=ejemplo20", 12, true),
                    crearLeccion("Canva profesional: tips avanzados", "Funciones menos conocidas de Canva: colores de marca, templates personalizados y elementos vectoriales.", "https://www.youtube.com/watch?v=ejemplo21", 18, false),
                    crearLeccion("Palette de colores", "Cómo crear paletas de colores armónicas. Teoría del color y herramientas útiles.", null, 10, false),
                    crearLeccion("Tipografía para no diseñadores", "Combinar fuentes sin romper el diseño. Google Fonts, pairing y tamaño correcto.", "https://www.youtube.com/watch?v=ejemplo22", 14, false),
                    crearLeccion("Stories de Instagram", "Plantillas para stories: portadas, countdowns, carruseles educativos y promos.", "https://www.youtube.com/watch?v=ejemplo23", 15, false),
                    crearLeccion("Packaging básico", "Cómo diseñar etiquetas, cajas y packaging simple para productos artesanales.", null, 12, false),
                    crearLeccion("Cartelería para eventos", "Diseñar entradas, números de mesa y banners para bodas y cumpleaños.", "https://www.youtube.com/watch?v=ejemplo24", 16, false),
                    crearLeccion("Portfolio visual", "Cómo armar un portfolio digital atractivo para mostrar tu trabajo. Proyecto final.", "https://www.youtube.com/watch?v=ejemplo25", 20, true)
                )
            )
        );

        // Only save courses that don't already exist (by slug)
        List<Curso> nuevosCursos = allCursos.stream()
                .filter(c -> !existingSlugs.contains(c.getSlug()))
                .toList();

        if (!nuevosCursos.isEmpty()) {
            for (Curso curso : nuevosCursos) {
                for (Leccion leccion : curso.getLecciones()) {
                    leccion.setCurso(curso);
                }
            }
            cursoRepository.saveAll(nuevosCursos);
            System.out.println("=== " + nuevosCursos.size() + " cursos nuevos cargados con " + nuevosCursos.stream().mapToInt(c -> c.getLecciones().size()).sum() + " lecciones ===");
        }
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
                .tags(tags != null ? new java.util.HashSet<>(tags) : null)
                .lecciones(lecciones != null ? new java.util.HashSet<>(lecciones) : null)
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