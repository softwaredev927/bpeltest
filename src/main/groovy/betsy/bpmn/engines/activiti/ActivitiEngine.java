package betsy.bpmn.engines.activiti;

import betsy.bpel.engines.tomcat.Tomcat;
import betsy.bpel.engines.tomcat.TomcatInstaller;
import betsy.bpmn.engines.BPMNEngine;
import betsy.bpmn.engines.camunda.JsonHelper;
import betsy.bpmn.model.BPMNProcess;
import betsy.bpmn.model.BPMNTestBuilder;
import betsy.bpmn.model.BPMNTestCase;
import betsy.bpmn.reporting.BPMNTestcaseMerger;
import betsy.common.config.Configuration;
import betsy.common.tasks.FileTasks;
import betsy.common.tasks.NetworkTasks;
import betsy.common.tasks.XSLTTasks;
import betsy.common.tasks.ZipTasks;
import betsy.common.util.ClasspathHelper;
import org.apache.log4j.Logger;

import java.nio.file.Path;

public class ActivitiEngine extends BPMNEngine {
    @Override
    public void testProcess(BPMNProcess process) {
        for (BPMNTestCase testCase : process.getTestCases()) {
            ActivitiTester tester = new ActivitiTester();
            tester.setTestCase(testCase);
            tester.setTestSrc(process.getTargetTestSrcPathWithCase(testCase.getNumber()));
            tester.setTestBin(process.getTargetTestBinPathWithCase(testCase.getNumber()));
            tester.setRestURL(URL);
            tester.setReportPath(process.getTargetReportsPathWithCase(testCase.getNumber()));
            tester.setKey(process.getName());
            tester.setLogDir(getTomcat().getTomcatLogsDir());

            tester.runTest();
        }
        new BPMNTestcaseMerger(process.getTargetReportsPath()).mergeTestCases();
    }

    @Override
    public String getName() {
        return "activiti";
    }

    @Override
    public void deploy(BPMNProcess process) {
        deployBpmnProcess(getTargetProcessBpmnFile(process));
    }

    public static void deployBpmnProcess(Path bpmnFile) {
        log.info("Deploying file " + bpmnFile.toAbsolutePath());
        try {
            JsonHelper.post(URL + "/service/repository/deployments", bpmnFile, 201);
        } catch (Exception e) {
            log.info("deployment failed", e);
        }
    }

    @Override
    public Path getXsltPath() {
        return ClasspathHelper.getFilesystemPathFromClasspathPath("/bpmn/camunda");
    }

    @Override
    public void buildArchives(BPMNProcess process) {
        XSLTTasks.transform(getXsltPath().resolve("../scriptTask.xsl"),
                process.getResourcePath().resolve(process.getName() + ".bpmn"),
                getTargetProcessFolder(process).resolve(process.getName() + ".bpmn-temp"));

        XSLTTasks.transform(getXsltPath().resolve("camunda.xsl"),
                getTargetProcessFolder(process).resolve(process.getName() + ".bpmn-temp"),
                getTargetProcessBpmnFile(process));

        FileTasks.deleteFile(getTargetProcessFolder(process).resolve(process.getName() + ".bpmn-temp"));
    }

    private Path getTargetProcessBpmnFile(BPMNProcess process) {
        return getTargetProcessFolder(process).resolve(process.getName() + ".bpmn");
    }

    private Path getTargetProcessFolder(BPMNProcess process) {
        return process.getTargetPath().resolve("process");
    }

    @Override
    public void buildTest(BPMNProcess process) {
        BPMNTestBuilder bpmnTestBuilder = new BPMNTestBuilder();
        bpmnTestBuilder.setPackageString(getName() + "." + process.getGroup());
        bpmnTestBuilder.setLogDir(getTomcat().getTomcatBinDir());
        bpmnTestBuilder.setProcess(process);

        bpmnTestBuilder.buildTests();
    }

    @Override
    public String getEndpointUrl(BPMNProcess process) {
        return URL + "/service/repository/";
    }

    @Override
    public void storeLogs(BPMNProcess process) {
        FileTasks.mkdirs(process.getTargetLogsPath());

        // TODO only copy log files from tomcat, the other files are files for the test
        FileTasks.copyFilesInFolderIntoOtherFolder(getTomcat().getTomcatLogsDir(), process.getTargetLogsPath());

        for (BPMNTestCase tc : process.getTestCases()) {
            FileTasks.copyFileIntoFolder(getTomcat().getTomcatBinDir().resolve("log" + tc.getNumber() + ".txt"), process.getTargetLogsPath());
        }
    }

    @Override
    public void install() {
        // install tomcat
        TomcatInstaller tomcatInstaller = new TomcatInstaller();
        tomcatInstaller.setDestinationDir(getServerPath());
        tomcatInstaller.install();

        // unzip activiti
        String filename = "activiti-5.16.3.zip";
        NetworkTasks.downloadFile("https://github.com/Activiti/Activiti/releases/download/activiti-5.16.3/" + filename, Configuration.getDownloadsDir());
        ZipTasks.unzip(Configuration.getDownloadsDir().resolve(filename), getServerPath());

        // deploy
        getTomcat().deployWar(getServerPath().resolve("activiti-5.16.3").resolve("wars").resolve("activiti-rest.war"));

        String groovyFile = "groovy-all-2.2.0.jar";
        NetworkTasks.downloadFileFromBetsyRepo(groovyFile);
        getTomcat().addLib(Configuration.getDownloadsDir().resolve(groovyFile));
    }

    public Tomcat getTomcat() {
        Tomcat tomcat = new Tomcat();
        tomcat.setEngineDir(getServerPath());
        return tomcat;
    }

    @Override
    public void startup() {
        getTomcat().startup();
    }

    @Override
    public void shutdown() {
        getTomcat().shutdown();
    }

    @Override
    public boolean isRunning() {
        return getTomcat().checkIfIsRunning();
    }

    private static final Logger log = Logger.getLogger(ActivitiEngine.class);
    public static final String URL = "http://kermit:kermit@localhost:8080/activiti-rest";
}
