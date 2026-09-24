package dev.simplified.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Coverage of where {@link SystemUtil} finds the directories and variables it answers, independent
 * of what this machine's own environment holds.
 */
@DisplayName("SystemUtil reads its environment from where the application runs")
class SystemUtilTest {

    @Test
    @DisplayName("getCurrentDirectory is the absolute process working directory")
    void currentDirectoryIsTheWorkingDirectory() {
        assertThat(SystemUtil.getCurrentDirectory(), is(new File(System.getProperty("user.dir")).getAbsoluteFile()));
    }

    @Test
    @DisplayName("a .env beside the location utils was loaded from is not read, even when the main class was loaded from there too")
    void libraryLocationIsNotRead(@TempDir Path workingDirectory) throws Exception {
        Path libraryEnv = Path.of(SystemUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI()).resolveSibling(".env");
        assumeFalse(Files.exists(libraryEnv), "a .env already sits beside utils' class-path entry");
        writeEnv(workingDirectory, "SYSTEM_UTIL_TEST_WORKING_DIRECTORY=working");
        Files.write(libraryEnv, List.of("SYSTEM_UTIL_TEST_LIBRARY_LOCATION=library"));

        try {
            Map<String, String> variables = initializeFresh(Map.of(
                "user.dir", workingDirectory.toString(),
                "sun.java.command", SystemUtil.class.getName() + " argument"
            ));

            assertThat(variables.get("SYSTEM_UTIL_TEST_WORKING_DIRECTORY"), is("working"));
            assertThat(variables.get("SYSTEM_UTIL_TEST_LIBRARY_LOCATION"), is(nullValue()));
        } finally {
            Files.deleteIfExists(libraryEnv);
        }
    }

    @Test
    @DisplayName("the .env file in the given directory contributes its variables")
    void directoryContributes(@TempDir Path directory) throws IOException {
        writeEnv(directory, "FILE_ONLY=file");

        assertThat(SystemUtil.loadEnvironmentVariables(directory.toFile(), Map.of()), is(Map.of("FILE_ONLY", "file")));
    }

    @Test
    @DisplayName("the environment replaces a .env entry with the same key")
    void environmentWins(@TempDir Path directory) throws IOException {
        writeEnv(directory, "FILE_ONLY=file", "BOTH=file");

        Map<String, String> variables = SystemUtil.loadEnvironmentVariables(directory.toFile(), Map.of("BOTH", "environment"));

        assertThat(variables, is(Map.of("FILE_ONLY", "file", "BOTH", "environment")));
    }

    @Test
    @DisplayName("a directory without a .env file, one that does not exist, or no directory adds nothing")
    void missingFilesAddNothing(@TempDir Path empty) {
        Map<String, String> environment = Map.of("ONLY", "environment");

        assertThat(SystemUtil.loadEnvironmentVariables(empty.toFile(), environment), is(environment));
        assertThat(SystemUtil.loadEnvironmentVariables(empty.resolve("absent").toFile(), environment), is(environment));
        assertThat(SystemUtil.loadEnvironmentVariables(null, environment), is(environment));
    }

    @Test
    @DisplayName("the initialized map is the working directory's .env under the OS environment, and no class-path resource contributes")
    void initializedMapReadsOnlyItsSources(@TempDir Path workingDirectory) throws Exception {
        String shadowed = System.getenv()
            .keySet()
            .stream()
            .filter(key -> key.matches("[A-Za-z_][A-Za-z0-9_]*"))
            .min(Comparator.naturalOrder())
            .orElse(null);
        assumeTrue(shadowed != null, "the environment holds no variable a .env entry can share a key with");
        writeEnv(workingDirectory, "SYSTEM_UTIL_TEST_WORKING_DIRECTORY=working", shadowed + "=file");

        Map<String, String> expected = new HashMap<>(Map.of("SYSTEM_UTIL_TEST_WORKING_DIRECTORY", "working", shadowed, "file"));
        expected.putAll(System.getenv());

        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        thread.setContextClassLoader(new ClassLoader(original) {
            @Override
            public InputStream getResourceAsStream(String name) {
                return new ByteArrayInputStream("SYSTEM_UTIL_TEST_RESOURCE=resource".getBytes(StandardCharsets.UTF_8));
            }
        });

        try {
            assertThat(differingKeys(initializeFresh(Map.of("user.dir", workingDirectory.toString())), expected), is(empty()));
        } finally {
            thread.setContextClassLoader(original);
        }
    }

    @Test
    @DisplayName("a name spelled exactly as a key answers that key, whatever order the map iterates in")
    void exactCaseWins() {
        for (Map<String, String> variables : inBothOrders("db_url", "lower", "DB_URL", "upper")) {
            assertThat(SystemUtil.findEnv(variables, "DB_URL"), is(Optional.of("upper")));
            assertThat(SystemUtil.findEnv(variables, "db_url"), is(Optional.of("lower")));
        }
    }

    @Test
    @DisplayName("a name matching keys only ignoring case answers the key first by String.compareTo, whatever order the map iterates in")
    void caseInsensitiveFallbackIsFixed() {
        for (Map<String, String> variables : inBothOrders("db_url", "lower", "DB_URL", "upper"))
            assertThat(SystemUtil.findEnv(variables, "Db_Url"), is(Optional.of("upper")));

        for (Map<String, String> variables : inBothOrders("db_url", "lower", "dB_URL", "mixed", "Db_url", "capital"))
            assertThat(SystemUtil.findEnv(variables, "DB_URL"), is(Optional.of("capital")));
    }

    @Test
    @DisplayName("a name no key matches, in any case, answers empty")
    void unmatchedNameIsEmpty() {
        assertThat(SystemUtil.findEnv(Map.of("DB_URL", "upper"), "DB_HOST"), is(Optional.empty()));
    }

    @Test
    @DisplayName("a .env key differing only in case from an environment variable is kept, and each spelling finds its own value")
    void caseVariantsAcrossSourcesStayApart(@TempDir Path directory) throws IOException {
        writeEnv(directory, "db_url=file");

        Map<String, String> variables = SystemUtil.loadEnvironmentVariables(directory.toFile(), Map.of("DB_URL", "environment"));

        assertThat(SystemUtil.findEnv(variables, "db_url"), is(Optional.of("file")));
        assertThat(SystemUtil.findEnv(variables, "DB_URL"), is(Optional.of("environment")));
        assertThat(SystemUtil.findEnv(variables, "Db_Url"), is(Optional.of("environment")));
    }

    /**
     * Builds the same variables twice, once inserted in the given order and once in reverse, so a
     * lookup that leaned on iteration order answers differently for one of them.
     *
     * @param keysAndValues alternating keys and values
     * @return the forward and the reversed map
     */
    private static List<Map<String, String>> inBothOrders(String... keysAndValues) {
        Map<String, String> forward = new LinkedHashMap<>();
        Map<String, String> reversed = new LinkedHashMap<>();

        for (int i = 0; i < keysAndValues.length; i += 2)
            forward.put(keysAndValues[i], keysAndValues[i + 1]);

        for (int i = keysAndValues.length - 2; i >= 0; i -= 2)
            reversed.put(keysAndValues[i], keysAndValues[i + 1]);

        return List.of(forward, reversed);
    }

    /**
     * Lists the keys whose values differ between two maps, a key held by one map only included, so
     * a failed comparison of environment maps names variables without printing their values.
     *
     * @param actual the map under test
     * @param expected the map it should equal
     * @return the differing keys in natural order
     */
    private static Set<String> differingKeys(Map<String, String> actual, Map<String, String> expected) {
        Set<String> keys = new TreeSet<>(actual.keySet());
        keys.addAll(expected.keySet());
        keys.removeIf(key -> Objects.equals(actual.get(key), expected.get(key)));
        return keys;
    }

    /**
     * Initializes a copy of {@link SystemUtil} defined by a class loader of its own, so its static
     * initializer builds the environment map again while the given system properties are set.
     *
     * <p>Every other class resolves through the loader of the {@code SystemUtil} under test, and
     * the properties are put back once the copy has initialized.
     *
     * @param properties the system properties to set while the copy initializes
     * @return the environment map the copy built
     * @throws Exception if the copy cannot be defined or its map cannot be read
     */
    @SuppressWarnings("unchecked")
    private static Map<String, String> initializeFresh(Map<String, String> properties) throws Exception {
        Map<String, String> saved = new HashMap<>();
        properties.keySet().forEach(key -> saved.put(key, System.getProperty(key)));
        URL classes = SystemUtil.class.getProtectionDomain().getCodeSource().getLocation();

        try (URLClassLoader loader = new URLClassLoader(new URL[] { classes }, SystemUtil.class.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!name.equals(SystemUtil.class.getName()))
                    return super.loadClass(name, resolve);

                synchronized (getClassLoadingLock(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    return loaded != null ? loaded : findClass(name);
                }
            }
        }) {
            properties.forEach(System::setProperty);
            return (Map<String, String>) Class.forName(SystemUtil.class.getName(), true, loader).getMethod("getEnv").invoke(null);
        } finally {
            saved.forEach((key, value) -> {
                if (value == null)
                    System.clearProperty(key);
                else
                    System.setProperty(key, value);
            });
        }
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
