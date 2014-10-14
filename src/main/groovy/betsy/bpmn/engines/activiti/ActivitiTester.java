package betsy.bpmn.engines.activiti;

import betsy.bpmn.engines.BPMNTester;
import betsy.bpmn.engines.LogFileAnalyzer;
import betsy.bpmn.engines.camunda.JsonHelper;
import betsy.bpmn.model.BPMNTestCase;
import betsy.bpmn.model.BPMNTestCaseVariable;
import betsy.common.tasks.FileTasks;
import betsy.common.tasks.WaitTasks;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ActivitiTester {
    /**
     * runs a single test
     */
    public void runTest() {
        //make bin dir
        FileTasks.mkdirs(testBin);
        FileTasks.mkdirs(reportPath);

        Path logFile = FileTasks.findFirstMatchInFolder(logDir, "catalina*");

        addDeploymentErrorsToLogFile(logFile);

        if (!testCase.getSelfStarting()) {
            try {
                startProcess(key, BPMNTestCaseVariable.mapToArrayWithMaps(testCase.getVariables()));
            } catch (Exception e) {
                log.info("Could not start test case", e);
            }
        }

        WaitTasks.sleep(testCase.getDelay());
        addRuntimeErrorsToLogFile(logFile);

        BPMNTester.setupPathToToolsJarForJavacAntTask(this);
        BPMNTester.compileTest(testSrc, testBin);
        BPMNTester.executeTest(testSrc, testBin, reportPath);
    }

    private void addDeploymentErrorsToLogFile(Path logFile) {
        LogFileAnalyzer analyzer = new LogFileAnalyzer(logFile);
        analyzer.addSubstring("org.activiti.engine.ActivitiException", "runtimeException");
        analyzer.addSubstring("EndEvent_2 throws error event with errorCode 'ERR-1'", "thrownErrorEvent");
        for (String deploymentError : analyzer.getErrors()) {
            BPMNTester.appendToFile(getFileName(), deploymentError);
        }
    }

    private void addRuntimeErrorsToLogFile(Path logFile) {
        LogFileAnalyzer analyzer2 = new LogFileAnalyzer(logFile);
        analyzer2.addSubstring("Ignoring unsupported activity type");
        analyzer2.addSubstring("org.activiti.engine.ActivitiException");
        for (String deploymentError : analyzer2.getErrors()) {
            BPMNTester.appendToFile(getFileName(), deploymentError);
        }
    }

    private Path getFileName() {
        return logDir.resolve("..").normalize().resolve("bin").resolve("log" + testCase.getNumber() + ".txt");
    }

    public static void startProcess(String id, Object[] variables) {
        log.info("Start process instance for " + id);
        String deploymentUrl = ActivitiEngine.URL + "/service/runtime/process-instances";

        Map<String, Object> request = new HashMap<>();
        request.put("processDefinitionKey", id);
        request.put("variables", variables);
        request.put("businessKey", "key-" + id);

        JSONObject jsonRequest = new JSONObject(request);
        log.info("With json message: " + jsonRequest.toString());

        JsonHelper.post(deploymentUrl, jsonRequest, 201);
    }

    public BPMNTestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(BPMNTestCase testCase) {
        this.testCase = testCase;
    }

    public String getRestURL() {
        return restURL;
    }

    public void setRestURL(String restURL) {
        this.restURL = restURL;
    }

    public Path getReportPath() {
        return reportPath;
    }

    public void setReportPath(Path reportPath) {
        this.reportPath = reportPath;
    }

    public Path getTestBin() {
        return testBin;
    }

    public void setTestBin(Path testBin) {
        this.testBin = testBin;
    }

    public Path getTestSrc() {
        return testSrc;
    }

    public void setTestSrc(Path testSrc) {
        this.testSrc = testSrc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Path getLogDir() {
        return logDir;
    }

    public void setLogDir(Path logDir) {
        this.logDir = logDir;
    }

    private BPMNTestCase testCase;
    private String restURL;
    private Path reportPath;
    private Path testBin;
    private Path testSrc;
    private String key;
    private Path logDir;
    private static final Logger log = Logger.getLogger(ActivitiTester.class);
}
