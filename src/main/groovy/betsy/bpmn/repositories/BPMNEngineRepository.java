package betsy.bpmn.repositories;

import betsy.bpmn.engines.AbstractBPMNEngine;
import betsy.bpmn.engines.activiti.*;
import betsy.bpmn.engines.camunda.*;
import betsy.bpmn.engines.flowable.Flowable5220Engine;
import betsy.bpmn.engines.jbpm.*;
import betsy.common.repositories.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BPMNEngineRepository {
    private final Repository<AbstractBPMNEngine> repo = new Repository<>();

    public BPMNEngineRepository() {
        List<AbstractBPMNEngine> all = new ArrayList<>(Arrays.asList(
                new CamundaEngine(), new Camunda710Engine(), new Camunda720Engine(), new Camunda730Engine(), new Camunda740Engine(), new Camunda750Engine(),
                new Activiti5151Engine(), new ActivitiEngine(), new Activiti5170Engine(), new Activiti5180Engine(), new Activiti5190Engine(), new Activiti51902Engine(), new Activiti5200Engine(), new Activiti5210Engine(), new Activiti5220Engine(),
                new Flowable5220Engine(),
                new JbpmEngine(), new JbpmEngine610(), new JbpmEngine620(), new JbpmEngine630(), new JbpmEngine640()));
        repo.put("ALL", all);

        // insert every engine into the map
        for (AbstractBPMNEngine engine : repo.getByName("ALL")) {
            repo.put(engine.getName(), Collections.singletonList(engine));
        }

        repo.put("activiti", repo.getByName("activiti__5_20_0"));
        repo.put("jbpm", repo.getByName("jbpm__6_4_0"));
        repo.put("camunda", repo.getByName("camunda__7_5_0"));
    }

    public List<AbstractBPMNEngine> getByName(String name) {
        return repo.getByName(name);
    }

    public List<AbstractBPMNEngine> getByNames(String... names) {
        return repo.getByNames(names);
    }

    public List<String> getNames() {
        return repo.getNames();
    }

}
