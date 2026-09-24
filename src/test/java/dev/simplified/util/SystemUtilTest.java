package dev.simplified.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

}
