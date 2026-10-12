package com.bspapeleria.backend.repository;

import com.bspapeleria.backend.entity.Cupon;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.hibernate.grammars.hql.HqlLexer;
import org.hibernate.grammars.hql.HqlParser;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.parser.PartTree;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The application cannot be started in this project's tests (its datasource is the production database), and a
 * typo in a repository declaration would only show up as a failed startup on deploy. These checks catch the two
 * kinds of declaration we add by hand, with no Spring context and no database.
 */
class CuponRepositoryQueriesTest {

    @Test
    void derivedQueryNames_resolveAgainstTheCuponEntity() {
        assertThatCode(() -> new PartTree("existsByCodigoIgnoreCaseAndIdNot", Cupon.class)).doesNotThrowAnyException();
        assertThatCode(() -> new PartTree("existsByCodigoIgnoreCase", Cupon.class)).doesNotThrowAnyException();
        assertThatCode(() -> new PartTree("findByCodigoIgnoreCase", Cupon.class)).doesNotThrowAnyException();
    }

    @Test
    void atomicIncrement_isAModifyingQueryWithValidHqlThatOnlyReferencesRealAttributes() throws Exception {
        Method method = CuponRepository.class.getMethod("incrementarUsosSiDisponible", Long.class);

        assertThat(method.isAnnotationPresent(Modifying.class)).isTrue();
        assertThat(method.getReturnType()).isEqualTo(int.class);

        String hql = method.getAnnotation(Query.class).value();
        assertThatCode(() -> parseStrictly(hql)).doesNotThrowAnyException();
        for (String attribute : new String[] {"id", "usosCount", "maxUsos"}) {
            assertThat(Cupon.class.getDeclaredField(attribute)).isNotNull();
            assertThat(hql).contains("c." + attribute);
        }
    }

    @Test
    void theHqlCheckItselfRejectsBrokenStatements() {
        assertThatThrownBy(() -> parseStrictly("update Cupon c set where c.id = :id"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> parseStrictly("update Cupon c set c.usosCount = c.usosCount + 1 wher c.id = :id"))
                .isInstanceOf(IllegalStateException.class);
    }

    private static void parseStrictly(String hql) {
        BaseErrorListener failFast = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                    int charPositionInLine, String msg, RecognitionException e) {
                throw new IllegalStateException("HQL syntax error at " + line + ":" + charPositionInLine + " " + msg);
            }
        };
        HqlLexer lexer = new HqlLexer(CharStreams.fromString(hql));
        lexer.removeErrorListeners();
        lexer.addErrorListener(failFast);
        HqlParser parser = new HqlParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(failFast);
        parser.statement();
    }
}
