package dev.simplified.util;

import dev.simplified.annotations.Cleanup;
import dev.simplified.annotations.Getter;
import dev.simplified.annotations.SilentThrows;
import dev.simplified.annotations.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Helpers for {@code java.lang.System} providing cached system properties, environment variable
 * access, and classpath resource utilities.
 *
 * <p>If a system property cannot be read due to security restrictions, the corresponding field
 * in this class will be set to {@code null} and a message will be written to {@code System.err}.
 *
 * <p>Property values are initialized when the class is loaded. If
 * {@link System#setProperty(String, String)} or {@link System#setProperties(java.util.Properties)}
 * is called after this class is loaded, the cached values will be out of sync.
 */
@UtilityClass
public final class SystemUtil {

    /**
     * The {@code os.name} system property - operating system short name.
     */
    private static final String OS_NAME_KEY = "os.name";

    /**
     * The {@code user.home} system property - user home directory.
     */
    private static final String USER_HOME_KEY = "user.home";

    /**
     * The {@code user.dir} system property - user directory.
     */
    private static final String USER_DIR_KEY = "user.dir";

    /**
     * The {@code java.io.tmpdir} system property - Java IO temporary directory.
     */
    private static final String JAVA_IO_TMPDIR_KEY = "java.io.tmpdir";

    /**
     * The {@code java.home} system property - Java home directory.
     */
    private static final String JAVA_HOME_KEY = "java.home";

    /**
     * The {@code file.encoding} system property, such as {@code Cp1252}.
     */
    public static final String FILE_ENCODING = getSystemProperty("file.encoding");

    /**
     * The {@code file.separator} system property - file separator ({@code "/"} on UNIX).
     */
    public static final String FILE_SEPARATOR = getSystemProperty("file.separator");

    /**
     * The {@code java.awt.headless} system property - {@code "true"} or {@code "false"}.
     */
    private static final String JAVA_AWT_HEADLESS = getSystemProperty("java.awt.headless");

    /**
     * The {@code java.home} system property - Java installation directory.
     */
    public static final String JAVA_HOME = getSystemProperty(JAVA_HOME_KEY);

    /**
     * The {@code java.io.tmpdir} system property - default temp file path.
     */
    public static final String JAVA_IO_TMPDIR = getSystemProperty(JAVA_IO_TMPDIR_KEY);

    /**
     * The {@code java.class.path} system property - Java class path.
     */
    public static final String JAVA_CLASS_PATH = getSystemProperty("java.class.path");

    /**
     * The {@code java.library.path} system property - list of paths to search when loading libraries.
     */
    public static final String JAVA_LIBRARY_PATH = getSystemProperty("java.library.path");

    /**
     * The {@code java.runtime.name} system property - Java Runtime Environment name.
     */
    public static final String JAVA_RUNTIME_NAME = getSystemProperty("java.runtime.name");

    /**
     * The {@code java.runtime.version} system property - Java Runtime Environment version.
     */
    public static final String JAVA_RUNTIME_VERSION = getSystemProperty("java.runtime.version");

    /**
     * The {@code java.specification.name} system property - Java Runtime Environment specification name.
     */
    public static final String JAVA_SPECIFICATION_NAME = getSystemProperty("java.specification.name");

    /**
     * The {@code java.specification.vendor} system property - Java Runtime Environment specification vendor.
     */
    public static final String JAVA_SPECIFICATION_VENDOR = getSystemProperty("java.specification.vendor");

    /**
     * The {@code java.specification.version} system property - Java Runtime Environment specification version.
     */
    public static final String JAVA_SPECIFICATION_VERSION = getSystemProperty("java.specification.version");

    /**
     * The {@code java.util.prefs.PreferencesFactory} system property - a class name.
     */
    public static final String JAVA_UTIL_PREFS_PREFERENCES_FACTORY = getSystemProperty("java.util.prefs.PreferencesFactory");

    /**
     * The {@code java.vendor} system property - Java vendor-specific string.
     */
    public static final String JAVA_VENDOR = getSystemProperty("java.vendor");

    /**
     * The {@code java.vendor.url} system property - Java vendor URL.
     */
    public static final String JAVA_VENDOR_URL = getSystemProperty("java.vendor.url");

    /**
     * The {@code java.version} system property - Java version number.
     */
    public static final String JAVA_VERSION = getSystemProperty("java.version");

    /**
     * The {@code java.vm.info} system property - Java Virtual Machine implementation info.
     */
    public static final String JAVA_VM_INFO = getSystemProperty("java.vm.info");

    /**
     * The {@code java.vm.name} system property - Java Virtual Machine implementation name.
     */
    public static final String JAVA_VM_NAME = getSystemProperty("java.vm.name");

    /**
     * The {@code java.vm.specification.name} system property - Java Virtual Machine specification name.
     */
    public static final String JAVA_VM_SPECIFICATION_NAME = getSystemProperty("java.vm.specification.name");

    /**
     * The {@code java.vm.specification.vendor} system property - Java Virtual Machine specification vendor.
     */
    public static final String JAVA_VM_SPECIFICATION_VENDOR = getSystemProperty("java.vm.specification.vendor");

    /**
     * The {@code java.vm.specification.version} system property - Java Virtual Machine specification version.
     */
    public static final String JAVA_VM_SPECIFICATION_VERSION = getSystemProperty("java.vm.specification.version");

    /**
     * The {@code java.vm.vendor} system property - Java Virtual Machine implementation vendor.
     */
    public static final String JAVA_VM_VENDOR = getSystemProperty("java.vm.vendor");

    /**
     * The {@code java.vm.version} system property - Java Virtual Machine implementation version.
     */
    public static final String JAVA_VM_VERSION = getSystemProperty("java.vm.version");

    /**
     * The {@code line.separator} system property - line separator ({@code "\n"} on UNIX).
     */
    public static final String LINE_SEPARATOR = getSystemProperty("line.separator");

    /**
     * The {@code path.separator} system property - path separator ({@code ":"} on UNIX).
     */
    public static final String PATH_SEPARATOR = getSystemProperty("path.separator");

    /**
     * The {@code user.dir} system property - user's current working directory.
     */
    public static final String USER_DIR = getSystemProperty(USER_DIR_KEY);

    /**
     * The {@code user.home} system property - user's home directory.
     */
    public static final String USER_HOME = getSystemProperty(USER_HOME_KEY);

    /**
     * The {@code user.language} system property - user's language code, such as {@code "en"}.
     */
    public static final String USER_LANGUAGE = getSystemProperty("user.language");

    /**
     * The {@code user.name} system property - user's account name.
     */
    public static final String USER_NAME = getSystemProperty("user.name");

    /**
     * The {@code user.timezone} system property, for example {@code "America/Los_Angeles"}.
     */
    public static final String USER_TIMEZONE = getSystemProperty("user.timezone");

    /**
     * Unmodifiable map of every variable read from two sources, the second laid over the first where
     * a key matches exactly: the {@code .env} file in {@link #getCurrentDirectory()}, the process
     * working directory, then the OS environment from {@link System#getenv()}.
     *
     * <p>No {@code .env} is read from the class path or from beside any jar, so a deployment keeps
     * its file in the directory it starts the application from. Under a launcher script such as a
     * Gradle distribution's that is the directory the script is run from, not its {@code lib/}
     * directory, and under Gradle's {@code test} task it is the directory of the project under test.
     *
     * <p>A missing or unreadable file adds nothing. The map is built once when the class
     * initializes, so a file written later is not read.
     */
    @Getter
    private static @NotNull Map<String, String> env = Collections.unmodifiableMap(loadEnvironmentVariables(USER_DIR != null ? getCurrentDirectory() : null, System.getenv()));

    /**
     * Returns the Java home directory as a {@link File}.
     *
     * @return the Java home directory
     * @throws SecurityException if a security manager prevents access to the system property
     * @see System#getProperty(String)
     */
    public static File getJavaHome() {
        return new File(System.getProperty(JAVA_HOME_KEY));
    }

    /**
     * Returns the Java IO temporary directory as a {@link File}.
     *
     * @return the temporary directory
     * @throws SecurityException if a security manager prevents access to the system property
     * @see System#getProperty(String)
     */
    public static File getJavaIoTmpDir() {
        return new File(System.getProperty(JAVA_IO_TMPDIR_KEY));
    }

    /**
     * Returns the user's current working directory as a {@link File}.
     *
     * @return the user directory
     * @throws SecurityException if a security manager prevents access to the system property
     * @see System#getProperty(String)
     */
    public static File getUserDir() {
        return new File(System.getProperty(USER_DIR_KEY));
    }

    /**
     * Returns the user's home directory as a {@link File}.
     *
     * @return the user home directory
     * @throws SecurityException if a security manager prevents access to the system property
     * @see System#getProperty(String)
     */
    public static File getUserHome() {
        return new File(System.getProperty(USER_HOME_KEY));
    }

    /**
     * Checks whether the {@code java.awt.headless} system property is set to {@code "true"}.
     *
     * @return {@code true} if the JVM is running in headless mode, {@code false} otherwise
     * @see #JAVA_AWT_HEADLESS
     */
    public static boolean isJavaAwtHeadless() {
        return JAVA_AWT_HEADLESS != null && JAVA_AWT_HEADLESS.equals(Boolean.TRUE.toString());
    }

    /**
     * Determines the preferred local {@link InetAddress} by opening a UDP socket to a well-known
     * external address and reading back the local endpoint.
     *
     * @return the preferred outbound network address
     */
    @SilentThrows
    public static @NotNull InetAddress getPreferredAddress() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress();
        }
    }

    /**
     * Retrieves a system property by name, defaulting to {@code null} if the property cannot be read.
     *
     * <p>If a {@link SecurityException} is caught, the return value is {@code null} and a message is
     * written to {@code System.err}.
     *
     * @param property the system property name
     * @return the system property value, or {@code null} if a security problem occurs
     */
    private static String getSystemProperty(String property) {
        try {
            return System.getProperty(property);
        } catch (SecurityException ex) {
            // we are not allowed to look at this property
            System.err.println("Caught a SecurityException reading the system property '" + property
                                   + "'; the SystemUtils property value will default to null.");
            return null;
        }
    }

    /**
     * Parses the lines of the given stream that contain {@code =} into a map, splitting each at its
     * first {@code =} into a key and a value.
     *
     * <p>Both halves are kept verbatim - nothing is trimmed, unquoted or treated as a comment - and
     * a line without {@code =} is skipped. A later line replaces an earlier one with the same key.
     * The stream is decoded with the default charset and is not closed here.
     *
     * @param inputStream the stream to read, or {@code null} for none
     * @return a mutable map of the parsed variables, empty when {@code inputStream} is {@code null}
     */
    private static @NotNull Map<String, String> readEnvironmentFile(@Nullable InputStream inputStream) {
        Map<String, String> variables = new HashMap<>();

        if (inputStream != null) {
            Scanner scanner = new Scanner(inputStream);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.contains("=")) {
                    String[] pair = line.split("=", 2);
                    variables.put(pair[0], pair.length == 2 ? pair[1] : "");
                }
            }
        }

        return variables;
    }

    /**
     * Returns the process working directory - the directory the application was started from, which
     * the {@code user.dir} system property names - as an absolute file.
     *
     * @return the absolute working directory
     * @throws SecurityException if a security manager prevents access to the system property
     * @see #getUserDir()
     */
    public static @NotNull File getCurrentDirectory() {
        return getUserDir().getAbsoluteFile();
    }

    /**
     * Builds an environment map from the {@code .env} file in the given directory with the given
     * environment laid over it, an entry of the file replaced only where a key matches exactly, case
     * included.
     *
     * <p>A missing or unreadable file, or no directory at all, adds nothing.
     *
     * @param directory the directory whose {@code .env} file is read, or {@code null} for none
     * @param environment the variables laid over the file
     * @return a mutable map of every variable read
     */
    static @NotNull Map<String, String> loadEnvironmentVariables(@Nullable File directory, @NotNull Map<String, String> environment) {
        Map<String, String> variables = new HashMap<>();

        if (directory != null) {
            try (InputStream file = new FileInputStream(new File(directory, ".env"))) {
                variables.putAll(readEnvironmentFile(file));
            } catch (Exception ignore) { }
        }

        variables.putAll(environment);
        return variables;
    }

    /**
     * Looks up an environment variable in the map {@code getEnv()} answers, preferring the name as
     * spelled and falling back to a match that ignores case.
     *
     * <p>A key equal to the name, case included, always answers. Failing that, of the keys equal to
     * it under {@link String#equalsIgnoreCase(String)}, the one first by
     * {@link String#compareTo(String)} answers - for an ASCII name, the spelling with the upper-case
     * letter where the spellings first differ. So beside a {@code .env} entry {@code db_url} and the
     * OS variable {@code DB_URL}, {@code getEnv("db_url")} answers the entry's value and both
     * {@code getEnv("DB_URL")} and {@code getEnv("Db_Url")} answer the variable's.
     *
     * @param variableName the name of the variable, matched exactly first and then ignoring case
     * @return the value of the matching key, or empty if no key matches the name in any case
     */
    public static @NotNull Optional<String> getEnv(@NotNull String variableName) {
        return findEnv(getEnv(), variableName);
    }

    /**
     * Looks up a variable in the given map by the rule {@link #getEnv(String)} follows: the key equal
     * to the name, case included, and failing that the key first by {@link String#compareTo(String)}
     * among those equal to it ignoring case.
     *
     * @param variables the variables to search
     * @param variableName the name of the variable, matched exactly first and then ignoring case
     * @return the value of the matching key, or empty if no key matches the name in any case
     */
    static @NotNull Optional<String> findEnv(@NotNull Map<String, String> variables, @NotNull String variableName) {
        String exact = variables.get(variableName);

        if (exact != null)
            return Optional.of(exact);

        return variables.entrySet()
            .stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(variableName))
            .min(Map.Entry.comparingByKey())
            .map(Map.Entry::getValue);
    }

    /**
     * Opens a resource as an {@link InputStream} through the current thread's context class
     * loader, or through the class loader of {@code SystemUtil} when the thread has none.
     *
     * <p>A leading {@code resources/} is removed from the path and then a leading {@code /}, so
     * {@code resources/a.txt} and {@code /a.txt} both open {@code a.txt}; any other path reaches the
     * class loader unchanged.
     *
     * @param resourcePath the classpath-relative resource path
     * @return an input stream for the resource, or {@code null} if not found
     */
    public static @Nullable InputStream getResource(@NotNull String resourcePath) {
        return ClassUtil.getClassLoader(SystemUtil.class).getResourceAsStream(
            RegexUtil.replaceFirst(resourcePath, "^resources/", "").replaceFirst("^/", "")
        );
    }

    /**
     * Lists the file names available under a classpath resource directory by reading lines
     * from the directory's input stream.
     *
     * @param resourcePath the classpath-relative directory path
     * @return an unmodifiable list of file names found under the resource path, or an empty list on
     *         failure
     */
    public static @NotNull List<String> getResourceFiles(@NotNull String resourcePath) {
        List<String> fileNames = new ArrayList<>();

        try {
            resourcePath = RegexUtil.replaceFirst(resourcePath, "^resources/", "");
            @Cleanup InputStream inputStream = getResource(resourcePath);
            if (inputStream != null) {
                @Cleanup BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String resource;

                while ((resource = bufferedReader.readLine()) != null)
                    fileNames.add(resource);
            }
        } catch (IOException ignore) { }

        return Collections.unmodifiableList(fileNames);
    }

    /**
     * Reads all bytes from a classpath resource.
     *
     * @param resourcePath the classpath-relative resource path
     * @return the full byte contents of the resource, or an empty array if not found
     */
    @SilentThrows
    public static byte[] readResource(@NotNull String resourcePath) {
        @Cleanup InputStream inputStream = getResource(resourcePath);
        return inputStream != null ? inputStream.readAllBytes() : new byte[0];
    }

    /**
     * Saves a classpath resource to the given output directory, optionally replacing an
     * existing file.
     *
     * @param outputDir the target directory
     * @param resourcePath the classpath-relative resource path
     * @param replace {@code true} to overwrite an existing file, {@code false} to throw if it exists
     * @throws IllegalStateException if the file already exists and {@code replace} is {@code false},
     *                               or if parent directories cannot be created
     */
    public static void saveResource(@NotNull File outputDir, @NotNull String resourcePath, boolean replace) {
        saveResource(outputDir, resourcePath, "", replace);
    }

    /**
     * Saves a classpath resource to a child subdirectory of the given output directory,
     * optionally replacing an existing file.
     *
     * @param outputDir the base output directory
     * @param resourcePath the classpath-relative resource path
     * @param child an optional subdirectory name within {@code outputDir}
     * @param replace {@code true} to overwrite an existing file, {@code false} to throw if it exists
     * @throws IllegalStateException if the file already exists and {@code replace} is {@code false},
     *                               or if parent directories cannot be created
     */
    @SuppressWarnings("all")
    public static void saveResource(@NotNull File outputDir, @NotNull String resourcePath, @Nullable String child, boolean replace) {
        File directory = outputDir;

        if (StringUtil.isNotEmpty(child))
            directory = new File(directory, child);

        File output = new File(directory, resourcePath);

        try (InputStream inputStream = getResource(resourcePath)) {
            if (!directory.exists()) {
                if (!directory.mkdirs())
                    throw new IllegalStateException(String.format("Unable to create parent directories for '%s'", output));
            }

            if (replace)
                output.delete();
            else if (output.exists())
                throw new IllegalStateException(String.format("Output file '%s' already exists", output));

            try (FileOutputStream outputStream = new FileOutputStream(output)) {
                byte[] buffer = new byte[1024];
                int length;

                while ((length = inputStream.read(buffer)) > 0)
                    outputStream.write(buffer, 0, length);
            }
        } catch (Exception exception) {
            throw new IllegalStateException(String.format("Unable to save resource '%s' to '%s'", resourcePath, output), exception);
        }
    }

}
