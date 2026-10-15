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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the optional one-sentence course summary ({@code descripcionCorta}) in {@link CursoService}.
 * No Spring context and no database: the repository is a mock.
 *
 * <p>The field travels like {@code descripcion} (overwritten on every update, null when the request omits it),
 * with one difference: it is trimmed, and a blank value is stored as {@code null}.
 */
@ExtendWith(MockitoExtension.class)
class CursoServiceDescripcionCortaTest {

    private static final long CURSO_ID = 5L;
    private static final String FRASE = "Aprendé a diseñar y crear agendas desde cero.";

    @Mock CursoRepository cursoRepository;

    CursoService cursoService;

    @BeforeEach
    void setUp() {
        cursoService = new CursoService(cursoRepository);
    }

    // ---------------------------------------------------------------- helpers

    private static CursoRequest request(String descripcionCorta) {
        CursoRequest request = new CursoRequest();
        request.setTitulo("El Mundo de las Agendas");
        request.setDescripcion("Descripción larga del curso.");
        request.setPrecio(40000.0);
        request.setNivel("principiante");
        request.setModalidad("video");
        request.setDescripcionCorta(descripcionCorta);
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
    void create_storesTheShortDescription() {
        assertThat(savedByCreate(request(FRASE)).getDescripcionCorta()).isEqualTo(FRASE);
    }

    @Test
    void create_trimsTheShortDescription() {
        assertThat(savedByCreate(request("  " + FRASE + " \t\n")).getDescripcionCorta()).isEqualTo(FRASE);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "   ", "\t", "\n", " \t \n "})
    void create_storesNullWhenTheShortDescriptionIsMissingOrBlank(String blank) {
        assertThat(savedByCreate(request(blank)).getDescripcionCorta()).isNull();
    }

    @Test
    void create_leavesTheLongDescriptionUntouched() {
        CursoRequest request = request(FRASE);
        request.setDescripcion("  Larga, con espacios a los costados.  ");

        assertThat(savedByCreate(request).getDescripcion()).isEqualTo("  Larga, con espacios a los costados.  ");
    }

    @Test
    void create_responseExposesTheTrimmedShortDescription() {
        CursoResponse response = cursoService.createCurso(request("  " + FRASE + "  "));

        assertThat(response.getDescripcionCorta()).isEqualTo(FRASE);
    }

    @Test
    void create_responseHasNullShortDescriptionWhenBlank() {
        assertThat(cursoService.createCurso(request("   ")).getDescripcionCorta()).isNull();
    }

    // ---------------------------------------------------------------- update

    @Test
    void update_changesTheShortDescription() {
        Curso existing = cursoBase().descripcionCorta("Frase vieja.").build();
        givenExisting(existing);

        cursoService.updateCurso(CURSO_ID, request(FRASE));

        assertThat(existing.getDescripcionCorta()).isEqualTo(FRASE);
        verify(cursoRepository).save(existing);
    }

    @Test
    void update_trimsTheShortDescription() {
        Curso existing = cursoBase().descripcionCorta("Frase vieja.").build();
        givenExisting(existing);

        cursoService.updateCurso(CURSO_ID, request("\t " + FRASE + "  "));

        assertThat(existing.getDescripcionCorta()).isEqualTo(FRASE);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t\n"})
    void update_clearsTheShortDescriptionWhenTheRequestIsMissingOrBlank(String blank) {
        // Same overwrite semantics as `descripcion`: an update replaces the stored value with what the request carries.
        Curso existing = cursoBase().descripcionCorta("Frase vieja.").build();
        givenExisting(existing);

        cursoService.updateCurso(CURSO_ID, request(blank));

        assertThat(existing.getDescripcionCorta()).isNull();
    }

    @Test
    void update_leavesTheLongDescriptionUpdatedAsBefore() {
        Curso existing = cursoBase().descripcionCorta("Frase vieja.").build();
        givenExisting(existing);
        CursoRequest request = request(FRASE);
        request.setDescripcion("  Nueva larga.  ");

        cursoService.updateCurso(CURSO_ID, request);

        assertThat(existing.getDescripcion()).isEqualTo("  Nueva larga.  ");
    }

    @Test
    void update_responseExposesTheNewShortDescription() {
        givenExisting(cursoBase().descripcionCorta("Frase vieja.").build());

        CursoResponse response = cursoService.updateCurso(CURSO_ID, request("  " + FRASE + " "));

        assertThat(response.getDescripcionCorta()).isEqualTo(FRASE);
    }

    // ---------------------------------------------------------------- read mapping

    @Test
    void getCursoBySlug_exposesTheShortDescription() {
        when(cursoRepository.findBySlug("agendas-en-powerpoint"))
                .thenReturn(Optional.of(cursoBase().descripcionCorta(FRASE).build()));

        assertThat(cursoService.getCursoBySlug("agendas-en-powerpoint").getDescripcionCorta()).isEqualTo(FRASE);
    }

    @Test
    void getCursoById_exposesTheShortDescription() {
        givenExisting(cursoBase().descripcionCorta(FRASE).build());

        assertThat(cursoService.getCursoById(CURSO_ID).getDescripcionCorta()).isEqualTo(FRASE);
    }

    @Test
    void listing_exposesTheShortDescriptionOnEveryCard() {
        PageRequest pageable = PageRequest.of(0, 12);
        when(cursoRepository.findByActivoTrue(pageable)).thenReturn(new PageImpl<>(List.of(
                cursoBase().descripcionCorta(FRASE).build(),
                cursoBase().id(6L).slug("sin-frase").descripcionCorta(null).build())));

        List<CursoResponse> cards = cursoService.getAllCursos(pageable).getContent();

        assertThat(cards).extracting(CursoResponse::getDescripcionCorta).containsExactly(FRASE, null);
    }
}
