package dev.simplified.util;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Coverage of where {@link SystemUtil} finds the directories and variables it answers, held apart
 * from this machine's own environment.
 */
@DisplayName("SystemUtil reads its environment from where the application runs")
class SystemUtilTest {

    @Test
    @DisplayName("getCurrentDirectory is the absolute process working directory")
    void currentDirectoryIsTheWorkingDirectory() {
        assertThat(SystemUtil.getCurrentDirectory(), is(new File(System.getProperty("user.dir")).getAbsoluteFile()));
    }

    @Test
    @DisplayName("the .env file in every directory contributes its variables")
    void everyDirectoryContributes(@TempDir Path first, @TempDir Path second) throws IOException {
        writeEnv(first, "FIRST_ONLY=first");
        writeEnv(second, "SECOND_ONLY=second");

        Map<String, String> variables = SystemUtil.loadEnvironmentVariables(List.of(first.toFile(), second.toFile()), Map.of());

        assertThat(variables, hasEntry("FIRST_ONLY", "first"));
        assertThat(variables, hasEntry("SECOND_ONLY", "second"));
    }

    @Test
    @DisplayName("a later directory's .env replaces an earlier one's, and the environment replaces both")
    void laterSourcesWin(@TempDir Path first, @TempDir Path second) throws IOException {
        writeEnv(first, "FILES=first", "ALL=first");
        writeEnv(second, "FILES=second", "ALL=second");

        Map<String, String> variables = SystemUtil.loadEnvironmentVariables(List.of(first.toFile(), second.toFile()), Map.of("ALL", "environment"));

        assertThat(variables, hasEntry("FILES", "second"));
        assertThat(variables, hasEntry("ALL", "environment"));
    }

    @Test
    @DisplayName("a directory without a .env file, or one that does not exist, adds nothing")
    void missingFilesAddNothing(@TempDir Path empty) {
        Map<String, String> variables = SystemUtil.loadEnvironmentVariables(
            List.of(empty.toFile(), empty.resolve("absent").toFile()),
            Map.of("ONLY", "environment")
        );

        assertThat(variables, is(Map.of("ONLY", "environment")));
    }

    @Test
    @DisplayName("no class-path resource contributes, even under a loader that answers ../.env")
    void noResourceContributes() {
        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        thread.setContextClassLoader(new ClassLoader(original) {
            @Override
            public InputStream getResourceAsStream(String name) {
                return new ByteArrayInputStream("RESOURCE=resource".getBytes(StandardCharsets.UTF_8));
            }
        });

        try {
            assertThat(SystemUtil.loadEnvironmentVariables(List.of(), Map.of()), is(anEmptyMap()));
        } finally {
            thread.setContextClassLoader(original);
        }
    }

    @Test
    @DisplayName("a jar started with -jar is the entry point, its directory answered even with spaces in the path")
    void jarLaunchAnswersTheJarsDirectory(@TempDir Path root) throws IOException {
        Path deployment = Files.createDirectories(root.resolve("deploy dir"));
        String jar = Files.createFile(deployment.resolve("app.jar")).toString();

        assertThat(SystemUtil.getEntryPointDirectory(jar + " --flag value", jar), is(deployment.toFile()));
        assertThat(SystemUtil.getEntryPointDirectory(jar, jar), is(deployment.toFile()));
    }

    @Test
    @DisplayName("a main class is the entry point, the directory above its code source answered")
    void classLaunchAnswersTheMainClassDirectory() throws URISyntaxException {
        File expected = new File(MatcherAssert.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();

        assertThat(SystemUtil.getEntryPointDirectory(MatcherAssert.class.getName() + " argument", "unrelated"), is(expected));
    }

    @Test
    @DisplayName("no command, an unknown main class or a module launch has no entry point directory")
    void unresolvableCommandsAnswerNull() {
        assertThat(SystemUtil.getEntryPointDirectory(null, null), is(nullValue()));
        assertThat(SystemUtil.getEntryPointDirectory("", "unrelated"), is(nullValue()));
        assertThat(SystemUtil.getEntryPointDirectory("dev.simplified.util.NoSuchMain argument", "unrelated"), is(nullValue()));
        assertThat(SystemUtil.getEntryPointDirectory("some.module/some.module.Main", "unrelated"), is(nullValue()));
    }

    /**
     * Writes the given lines as the {@code .env} file in the given directory.
     *
     * @param directory the directory to write into
     * @param lines the lines of the file
     * @throws IOException if the file cannot be written
     */
    private static void writeEnv(Path directory, String... lines) throws IOException {
        Files.write(directory.resolve(".env"), List.of(lines));
    }

}
