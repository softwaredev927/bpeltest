package betsy.bpmn.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import betsy.bpmn.engines.AbstractBPMNEngine;
import betsy.common.model.ProcessFolderStructure;
import betsy.common.model.engine.Engine;
import betsy.common.model.engine.EngineDimension;
import betsy.common.model.feature.Feature;
import betsy.common.model.feature.FeatureDimension;
import betsy.common.model.feature.Group;
import betsy.common.model.input.EngineIndependentProcess;

public class BPMNProcess implements ProcessFolderStructure, Comparable<BPMNProcess>, FeatureDimension, EngineDimension {

    private EngineIndependentProcess engineIndependentProcess;
    private AbstractBPMNEngine engine;
    private Path deploymentPackagePath;

    public EngineIndependentProcess getEngineIndependentProcess() {
        return engineIndependentProcess;
    }

    public void setEngineIndependentProcess(EngineIndependentProcess engineIndependentProcess) {
        this.engineIndependentProcess = engineIndependentProcess;
    }

    public Path getDeploymentPackagePath() {
        return deploymentPackagePath;
    }

    public void setDeploymentPackagePath(Path deploymentPackagePath) {
        this.deploymentPackagePath = deploymentPackagePath;
    }

    public BPMNProcess(EngineIndependentProcess engineIndependentProcess) {
        this.engineIndependentProcess = Objects.requireNonNull(engineIndependentProcess);
    }

    public BPMNProcess createCopyWithoutEngine() {
        return new BPMNProcess(engineIndependentProcess);
    }

    public void setEngine(AbstractBPMNEngine engine) {
        this.engine = engine;
    }

    public Path getTargetReportsPathWithCase(int testCaseNumber) {
        return getTargetReportsPath().resolve("case" + testCaseNumber);
    }

    public Path getTargetTestBinPath() {
        return getTargetPath().resolve("testBin");
    }

    public Path getTargetTestBinPathWithCase(int testCaseName) {
        return getTargetTestBinPath().resolve("case" + testCaseName);
    }

    public Path getTargetTestSrcPath() {
        return getTargetPath().resolve("testSrc");
    }

    public Path getTargetTestSrcPathWithCase(int testCaseNumber) {
        return getTargetTestSrcPath().resolve("case" + testCaseNumber);
    }

    @Override
    public AbstractBPMNEngine getEngine() {
        return engine;
    }

    @Override
    public Path getProcess() {
        return engineIndependentProcess.getProcess();
    }

    @Override
    public Group getGroup() {
        return this.engineIndependentProcess.getGroup();
    }

    @Override
    public Feature getFeature() {
        return engineIndependentProcess.getFeature();
    }

    public List<BPMNTestCase> getTestCases() {
        return engineIndependentProcess.getTestCases().stream().map(p -> (BPMNTestCase) p).collect(Collectors.toList());
    }

    @Override
    public int compareTo(BPMNProcess o) {
        return engineIndependentProcess.compareTo(o.engineIndependentProcess);
    }

    public String getDescription() {
        return engineIndependentProcess.getDescription();
    }

    @Override
    public Engine getEngineObject() {
        return getEngine().getEngineObject();
    }

    public FeatureDimension getFeatureDimension() {
        return engineIndependentProcess;
    }

    @Override
    public String getGroupName() {
        return getFeatureDimension().getGroup().getName();
    }

    public String getPackageID() {
        return String.join(".", getEngineID(), getGroup().getName());
    }
}
