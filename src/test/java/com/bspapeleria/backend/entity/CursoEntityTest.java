package com.bspapeleria.backend.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CursoEntityTest {

    @ParameterizedTest(name = "{0} keeps its explicit slug although the title is \"{1}\"")
    @CsvSource({
            "agendas-en-powerpoint,El Mundo de las Agendas",
            "etiquetas-escolares-en-powerpoint,Etiquetas Escolares",
            "candy-bar-powerpoint,La Magia del Candy Bar"
    })
    void prePersist_keepsAnExplicitSlug_evenWhenTheTitleWouldDeriveAnotherOne(String slug, String titulo) {
        Curso explicit = Curso.builder().slug(slug).titulo(titulo).precio(1.0).build();
        Curso withoutSlug = Curso.builder().titulo(titulo).precio(1.0).build();

        explicit.onCreate();
        withoutSlug.onCreate();

        assertThat(explicit.getSlug()).isEqualTo(slug);
        // The trap this guards against: without an explicit slug the new title would derive a different one,
        // and a seeder looking it up would insert a duplicate course.
        assertThat(withoutSlug.getSlug()).isNotEqualTo(slug);
    }

    @ParameterizedTest
    @CsvSource(value = {"NIL", "''"}, nullValues = "NIL")
    void prePersist_derivesTheSlugFromTheTitleOnlyWhenNoneWasGiven(String blankSlug) {
        Curso curso = Curso.builder().slug(blankSlug).titulo("La Magia del Candy Bar").precio(1.0).build();

        curso.onCreate();

        assertThat(curso.getSlug()).isEqualTo("la-magia-del-candy-bar");
    }
}
