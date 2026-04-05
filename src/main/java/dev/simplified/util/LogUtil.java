package dev.simplified.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * A logging facade for programmatic log-level changes that works across logging backends.
 *
 * <p>Detects the active logging implementation at class-load time via reflection,
 * requiring no compile-time dependency on any logging framework:</p>
 * <ul>
 *   <li><b>Log4j2 Core</b> - detects {@code LoggerContext} and delegates to
 *       {@code Configurator.setLevel} via cached reflection</li>
 *   <li><b>Logback</b> (via {@code log4j-to-slf4j} bridge) - sets levels via cached
 *       reflection on {@code ch.qos.logback.classic.Logger}</li>
 * </ul>
 *
 * <p>If neither backend is detected, level changes are silently skipped.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogUtil {

    private static final @Nullable Bridge BRIDGE;

    static {
        Bridge detected = null;

        try {
            detected = new Log4j2Bridge();
        } catch (Exception ignored) {
            try {
                detected = new LogbackBridge();
            } catch (Exception ignored2) {
                // No supported backend
            }
        }

        BRIDGE = detected;
    }

    /**
     * Sets the log level for the named logger.
     *
     * @param loggerName the fully-qualified logger name
     * @param level the log level name (e.g. "DEBUG", "INFO", "WARN", "ERROR")
     */
    public static void setLevel(@NotNull String loggerName, @NotNull String level) {
        if (BRIDGE != null)
            BRIDGE.setLevel(loggerName, level);
    }

    /**
     * Sets the root logger level.
     *
     * @param level the log level name (e.g. "DEBUG", "INFO", "WARN", "ERROR")
     */
    public static void setRootLevel(@NotNull String level) {
        if (BRIDGE != null)
            BRIDGE.setRootLevel(level);
    }

    private interface Bridge {

        void setLevel(@NotNull String loggerName, @NotNull String level);

        void setRootLevel(@NotNull String level);

    }

    /**
     * Cached Log4j2 Core reflection bridge that detects {@code LoggerContext} and delegates
     * to {@code Configurator} for level changes.
     */
    private static final class Log4j2Bridge implements Bridge {

        private final @NotNull Method getLevel;
        private final @NotNull Method setLevelMethod;
        private final @NotNull Method setRootLevelMethod;

        private Log4j2Bridge() throws Exception {
            Class<?> logManager = Class.forName("org.apache.logging.log4j.LogManager");
            Class<?> loggerContextClass = Class.forName("org.apache.logging.log4j.core.LoggerContext");
            Method getContext = logManager.getMethod("getContext", boolean.class);
            Object context = getContext.invoke(null, false);

            if (!loggerContextClass.isInstance(context))
                throw new IllegalStateException("Not a Log4j2 Core context");

            Class<?> levelClass = Class.forName("org.apache.logging.log4j.Level");
            Class<?> configuratorClass = Class.forName("org.apache.logging.log4j.core.config.Configurator");

            this.getLevel = levelClass.getMethod("getLevel", String.class);
            this.setLevelMethod = configuratorClass.getMethod("setLevel", String.class, levelClass);
            this.setRootLevelMethod = configuratorClass.getMethod("setRootLevel", levelClass);
        }

        @Override
        public void setLevel(@NotNull String loggerName, @NotNull String level) {
            try {
                Object lvl = this.getLevel.invoke(null, level);
                this.setLevelMethod.invoke(null, loggerName, lvl);
            } catch (ReflectiveOperationException ignored) {
                // Silently skip on failure
            }
        }

        @Override
        public void setRootLevel(@NotNull String level) {
            try {
                Object lvl = this.getLevel.invoke(null, level);
                this.setRootLevelMethod.invoke(null, lvl);
            } catch (ReflectiveOperationException ignored) {
                // Silently skip on failure
            }
        }

    }

    /**
     * Cached Logback reflection bridge that resolves method accessors once at construction time.
     */
    private static final class LogbackBridge implements Bridge {

        private final @NotNull Object factory;
        private final @NotNull Method getLogger;
        private final @NotNull Method toLevel;
        private final @NotNull Method setLevelMethod;

        private LogbackBridge() throws Exception {
            Class<?> loggerFactoryClass = Class.forName("org.slf4j.LoggerFactory");
            Method getILoggerFactory = loggerFactoryClass.getMethod("getILoggerFactory");
            this.factory = getILoggerFactory.invoke(null);

            if (!"ch.qos.logback.classic.LoggerContext".equals(this.factory.getClass().getName()))
                throw new IllegalStateException("Not a Logback LoggerContext");

            Class<?> levelClass = Class.forName("ch.qos.logback.classic.Level");
            Class<?> loggerClass = Class.forName("ch.qos.logback.classic.Logger");

            this.getLogger = this.factory.getClass().getMethod("getLogger", String.class);
            this.toLevel = levelClass.getMethod("toLevel", String.class);
            this.setLevelMethod = loggerClass.getMethod("setLevel", levelClass);
        }

        @Override
        public void setLevel(@NotNull String loggerName, @NotNull String level) {
            try {
                Object logger = this.getLogger.invoke(this.factory, loggerName);
                Object logbackLevel = this.toLevel.invoke(null, level);
                this.setLevelMethod.invoke(logger, logbackLevel);
            } catch (ReflectiveOperationException ignored) {
                // Silently skip on failure
            }
        }

        @Override
        public void setRootLevel(@NotNull String level) {
            setLevel("ROOT", level);
        }

    }

}
