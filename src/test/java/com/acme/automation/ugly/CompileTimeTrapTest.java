package com.acme.automation.ugly;

import org.junit.jupiter.api.Test;

import static com.acme.automation.support.InMemoryCompiler.compileErrors;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The other tests can only show compile errors as commented-out lines. These tests run javac
 * on small snippets and assert on its error codes, so every "does not compile" and "compiles
 * silently" claim is checked by the build.
 */
class CompileTimeTrapTest {

    /**
     * javac's code for "incompatible types".
     */
    private static final String INCOMPATIBLE_TYPES = "compiler.err.prob.found.req";

    @Test
    void intersectionType_RejectsObject() {
        // TRAP: the list element type is the intersection of String, Integer and Double
        var errors = compileErrors("""
                import java.util.*;
                class Snippet {
                    void f() {
                        var fuzzInputs = new ArrayList<>(List.of("hello", 1, 2.0));
                        fuzzInputs.add(new Object());
                    }
                }
                """);

        assertThat(errors).containsExactly(INCOMPATIBLE_TYPES);
    }

    @Test
    void intersectionType_RejectsMap() {
        var errors = compileErrors("""
                import java.util.*;
                class Snippet {
                    void f() {
                        var fuzzInputs = new ArrayList<>(List.of("hello", 1, 2.0));
                        fuzzInputs.add(Map.of("malformed", true));
                    }
                }
                """);

        assertThat(errors).containsExactly(INCOMPATIBLE_TYPES);
    }

    @Test
    void intersectionType_ObjectWitnessCompiles() {
        // FIX: List.<Object>of makes var infer ArrayList<Object>
        var errors = compileErrors("""
                import java.util.*;
                class Snippet {
                    void f() {
                        var safeFuzzInputs = new ArrayList<>(List.<Object>of("hello", 1, 2.0));
                        safeFuzzInputs.add(new Object());
                        safeFuzzInputs.add(Map.of("malformed", true));
                    }
                }
                """);

        assertThat(errors).isEmpty();
    }

    @Test
    void diamondOperator_AcceptsAnythingSilently() {
        // TRAP: var + empty <> infers ArrayList<Object>, so mixed elements compile without a warning
        var errors = compileErrors("""
                import java.util.*;
                class Snippet {
                    void f() {
                        var activeElements = new ArrayList<>();
                        activeElements.add("#row-3");
                        activeElements.add(42);
                    }
                }
                """);

        assertThat(errors).isEmpty();
    }

    @Test
    void diamondOperator_ExplicitRhsTypeIsChecked() {
        // FIX: the type on the right-hand side brings compile-time checking back
        var errors = compileErrors("""
                import java.util.*;
                class Snippet {
                    void f() {
                        var safeElements = new ArrayList<String>();
                        safeElements.add(42);
                    }
                }
                """);

        assertThat(errors).containsExactly(INCOMPATIBLE_TYPES);
    }

    @Test
    void contextualKeyword_VarIsALegalName() {
        // var is a contextual keyword: fields, methods and locals may still be called var
        var errors = compileErrors("""
                class Snippet {
                    private int var = 1;

                    int var() {
                        var var = this.var;
                        return var;
                    }
                }
                """);

        assertThat(errors).isEmpty();
    }

    @Test
    void contextualKeyword_VarIsNotALegalTypeName() {
        // The one exception: since Java 10, var cannot name a class, interface or record
        var errors = compileErrors("class var {}");

        assertThat(errors).containsExactly("compiler.err.restricted.type.not.allowed");
    }
}
