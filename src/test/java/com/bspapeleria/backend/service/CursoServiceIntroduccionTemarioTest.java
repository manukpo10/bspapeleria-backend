package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.CursoRequest;
import com.bspapeleria.backend.dto.CursoResponse;
import com.bspapeleria.backend.entity.Curso;
import com.bspapeleria.backend.repository.CursoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the optional syllabus intro sentence ({@code introduccionTemario}) in {@link CursoService}.
 * No Spring context and no database: the repository is a mock.
 *
 * <p>It follows the same rules as {@code descripcionCorta}: trimmed, blank stored as {@code null}, kept on update
 * when the request omits it and cleared when the request sends it blank.
 */
@ExtendWith(MockitoExtension.class)
class CursoServiceIntroduccionTemarioTest {

    private static final long CURSO_ID = 5L;
    private static final String INTRO = "Un recorrido paso a paso, desde tus primeros diseños en PowerPoint "
            + "hasta la creación de agendas listas para usar o vender.";

    @Mock CursoRepository cursoRepository;

    CursoService cursoService;

    @BeforeEach
    void setUp() {
        cursoService = new CursoService(cursoRepository);
    }

    // ---------------------------------------------------------------- helpers

    private static CursoRequest request(String introduccionTemario) {
        CursoRequest request = new CursoRequest();
        request.setTitulo("El Mundo de las Agendas");
        request.setDescripcion("Descripción larga del curso.");
        request.setPrecio(40000.0);
        request.setNivel("principiante");
        request.setModalidad("video");
        request.setIntroduccionTemario(introduccionTemario);
        return request;
    }

    private static Curso.CursoBuilder cursoBase() {
        return Curso.builder()
                .id(CURSO_ID)
                .slug("agendas-en-powerpoint")
                .titulo("El Mundo de las Agendas")
                .descripcion("Descripción larga del curso.")
                .precio(40000.0)
                .nivel(Curso.Nivel.principiante)
                .modalidad(Curso.Modalidad.video)
                .activo(true);
    }

    private Curso savedByCreate(CursoRequest request) {
        cursoService.createCurso(request);
        ArgumentCaptor<Curso> captor = ArgumentCaptor.forClass(Curso.class);
        verify(cursoRepository).save(captor.capture());
        return captor.getValue();
    }

    private void givenExisting(Curso curso) {
        when(cursoRepository.findByIdWithModulos(CURSO_ID)).thenReturn(Optional.of(curso));
    }

    // ---------------------------------------------------------------- create

    @Test
    void create_storesTheTrimmedSyllabusIntro() {
        assertThat(savedByCreate(request("  " + INTRO + " \t\n")).getIntroduccionTemario()).isEqualTo(INTRO);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t\n"})
    void create_storesNullWhenTheSyllabusIntroIsMissingOrBlank(String blank) {
        assertThat(savedByCreate(request(blank)).getIntroduccionTemario()).isNull();
    }

    @Test
    void create_keepsTheSyllabusIntroIndependentFromTheShortDescription() {
        CursoRequest request = request(INTRO);
        request.setDescripcionCorta("Frase corta.");

        Curso saved = savedByCreate(request);

        assertThat(saved.getIntroduccionTemario()).isEqualTo(INTRO);
        assertThat(saved.getDescripcionCorta()).isEqualTo("Frase corta.");
    }

    @Test
    void create_responseExposesTheSyllabusIntro() {
        CursoResponse response = cursoService.createCurso(request("  " + INTRO + "  "));

        assertThat(response.getIntroduccionTemario()).isEqualTo(INTRO);
    }

    // ---------------------------------------------------------------- update

    @Test
    void update_changesAndTrimsTheSyllabusIntro() {
        Curso existing = cursoBase().introduccionTemario("Intro vieja.").build();
        givenExisting(existing);

        cursoService.updateCurso(CURSO_ID, request("\t " + INTRO + "  "));

        assertThat(existing.getIntroduccionTemario()).isEqualTo(INTRO);
        verify(cursoRepository).save(existing);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n"})
    void update_clearsTheSyllabusIntroWhenTheRequestSendsItBlank(String blank) {
        Curso existing = cursoBase().introduccionTemario("Intro vieja.").build();
        givenExisting(existing);

        cursoService.updateCurso(CURSO_ID, request(blank));

        assertThat(existing.getIntroduccionTemario()).isNull();
    }

    @Test
    void update_keepsTheSyllabusIntroWhenTheRequestOmitsIt() {
        // An admin tab still running a bundle from before the field existed sends no introduccionTemario at all:
        // saving there must not wipe it. The current admin always sends the field, '' to clear it
        Curso existing = cursoBase().introduccionTemario("Intro vieja.").build();
        givenExisting(existing);

        cursoService.updateCurso(CURSO_ID, request(null));

        assertThat(existing.getIntroduccionTemario()).isEqualTo("Intro vieja.");
    }

    @Test
    void update_responseExposesTheNewSyllabusIntro() {
        givenExisting(cursoBase().introduccionTemario("Intro vieja.").build());

        CursoResponse response = cursoService.updateCurso(CURSO_ID, request("  " + INTRO + " "));

        assertThat(response.getIntroduccionTemario()).isEqualTo(INTRO);
    }

    // ---------------------------------------------------------------- read mapping

    @Test
    void getCursoBySlug_exposesTheSyllabusIntro() {
        when(cursoRepository.findBySlug("agendas-en-powerpoint"))
                .thenReturn(Optional.of(cursoBase().introduccionTemario(INTRO).build()));

        assertThat(cursoService.getCursoBySlug("agendas-en-powerpoint").getIntroduccionTemario()).isEqualTo(INTRO);
    }
}
