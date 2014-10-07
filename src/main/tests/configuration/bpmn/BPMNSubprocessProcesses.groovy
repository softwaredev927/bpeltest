package configuration.bpmn

import betsy.bpmn.model.BPMNProcess
import betsy.bpmn.model.BPMNTestCase

class BPMNSubprocessProcesses {
    static BPMNProcessBuilder builder = new BPMNProcessBuilder()

    public static final BPMNProcess SUBPROCESS = builder.buildSubprocessProcess(
            "Subprocess", "A simple test for a subprocess",
            [
                    new BPMNTestCase(1).assertSuccess().assertSubprocess()
            ]
    )

    public static final BPMNProcess TRANSACTION = builder.buildSubprocessProcess(
            "Transaction", "A simple test for a transaction subprocess",
            [
                    new BPMNTestCase(1).assertTransactionTask().assertSuccess()
            ]
    )

    public static final List<BPMNProcess> SUBPROCESSES = [
            SUBPROCESS,
            TRANSACTION
    ].flatten() as List<BPMNProcess>
}