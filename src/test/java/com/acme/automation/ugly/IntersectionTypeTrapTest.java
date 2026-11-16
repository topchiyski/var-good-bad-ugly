package com.acme.automation.ugly;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mixed arguments to a generic factory infer an intersection type that {@code var} keeps.
 */
class IntersectionTypeTrapTest {

    /**
     * Trap: a list copied from {@code List.of("hello", 1, 2.0)} rejects a {@link Map} or
     * {@link Object} at compile time.
     */
    @Test
    void heterogeneousList_InferredIntersectionType() {
        // Without a left-hand target type, javac computes the Least Upper Bound (LUB)
        // of String, Integer, and Double. On JDK 12+ that is:
        // List<Serializable & Comparable<? extends ...> & Constable & ConstantDesc>
        var inferredPayloads = List.of("hello", 1, 2.0);

        // The proof is that these assignments compile without casts: each works only because
        // the static element type is that intersection. If var had inferred Object, every
        // line would need a cast. (A runtime instanceof check could not show this: it only
        // sees that the element is a String.)
        var firstElement = inferredPayloads.get(0);
        Serializable asSerializable = firstElement;
        Comparable<?> asComparable = firstElement;
        Constable asConstable = firstElement;
        ConstantDesc asConstantDesc = firstElement;

        assertThat(List.of(asSerializable, asComparable, asConstable, asConstantDesc))
                .containsOnly("hello");

        // JUNIOR TRAP: Wrapping that inferred list in an ArrayList prevents adding Map or Object later!
        // CompileTimeTrapTest compiles both commented lines below and asserts they are rejected.
        var fuzzInputs = new ArrayList<>(List.of("hello", 1, 2.0));
        // fuzzInputs.add(Map.of("malformed", true)); // <-- COMPILE ERROR: Map is not Comparable!
        // fuzzInputs.add(new Object());              // <-- COMPILE ERROR!
        assertThat(fuzzInputs).hasSize(3);
    }

    /**
     * Fix: an explicit {@code <Object>} type witness makes the list accept any element.
     */
    @Test
    void heterogeneousFuzzData_SeniorPattern_ExplicitTypeWitness() {
        // SENIOR PATTERN: Provide an explicit <Object> type witness on the right-hand factory
        // so 'var' infers ArrayList<Object>.
        var safeFuzzInputs = new ArrayList<>(List.<Object>of("hello", 1, 2.0));

        safeFuzzInputs.add(Map.of("malformed", true));
        safeFuzzInputs.add(new Object());

        assertThat(safeFuzzInputs).hasSize(5);
    }
}
