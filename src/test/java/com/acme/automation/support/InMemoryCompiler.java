package com.acme.automation.support;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;

/**
 * Compiles a single Java source snippet with the JDK's own compiler and returns the
 * error codes javac reports, so a test can prove that code does or does not compile.
 */
public final class InMemoryCompiler {

    /**
     * Not instantiable.
     */
    private InMemoryCompiler() {
    }

    /**
     * Compiles {@code source} as {@code Snippet.java} and returns the codes of all error
     * diagnostics, for example {@code compiler.err.prob.found.req}. An empty list means the
     * snippet compiled.
     */
    public static List<String> compileErrors(String source) {
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No system Java compiler: run the tests on a JDK, not a JRE");
        }
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var file = new SimpleJavaFileObject(URI.create("string:///Snippet.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        try {
            var outputDir = Files.createTempDirectory("snippet-classes");
            compiler.getTask(null, null, diagnostics,
                    List.of("-d", outputDir.toString(), "-proc:none"), null, List.of(file)).call();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return diagnostics.getDiagnostics().stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                .map(Diagnostic::getCode)
                .toList();
    }
}
