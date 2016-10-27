package com.mesosphere.dcos.kafka.plan;

import com.mesosphere.dcos.kafka.config.KafkaConfigState;
import com.mesosphere.dcos.kafka.offer.PersistentOfferRequirementProvider;
import com.mesosphere.dcos.kafka.state.ClusterState;
import com.mesosphere.dcos.kafka.state.FrameworkState;
import com.mesosphere.dcos.kafka.test.ConfigTestUtils;
import com.mesosphere.dcos.kafka.test.KafkaTestUtils;
import org.apache.mesos.Protos;
import org.apache.mesos.dcos.Capabilities;
import org.apache.mesos.offer.OfferRequirement;
import org.apache.mesos.offer.TaskUtils;
import org.apache.mesos.scheduler.plan.DefaultBlock;
import org.apache.mesos.state.StateStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class tests the KafkaUpdateBlock class.
 */
public class KafkaUpdateBlockTest {
    public static final String TASK_NAME = "test-task-name";
    public static final Protos.TaskID TASK_ID = TaskUtils.toTaskId(TASK_NAME);
    public static final Protos.SlaveID AGENT_ID = Protos.SlaveID.newBuilder().setValue("test-slave-id").build();

    @Mock private FrameworkState frameworkState;
    @Mock private KafkaConfigState configState;
    @Mock private ClusterState clusterState;
    @Mock private Capabilities capabilities;
    private PersistentOfferRequirementProvider offerRequirementProvider;
    private DefaultBlock updateBlock;

    private static final Protos.Offer.Operation operation = Protos.Offer.Operation.newBuilder()
            .setType(Protos.Offer.Operation.Type.LAUNCH)
            .setLaunch(Protos.Offer.Operation.Launch.newBuilder()
                    .addTaskInfos(Protos.TaskInfo.newBuilder()
                            .setTaskId(TASK_ID)
                            .setName(TASK_NAME)
                            .setSlaveId(AGENT_ID)))
            .build();
    private static final Collection<Protos.Offer.Operation> nonEmptyOperations =
            Arrays.asList(operation);

    @Before
    public void beforeEach() throws Exception {
        MockitoAnnotations.initMocks(this);
        StateStore stateStore = mock(StateStore.class);
        when(stateStore.fetchFrameworkId()).thenReturn(Optional.of(KafkaTestUtils.testFrameworkId));
        when(frameworkState.getStateStore()).thenReturn(stateStore);
        when(frameworkState.getTaskStatusForBroker(any())).thenReturn(Optional.empty());
        when(configState.fetch(UUID.fromString(KafkaTestUtils.testConfigName))).thenReturn(
                ConfigTestUtils.getTestKafkaSchedulerConfiguration());
        when(capabilities.supportsNamedVips()).thenReturn(true);
        when(clusterState.getCapabilities()).thenReturn(capabilities);
        offerRequirementProvider = new PersistentOfferRequirementProvider(frameworkState, configState, clusterState);
        updateBlock =
                KafkaUpdateBlock.create(
                        frameworkState,
                        offerRequirementProvider,
                        KafkaTestUtils.testConfigName,
                        0);
    }

    @Test
    public void testKafkaUpdateBlockConstruction() {
        Assert.assertNotNull(updateBlock);
    }

    @Test
    public void testStart() {
        OfferRequirement offerRequirement = updateBlock.start().get();
        Assert.assertNotNull(offerRequirement);
        Assert.assertEquals(1, offerRequirement.getTaskRequirements().size());
        Assert.assertTrue(offerRequirement.getExecutorRequirementOptional().isPresent());
    }

    @Test
    public void testUpdateWhilePending() {
        Assert.assertTrue(updateBlock.isPending());
        updateBlock.update(getRunningTaskStatus("bad-task-id"));
        Assert.assertTrue(updateBlock.isPending());
    }

    @Test
    public void testUpdateUnknownTaskId() {
        Assert.assertTrue(updateBlock.isPending());
        updateBlock.start();
        updateBlock.updateOfferStatus(nonEmptyOperations);
        Assert.assertTrue(updateBlock.isInProgress());
        updateBlock.update(getRunningTaskStatus("bad-task-id"));
        Assert.assertTrue(updateBlock.isInProgress());
    }

    @Test
    public void testReconciliationUpdate() {
        Assert.assertTrue(updateBlock.isPending());
        updateBlock.start();
        updateBlock.updateOfferStatus(nonEmptyOperations);
        Assert.assertTrue(updateBlock.isInProgress());
        Protos.TaskStatus reconciliationTaskStatus = getRunningTaskStatus(TASK_ID.getValue());
        reconciliationTaskStatus = Protos.TaskStatus.newBuilder(reconciliationTaskStatus)
                .setReason(Protos.TaskStatus.Reason.REASON_RECONCILIATION)
                .build();
        updateBlock.update(reconciliationTaskStatus);
        Assert.assertTrue(updateBlock.isInProgress());
    }

    @Test
    public void testUpdateExpectedTaskIdRunning() {
        Assert.assertTrue(updateBlock.isPending());
        updateBlock.start();
        updateBlock.updateOfferStatus(nonEmptyOperations);
        Assert.assertTrue(updateBlock.isInProgress());
        updateBlock.update(getRunningTaskStatus(TASK_ID.getValue()));
        Assert.assertTrue(updateBlock.isComplete());
    }

    @Test
    public void testUpdateExpectedTaskIdTerminated() {
        Assert.assertTrue(updateBlock.isPending());
        updateBlock.start();
        updateBlock.updateOfferStatus(nonEmptyOperations);
        Assert.assertTrue(updateBlock.isInProgress());
        updateBlock.update(getFailedTaskStatus(TASK_ID.getValue()));
        Assert.assertTrue(updateBlock.isPending());
    }

    private Protos.TaskStatus getRunningTaskStatus(String taskId) {
        return getTaskStatus(taskId, Protos.TaskState.TASK_RUNNING);
    }

    private Protos.TaskStatus getFailedTaskStatus(String taskId) {
        return getTaskStatus(taskId, Protos.TaskState.TASK_FAILED);
    }

    private Protos.TaskStatus getTaskStatus(String taskId, Protos.TaskState state) {
        return Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskId))
                .setState(state)
                .build();
    }
}
