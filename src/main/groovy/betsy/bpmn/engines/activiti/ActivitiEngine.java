package betsy.bpmn.engines.activiti;

import betsy.bpel.engines.tomcat.Tomcat;
import betsy.bpel.engines.tomcat.TomcatInstaller;
import betsy.bpmn.engines.BPMNEngine;
import betsy.bpmn.model.BPMNProcess;
import betsy.common.config.Configuration;
import betsy.common.tasks.FileTasks;
import betsy.common.tasks.NetworkTasks;
import betsy.common.tasks.XSLTTasks;
import betsy.common.tasks.ZipTasks;
import betsy.common.util.ClasspathHelper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public class ActivitiEngine extends BPMNEngine {
    @Override
    public void testProcess(BPMNProcess process) {

    }

    @Override
    public String getName() {
        return "activiti";
    }

    @Override
    public void deploy(BPMNProcess process) {
        deployBpmnProcess(getTargetProcessBpmnFile(process));
    }

    public static boolean deployBpmnProcess(Path bpmnFile) {
        try {
            log.info("Deploying file " + bpmnFile.toAbsolutePath());

            String deploymentUrl = URL + "/service/repository/deployments";
            log.info("HTTP POST to " + deploymentUrl);

            HttpResponse<JsonNode> jsonResponse = Unirest.post(deploymentUrl).header("type", "multipart/form-data").field("file", bpmnFile.toFile()).asJson();

            log.info("HTTP RESPONSE code: " + jsonResponse.getCode());
            log.info("HTTP RESPONSE body: " + jsonResponse.getBody());

            return 201 == jsonResponse.getCode();

        } catch (UnirestException e) {
            throw new RuntimeException("Could not deploy", e);
        } finally {
            try {
                Unirest.shutdown();
            } catch (IOException e) {
                log.error("problem during shutdown of unirest lib", e);
            }

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

    }

    @Override
    public String getEndpointUrl(BPMNProcess process) {
        return URL + "/service/repository/";
    }

    @Override
    public void storeLogs(BPMNProcess process) {

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
    }

    public Tomcat getTomcat() {
        Tomcat tomcat = new Tomcat();
        tomcat.setEngineDir(getServerPath());
        return null;
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
