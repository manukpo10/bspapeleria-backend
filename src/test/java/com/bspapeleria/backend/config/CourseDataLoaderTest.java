package com.bspapeleria.backend.config;

import com.bspapeleria.backend.entity.Curso;
import com.bspapeleria.backend.repository.CursoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Captor;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The seeder runs on every startup against the production database, and it decides "missing" by slug. These tests
 * pin down that decision: it uses the slug the seed course declares, never one derived from its title, so a restart
 * can not insert a duplicate of a course that is already live.
 */
@ExtendWith(MockitoExtension.class)
class CourseDataLoaderTest {

    /** The slugs of the courses that are live in production. They were derived from the first title an admin typed. */
    private static final List<String> LIVE_SLUGS = List.of(
            "agendas-en-powerpoint",
            "etiquetas-escolares-en-powerpoint",
            "candy-bar-powerpoint");

    /** The legacy seed courses, inactive in production but still present in the table. */
    private static final List<String> LEGACY_SLUGS = List.of(
            "sublimacion-desde-cero",
            "lettering-basico",
            "diseno-grafico-para-no-disenhadores");

    @Mock CursoRepository cursoRepository;
    @Captor ArgumentCaptor<List<Curso>> saved;

    CourseDataLoader loader;

    @BeforeEach
    void setUp() {
        loader = new CourseDataLoader(cursoRepository);
    }

    private void givenTableHolds(List<String> slugs) {
        when(cursoRepository.findAll()).thenReturn(slugs.stream()
                .map(slug -> Curso.builder().slug(slug).titulo("Título cambiado por un admin").build())
                .toList());
    }

    @Test
    void restartAgainstProductionData_insertsNothing() {
        List<String> productionSlugs = new ArrayList<>(LIVE_SLUGS);
        productionSlugs.addAll(LEGACY_SLUGS);
        givenTableHolds(productionSlugs);

        loader.run();

        verify(cursoRepository, never()).saveAll(any());
    }

    @Test
    void neverInsertsACourseWhoseSlugIsAlreadyLive() {
        givenTableHolds(LIVE_SLUGS);

        loader.run();

        verify(cursoRepository).saveAll(saved.capture());
        assertThat(saved.getValue()).extracting(Curso::getSlug).doesNotContainAnyElementsOf(LIVE_SLUGS);
    }

    @Test
    void everySeedCourseDeclaresItsSlug_andPrePersistKeepsIt() {
        givenTableHolds(List.of());

        loader.run();

        verify(cursoRepository).saveAll(saved.capture());
        assertThat(saved.getValue()).isNotEmpty().allSatisfy(curso -> {
            String declared = curso.getSlug();
            assertThat(declared).isNotBlank();
            ReflectionTestUtils.invokeMethod(curso, "onCreate");
            assertThat(curso.getSlug()).isEqualTo(declared);
        });
    }
}
