package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.CursoRequest;
import com.bspapeleria.backend.dto.CursoResponse;
import com.bspapeleria.backend.dto.PreguntaFrecuente;
import com.bspapeleria.backend.entity.Curso;
import com.bspapeleria.backend.repository.CursoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the ordered list of frequently asked questions ({@code preguntasFrecuentes}) in {@link CursoService}.
 * No Spring context and no database: the repository is a mock.
 *
 * <p>Update rule, same as {@code descripcionCorta}: a {@code null} list means "not sent" and keeps the stored one;
 * any list, even an empty one, replaces it.
 */
@ExtendWith(MockitoExtension.class)
class CursoServicePreguntasFrecuentesTest {

    private static final long CURSO_ID = 5L;

    @Mock CursoRepository cursoRepository;

    CursoService cursoService;

    @BeforeEach
    void setUp() {
        cursoService = new CursoService(cursoRepository);
    }

    // ---------------------------------------------------------------- helpers

    private static PreguntaFrecuente faq(String pregunta, String respuesta) {
        return new PreguntaFrecuente(pregunta, respuesta);
    }

    private static CursoRequest request(List<PreguntaFrecuente> preguntas) {
        CursoRequest request = new CursoRequest();
        request.setTitulo("El Mundo de las Agendas");
        request.setDescripcion("Descripción larga del curso.");
        request.setPrecio(40000.0);
        request.setNivel("principiante");
        request.setModalidad("video");
        request.setPreguntasFrecuentes(preguntas);
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
    void create_storesTheQuestionsTrimmedAndInOrder() {
        Curso saved = savedByCreate(request(List.of(
                faq("  ¿Primera?  ", "\tRespuesta uno \n"),
                faq("¿Segunda?", "Respuesta dos"),
                faq(" ¿Tercera? ", " Respuesta tres "))));

        assertThat(saved.getPreguntasFrecuentes()).containsExactly(
                faq("¿Primera?", "Respuesta uno"),
                faq("¿Segunda?", "Respuesta dos"),
                faq("¿Tercera?", "Respuesta tres"));
    }

    @Test
    void create_dropsTheRowsWhereBothTextsAreBlankAndKeepsTheOrderOfTheRest() {
        Curso saved = savedByCreate(request(Arrays.asList(
                faq("¿Primera?", "Uno"),
                faq("   ", "\t\n"),
                null,
                faq("", ""),
                faq("¿Segunda?", "Dos"))));

        assertThat(saved.getPreguntasFrecuentes()).containsExactly(faq("¿Primera?", "Uno"), faq("¿Segunda?", "Dos"));
    }

    @Test
    void create_storesAnEmptyListWhenTheRequestSendsNone() {
        assertThat(savedByCreate(request(null)).getPreguntasFrecuentes()).isNotNull().isEmpty();
    }

    @Test
    void create_doesNotShareTheListInstanceWithTheRequest() {
        List<PreguntaFrecuente> sent = new ArrayList<>(List.of(faq("¿P?", "R")));
        CursoRequest request = request(sent);

        Curso saved = savedByCreate(request);
        sent.clear();

        assertThat(saved.getPreguntasFrecuentes()).containsExactly(faq("¿P?", "R"));
    }

    @Test
    void create_responseExposesTheCleanedQuestions() {
        CursoResponse response = cursoService.createCurso(request(List.of(faq(" ¿P? ", " R "), faq("", ""))));

        assertThat(response.getPreguntasFrecuentes()).containsExactly(faq("¿P?", "R"));
    }

    // ---------------------------------------------------------------- update

    @Test
    void update_replacesTheStoredListWhenAListIsSent() {
        Curso existing = cursoBase()
                .preguntasFrecuentes(List.of(faq("¿Vieja 1?", "Uno"), faq("¿Vieja 2?", "Dos")))
                .build();
        givenExisting(existing);

        cursoService.updateCurso(CURSO_ID, request(List.of(faq(" ¿Nueva? ", " Sí "), faq("   ", ""))));

        assertThat(existing.getPreguntasFrecuentes()).containsExactly(faq("¿Nueva?", "Sí"));
        verify(cursoRepository).save(existing);
    }

    @Test
    void update_clearsTheStoredListWhenAnEmptyListIsSent() {
        Curso existing = cursoBase().preguntasFrecuentes(List.of(faq("¿Vieja?", "Uno"))).build();
        givenExisting(existing);

        cursoService.updateCurso(CURSO_ID, request(List.of()));

        assertThat(existing.getPreguntasFrecuentes()).isNotNull().isEmpty();
    }

    @Test
    void update_keepsTheStoredListWhenTheRequestOmitsIt() {
        // An admin tab still running a bundle from before the field existed sends no preguntasFrecuentes at all:
        // saving there must not wipe the questions. The current admin always sends the list, [] to clear it
        Curso existing = cursoBase().preguntasFrecuentes(List.of(faq("¿Vieja?", "Uno"))).build();
        givenExisting(existing);

        cursoService.updateCurso(CURSO_ID, request(null));

        assertThat(existing.getPreguntasFrecuentes()).containsExactly(faq("¿Vieja?", "Uno"));
    }

    @Test
    void update_responseExposesTheNewQuestions() {
        givenExisting(cursoBase().preguntasFrecuentes(List.of(faq("¿Vieja?", "Uno"))).build());

        CursoResponse response = cursoService.updateCurso(CURSO_ID, request(List.of(faq(" ¿Nueva? ", " Sí "))));

        assertThat(response.getPreguntasFrecuentes()).containsExactly(faq("¿Nueva?", "Sí"));
    }

    // ---------------------------------------------------------------- read mapping

    @Test
    void getCursoBySlug_exposesTheQuestionsInOrder() {
        when(cursoRepository.findBySlug("agendas-en-powerpoint")).thenReturn(Optional.of(
                cursoBase().preguntasFrecuentes(List.of(faq("¿Uno?", "A"), faq("¿Dos?", "B"))).build()));

        assertThat(cursoService.getCursoBySlug("agendas-en-powerpoint").getPreguntasFrecuentes())
                .containsExactly(faq("¿Uno?", "A"), faq("¿Dos?", "B"));
    }

    @Test
    void getCursoBySlug_exposesAnEmptyListWhenTheCourseHasNoQuestions() {
        when(cursoRepository.findBySlug("agendas-en-powerpoint")).thenReturn(Optional.of(cursoBase().build()));

        assertThat(cursoService.getCursoBySlug("agendas-en-powerpoint").getPreguntasFrecuentes())
                .isNotNull().isEmpty();
    }

    @Test
    void getCursoBySlug_exposesAnEmptyListWhenTheEntityHoldsNull() {
        when(cursoRepository.findBySlug("agendas-en-powerpoint")).thenReturn(Optional.of(
                cursoBase().preguntasFrecuentes(null).build()));

        assertThat(cursoService.getCursoBySlug("agendas-en-powerpoint").getPreguntasFrecuentes())
                .isNotNull().isEmpty();
    }
}
