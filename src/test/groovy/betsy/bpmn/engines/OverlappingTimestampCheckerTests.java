package betsy.bpmn.engines;

import betsy.bpmn.model.BPMNAssertions;
import betsy.common.tasks.FileTasks;
import groovy.ui.SystemOutputInterceptor;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OverlappingTimestampCheckerTests {
    private static final Path ROOT = Paths.get(System.getProperty("user.dir"));
    private static final Path TEST_FOLDER = Paths.get("src", "test", "groovy", "betsy", "bpmn", "engines");

    private static final Path PATH_LOG_FILE = ROOT.resolve(TEST_FOLDER).resolve("log1.txt");
    private static final Path PATH_LOG_ONE = ROOT.resolve(TEST_FOLDER).resolve("log1_parallelOne.txt");
    private static final Path PATH_LOG_TWO = ROOT.resolve(TEST_FOLDER).resolve("log1_parallelTwo.txt");

    private static final String[] TIMESTAMPS = {"1420110000000", "1420111800000", "1420113600000", "1420115400000"};

    public void createOverlappingLogFilesWithTimestamps(int first, int second, int third, int fourth) throws IOException {
        String contentLogOne = String.format("%s%s%s", TIMESTAMPS[first], "\n", TIMESTAMPS[second]);
        String contentLogTwo = String.format("%s%s%s", TIMESTAMPS[third], "\n", TIMESTAMPS[fourth]);

        System.out.println("Create log files...");
        Files.createFile(PATH_LOG_FILE);
        FileTasks.createFile(PATH_LOG_ONE, contentLogOne);
        FileTasks.createFile(PATH_LOG_TWO, contentLogTwo);
        System.out.println("Creation successful!");
    }

    @Test
    public void testCheckParallelismWithOneOverlapsTwo() throws Exception {
        createOverlappingLogFilesWithTimestamps(0, 2, 1, 3);
        OverlappingTimestampChecker otc = new OverlappingTimestampChecker(PATH_LOG_FILE, PATH_LOG_ONE, PATH_LOG_TWO);
        otc.checkParallelism();
        assertTrue(executionWasParallel());
    }

    @Test
    public void testCheckParallelismWithOneBeforeTwo() throws Exception {
        createOverlappingLogFilesWithTimestamps(0, 1, 2, 3);
        OverlappingTimestampChecker otc = new OverlappingTimestampChecker(PATH_LOG_FILE, PATH_LOG_ONE, PATH_LOG_TWO);
        otc.checkParallelism();
        assertFalse(executionWasParallel());
    }

    @Test
    public void testCheckParallelismWithOneEqualsTwo() throws Exception {
        createOverlappingLogFilesWithTimestamps(0, 1, 0, 1);
        OverlappingTimestampChecker otc = new OverlappingTimestampChecker(PATH_LOG_FILE, PATH_LOG_ONE, PATH_LOG_TWO);
        otc.checkParallelism();
        assertTrue(executionWasParallel());
    }

    @Test
    public void testCheckParallelismWithOneDuringTwo() throws Exception {
        createOverlappingLogFilesWithTimestamps(1, 2, 0, 3);
        OverlappingTimestampChecker otc = new OverlappingTimestampChecker(PATH_LOG_FILE, PATH_LOG_ONE, PATH_LOG_TWO);
        otc.checkParallelism();
        assertTrue(executionWasParallel());
    }

    @Test
    public void testCheckParallelismWithOneAfterTwo() throws Exception {
        createOverlappingLogFilesWithTimestamps(2, 3, 0, 1);
        OverlappingTimestampChecker otc = new OverlappingTimestampChecker(PATH_LOG_FILE, PATH_LOG_ONE, PATH_LOG_TWO);
        otc.checkParallelism();
        assertFalse(executionWasParallel());
    }

    @After
    public void deleteLogFiles() throws IOException {
        System.out.println("Delete log files...");
        Files.deleteIfExists(PATH_LOG_FILE);
        Files.deleteIfExists(PATH_LOG_ONE);
        Files.deleteIfExists(PATH_LOG_TWO);
        System.out.println("Deletion successful!");
    }

    public boolean executionWasParallel() {
        List<String> lines = FileTasks.readAllLines(PATH_LOG_FILE);
        return lines.get(0).contains(BPMNAssertions.EXECUTION_PARALLEL.toString());
    }

}
