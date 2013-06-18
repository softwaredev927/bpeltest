package configuration.processes

import betsy.data.Process
import betsy.data.SoapTestStep
import betsy.data.TestCase
import betsy.data.assertions.ExitAssertion
import betsy.data.assertions.SoapFaultTestAssertion

class Processes {

    BasicActivityProcesses basicActivityProcesses = new BasicActivityProcesses()

    StructuredActivityProcesses structuredActivityProcesses = new StructuredActivityProcesses()

    ScopeProcesses scopeProcesses = new ScopeProcesses()

    PatternProcesses patternProcesses = new PatternProcesses()

    public final List<Process> ALL = [
            structuredActivityProcesses.STRUCTURED_ACTIVITIES,
            basicActivityProcesses.BASIC_ACTIVITIES,
            scopeProcesses.SCOPES,
            patternProcesses.CONTROL_FLOW_PATTERNS
    ].flatten() as List<Process>

    public final List<Process> FAULTS = ALL.findAll { process ->
        process.testCases.any {
            it.testSteps.any { it instanceof SoapTestStep && it.assertions.any { it instanceof SoapFaultTestAssertion } }
        }
    }

    public final List<Process> WITH_EXIT_ASSERTION = ALL.findAll { process ->
        process.testCases.any { it.testSteps.any { it instanceof SoapTestStep && it.assertions.any { it instanceof ExitAssertion } } }
    }

    public List<Process> get(String name) {
        String upperCaseName = name.toUpperCase()
        if ("ALL" == upperCaseName) {
            return ALL
        } else if ("WITH_EXIT_ASSERTION" == upperCaseName) {
            return WITH_EXIT_ASSERTION
        } else if ("FAULTS" == upperCaseName) {
            return FAULTS
        }

        List<Process> result = getBasicProcess(upperCaseName)
        if (result == null) {
            result = getStructuredProcess(upperCaseName)
            if (result == null) {
                result = getScopeProcess(upperCaseName)
                if (result == null) {
                    result = getPatternProcess(upperCaseName)
                    if (result == null) {
                        result = ALL.findAll({ it.bpelFileNameWithoutExtension.toUpperCase() == upperCaseName })
                        if (result.isEmpty()) {
                            throw new IllegalArgumentException("Process ${name} does not exist")
                        }
                    }
                }
            }
        }
        return result
    }

    List<Process> getBasicProcess(String name) {
        try {
            return [basicActivityProcesses."$name"]
        } catch (MissingPropertyException e) {
            return null
        }
    }

    List<Process> getStructuredProcess(String name) {
        try {
            return [structuredActivityProcesses."$name"]
        } catch (MissingPropertyException e) {
            return null
        }
    }

    List<Process> getPatternProcess(String name) {
        try {
            return [patternProcesses."$name"]
        } catch (MissingPropertyException e) {
            return null
        }
    }


    List<Process> getScopeProcess(String name) {
        try {
            return [scopeProcesses."$name"]
        } catch (MissingPropertyException e) {
            return null
        }
    }

}
