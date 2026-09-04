package com.example.mall.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.seata.saga.engine.StateMachineEngine;
import io.seata.saga.statelang.domain.ExecutionStatus;
import io.seata.saga.statelang.domain.StateMachineInstance;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

@SpringBootTest(properties = {
        "seata.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:orders;DB_CLOSE_DELAY=-1"
})
class SagaEngineTest {
    @Autowired
    private ApplicationContext context;

    @Autowired
    private StateMachineEngine engine;

    @Autowired
    private OrderStore orders;

    @MockBean
    private BranchClient branchClient;

    @Test
    void loadsAndParsesStateMachineResourceWithSafeFailureState() throws Exception {
        assertThat(engine).isNotNull();
        assertThat(context.getBeansOfType(StateMachineEngine.class)).hasSize(1);
        try (InputStream input = getClass().getResourceAsStream("/statemachines/mall-order-saga.json")) {
            JsonNode machine = new ObjectMapper().readTree(input);
            assertThat(machine.path("Name").asText()).isEqualTo("mallOrderSaga");
            assertThat(machine.path("States").path("CreateOrder").path("Next").asText())
                    .isEqualTo("InjectRequestedFailure");
            assertThat(machine.path("States").path("InjectRequestedFailure").path("Catch").isArray()).isTrue();
            assertThat(machine.path("States").path("DeductStock").path("ServiceMethod").asText())
                    .isEqualTo("deductStock");
        }
    }

    @Test
    void realNonpersistentEngineRunsForwardSaga() {
        when(branchClient.product(eq("saga/deduct"), eq("saga-ok"), eq("P1"), eq(2))).thenReturn(true);
        when(branchClient.points(eq("saga/deduct"), eq("saga-ok"), eq("U1"), eq(20))).thenReturn(true);

        OrderRequest request = new OrderRequest("saga-ok", "P1", "U1", 2, 20);
        StateMachineInstance instance = new SagaOrderService(engine).place(request);

        assertThat(instance.getStatus()).isEqualTo(ExecutionStatus.SU);
        assertThat(orders.status("saga-ok")).isEqualTo("CONFIRMED");
        InOrder order = inOrder(branchClient);
        order.verify(branchClient).product("saga/deduct", "saga-ok", "P1", 2);
        order.verify(branchClient).points("saga/deduct", "saga-ok", "U1", 20);
    }

    @Test
    void realNonpersistentEngineCompensatesCompletedActionsInReverseOrder() {
        when(branchClient.product(eq("saga/deduct"), eq("saga-fail"), eq("P1"), eq(2))).thenReturn(true);
        when(branchClient.points(eq("saga/deduct"), eq("saga-fail"), eq("U1"), eq(20))).thenReturn(true);
        when(branchClient.restorePoints("saga-fail")).thenReturn(true);
        when(branchClient.restoreProduct("saga-fail")).thenReturn(true);

        OrderRequest request = new OrderRequest("saga-fail", "P1", "U1", 2, 20);
        request.setFailAfterOrder(true);
        StateMachineInstance instance = new SagaOrderService(engine).place(request);

        assertThat(instance.getStatus()).isEqualTo(ExecutionStatus.UN);
        assertThat(instance.getCompensationStatus()).isEqualTo(ExecutionStatus.SU);
        assertThat(orders.status("saga-fail")).isEqualTo("CANCELED");
        InOrder order = inOrder(branchClient);
        order.verify(branchClient).product("saga/deduct", "saga-fail", "P1", 2);
        order.verify(branchClient).points("saga/deduct", "saga-fail", "U1", 20);
        order.verify(branchClient).restorePoints("saga-fail");
        order.verify(branchClient).restoreProduct("saga-fail");
    }
}
