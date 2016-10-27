package com.mesosphere.dcos.kafka.web;

import com.mesosphere.dcos.kafka.scheduler.KafkaScheduler;
import com.mesosphere.dcos.kafka.state.FrameworkState;
import org.apache.mesos.scheduler.plan.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * This class tests the BrokerCheck class.
 */
public class BrokerCheckTest {
    @Mock private PlanManager planManager;
    @Mock private Plan plan;
    @Mock private FrameworkState frameworkState;
    @Mock private DefaultPhase kafkaUpdatePhase;
    @Mock private Block block;
    @Mock private KafkaScheduler kafkaScheduler;
    private BrokerCheck brokerCheck;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        when(kafkaScheduler.getPlanManager()).thenReturn(planManager);
        when(kafkaScheduler.getFrameworkState()).thenReturn(frameworkState);
        brokerCheck = new BrokerCheck(kafkaScheduler);
    }

    @Test
    public void testBrokerCheckConstruction() {
        Assert.assertNotNull(brokerCheck);
    }

    @Test
    public void testCheckException() throws Exception {
        Assert.assertFalse(brokerCheck.check().isHealthy());
    }

    @Test
    public void testCheckNoUpdatePhase() throws Exception {
        when(planManager.getPlan()).thenReturn(plan);
        when(plan.getChildren()).thenReturn(Collections.emptyList());
        Assert.assertFalse(brokerCheck.check().isHealthy());
    }

    @Test
    public void testCheckBelowBrokerCount() throws Exception {
        when(planManager.getPlan()).thenReturn(plan);
        when(frameworkState.getRunningBrokersCount()).thenReturn(0);
        when(block.isComplete()).thenReturn(true);
        when(kafkaUpdatePhase.getChildren()).thenReturn(Arrays.asList(block));
        Mockito.<List<? extends Phase>>when(plan.getChildren()).thenReturn(getMockPhases());
        Assert.assertFalse(brokerCheck.check().isHealthy());
    }

    @Test
    public void testCheckAtBrokerCount() throws Exception {
        when(planManager.getPlan()).thenReturn(plan);
        when(frameworkState.getRunningBrokersCount()).thenReturn(1);
        when(block.isComplete()).thenReturn(true);
        when(kafkaUpdatePhase.getChildren()).thenReturn(Arrays.asList(block));
        Mockito.<List<? extends Phase>>when(plan.getChildren()).thenReturn(getMockPhases());
        Assert.assertTrue(brokerCheck.check().isHealthy());
    }

    private List<? extends Phase> getMockPhases() {
        return Arrays.asList(kafkaUpdatePhase);
    }
}