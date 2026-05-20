package com.bspapeleria.backend.config;

import com.bspapeleria.backend.entity.Curso;
import com.bspapeleria.backend.entity.Leccion;
import com.bspapeleria.backend.entity.Modulo;
import com.bspapeleria.backend.repository.CursoRepository;
import com.bspapeleria.backend.entity.Curso.Nivel;
import com.bspapeleria.backend.entity.Curso.Modalidad;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourseDataLoader implements CommandLineRunner {

    private final CursoRepository cursoRepository;

    @Override
    public void run(String... args) {
        Set<String> existingSlugs = cursoRepository.findAll().stream()
                .map(Curso::getSlug)
                .collect(Collectors.toSet());

        // candy-bar-powerpoint ya existe en la BD con lecciones viejas — lo saltamos
        // Los demás cursos se crean con módulos si no existen
        List<Curso> cursos = new ArrayList<>();

        if (!existingSlugs.contains("sublimacion-desde-cero")) {
            cursos.add(buildCurso(
                "sublimacion-desde-cero",
                "Sublimación desde Cero",
                "Aprendé todo sobre sublimación: desde la elección de productos hasta la impresión y transfer.",
                7500.0, Nivel.principiante, Modalidad.mixto, "María García", 12,
                List.of("sublimación", "merchandising", "remeras", "tazas"),
                List.of(
                    buildModulo("Módulo 1: Fundamentos", "Introducción al mundo de la sublimación.", 0, List.of(
                        buildLeccion("¿Qué es la sublimación?", "Fundamentos del proceso.", "https://www.youtube.com/watch?v=ejemplo1", 8, true, 0),
                        buildLeccion("Equipos necesarios", "Listado de equipamiento.", null, 12, true, 1),
                        buildLeccion("Materiales y proveedores", "Dónde comprar en Argentina.", null, 15, false, 2)
                    )),
                    buildModulo("Módulo 2: Técnicas", "Sublimación en distintos materiales.", 1, List.of(
                        buildLeccion("Sublimando remeras", "Tutorial paso a paso.", "https://www.youtube.com/watch?v=ejemplo3", 20, false, 0),
                        buildLeccion("Sublimando tazas", "Temperatura y tiempos.", "https://www.youtube.com/watch?v=ejemplo4", 15, false, 1),
                        buildLeccion("Placas de aluminio", "Trabajo con placas sublimables.", null, 12, false, 2)
                    )),
                    buildModulo("Módulo 3: Negocio", "Cómo vender tus productos sublimados.", 2, List.of(
                        buildLeccion("Calculadora de costos", "Pricing y margen de ganancia.", null, 8, false, 0),
                        buildLeccion("Marketing en redes", "Instagram, TikTok y WhatsApp.", "https://www.youtube.com/watch?v=ejemplo5", 16, false, 1)
                    ))
                ),
                null
            ));
        }

        if (!existingSlugs.contains("lettering-basico")) {
            cursos.add(buildCurso(
                "lettering-basico",
                "Lettering para Principiantes",
                "Domina la caligrafía manual y lettering desde cero.",
                5500.0, Nivel.principiante, Modalidad.mixto, "Sofía Martínez", 10,
                List.of("lettering", "caligrafía", "diseño"),
                List.of(
                    buildModulo("Módulo 1: Primeros pasos", "Introducción y materiales.", 0, List.of(
                        buildLeccion("Introducción al lettering", "Qué es y materiales básicos.", "https://www.youtube.com/watch?v=ejemplo10", 10, true, 0),
                        buildLeccion("Alfabeto básico", "Practica el alphabet.", "https://www.youtube.com/watch?v=ejemplo11", 18, false, 1)
                    )),
                    buildModulo("Módulo 2: Composición", "Espaciado y composición de frases.", 1, List.of(
                        buildLeccion("Espaciado y baselines", "Métricas tipográficas.", null, 12, false, 0),
                        buildLeccion("Composición de frases", "Armar palabras y frases.", "https://www.youtube.com/watch?v=ejemplo13", 20, false, 1)
                    ))
                ),
                null
            ));
        }

        if (!existingSlugs.contains("diseno-grafico-para-no-disenhadores")) {
            cursos.add(buildCurso(
                "diseno-grafico-para-no-disenhadores",
                "Diseño Gráfico para No Diseñadores",
                "Creá gráficos profesionales para redes sociales y packaging sin ser diseñador.",
                6000.0, Nivel.principiante, Modalidad.video, "Carlos Pérez", 8,
                List.of("diseño", "canva", "redes sociales"),
                List.of(
                    buildModulo("Módulo 1: Bases del diseño", "Principios fundamentales.", 0, List.of(
                        buildLeccion("Principios de diseño visual", "Contraste, jerarquía, alineación.", "https://www.youtube.com/watch?v=ejemplo20", 12, true, 0),
                        buildLeccion("Palette de colores", "Teoría del color.", null, 10, false, 1),
                        buildLeccion("Tipografía básica", "Combinar fuentes.", "https://www.youtube.com/watch?v=ejemplo22", 14, false, 2)
                    )),
                    buildModulo("Módulo 2: Aplicaciones prácticas", "Diseño para distintos formatos.", 1, List.of(
                        buildLeccion("Stories de Instagram", "Plantillas para redes.", "https://www.youtube.com/watch?v=ejemplo23", 15, false, 0),
                        buildLeccion("Packaging básico", "Etiquetas y cajas.", null, 12, false, 1),
                        buildLeccion("Cartelería para eventos", "Banners y entradas.", "https://www.youtube.com/watch?v=ejemplo24", 16, false, 2)
                    ))
                ),
                null
            ));
        }

        if (!cursos.isEmpty()) {
            cursoRepository.saveAll(cursos);
            System.out.println("=== " + cursos.size() + " cursos nuevos cargados ===");
        }
    }

    private Curso buildCurso(String slug, String titulo, String descripcion, Double precio,
                              Nivel nivel, Modalidad modalidad, String instructor, Integer duracionHoras,
                              List<String> tags, List<Modulo> modulos, List<String> materialUrls) {
        Curso curso = Curso.builder()
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
                .tags(tags != null ? new java.util.HashSet<>(tags) : new java.util.HashSet<>())
                .modulos(modulos != null ? new HashSet<>(modulos) : new HashSet<>())
                .materialUrls(materialUrls != null ? new ArrayList<>(materialUrls) : new ArrayList<>())
                .estudiantesCount(0)
                .rating(0.0)
                .build();

        if (modulos != null) {
            for (Modulo m : modulos) {
                m.setCurso(curso);
                if (m.getLecciones() != null) {
                    for (Leccion l : m.getLecciones()) {
                        l.setModulo(m);
                    }
                }
            }
        }

        return curso;
    }

    private Modulo buildModulo(String titulo, String descripcion, int orden, List<Leccion> lecciones) {
        return Modulo.builder()
                .titulo(titulo)
                .descripcion(descripcion)
                .orden(orden)
                .lecciones(new HashSet<>(lecciones))
                .build();
    }

    private Leccion buildLeccion(String titulo, String contenido, String urlVideo,
                                  int duracionMinutos, boolean esPreview, int orden) {
        return Leccion.builder()
                .titulo(titulo)
                .contenido(contenido)
                .urlVideo(urlVideo)
                .duracionMinutos(duracionMinutos)
                .esPreview(esPreview)
                .orden(orden)
                .build();
    }
}
