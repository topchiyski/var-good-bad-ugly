package com.acme.mechanics;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.spi.ToolProvider;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs javap on the compiled {@link BytecodeParityDemo} and checks that the explicit and the
 * {@code var} method compile to the same instructions and the same local variable tables.
 */
class BytecodeParityTest {

    @Test
    void explicitAndVarMethodsCompileToIdenticalBytecode() throws Exception {
        var javapOutput = javap(BytecodeParityDemo.class);

        var explicitBody = methodBodyWithoutLineNumbers(javapOutput, "public int computeTotalExplicit();");
        var inferredBody = methodBodyWithoutLineNumbers(javapOutput, "public int computeTotalInferred();");

        // Guard: without debug info both bodies would match trivially, with no tables to compare
        assertThat(explicitBody)
                .contains("LocalVariableTable:")
                .contains("LocalVariableTypeTable:")
                .anyMatch(line -> line.contains("Ljava/util/List<Ljava/lang/Integer;>;"));

        assertThat(inferredBody).isEqualTo(explicitBody);
    }

    /**
     * Returns the verbose javap disassembly of the given class.
     */
    private static String javap(Class<?> type) throws Exception {
        var javap = ToolProvider.findFirst("javap")
                .orElseThrow(() -> new IllegalStateException("javap not found: run the tests on a JDK"));
        var classesDir = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI());
        var out = new StringWriter();
        var exitCode = javap.run(new PrintWriter(out), new PrintWriter(out),
                "-c", "-v", "-cp", classesDir.toString(), type.getName());
        assertThat(exitCode).as("javap exit code, output:%n%s", out).isZero();
        return out.toString();
    }

    /**
     * Returns the lines of one method's javap block, without its signature line and without
     * the LineNumberTable, which differs only because the methods sit on different source lines.
     */
    private static List<String> methodBodyWithoutLineNumbers(String javapOutput, String signature) {
        var lines = javapOutput.lines().toList();
        var start = lines.stream().map(String::strip).toList().indexOf(signature);
        assertThat(start).as("method %s in javap output", signature).isNotNegative();

        // A method's block is every following line indented deeper than its signature
        var body = new ArrayList<String>();
        for (var i = start + 1; i < lines.size() && lines.get(i).startsWith("    "); i++) {
            var line = lines.get(i).strip();
            if (!line.equals("LineNumberTable:") && !line.startsWith("line ")) {
                body.add(line);
            }
        }
        return body;
    }
}
