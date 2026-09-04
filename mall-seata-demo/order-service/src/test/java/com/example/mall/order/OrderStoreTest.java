package com.example.mall.order;
import org.junit.jupiter.api.Test;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import static org.assertj.core.api.Assertions.*;
@SpringBootTest(properties={"seata.enabled=false","seata.saga.enabled=false","spring.datasource.url=jdbc:h2:mem:orders;DB_CLOSE_DELAY=-1"}) class OrderStoreTest {
 @Autowired OrderStore store;
 @Test void orderLifecycleIsIdempotent(){store.prepare(null,"ok");assertThat(store.status("ok")).isEqualTo("PENDING");store.complete("ok");store.complete("ok");assertThat(store.status("ok")).isEqualTo("CONFIRMED");}
 @Test void sagaCreateReplayPreservesPendingOrder(){store.sagaCreate("replay-pending");store.sagaCreate("replay-pending");assertThat(store.status("replay-pending")).isEqualTo("PENDING");}
 @Test void sagaCreateReplayPreservesConfirmedOrder(){store.sagaCreate("replay-confirmed");store.sagaConfirm("replay-confirmed");store.sagaCreate("replay-confirmed");assertThat(store.status("replay-confirmed")).isEqualTo("CONFIRMED");}
 @Test void sagaCreateReplayPreservesCanceledOrder(){store.sagaCreate("replay-canceled");store.sagaCancel("replay-canceled");store.sagaCreate("replay-canceled");assertThat(store.status("replay-canceled")).isEqualTo("CANCELED");}
 @Test void sagaOrderCompensationCancelsCreatedOrder(){OrderRequest r=new OrderRequest("rollback","P1","U1",1,1);SagaBranches branches=new SagaBranches(null,store);branches.createOrder(r);branches.cancelOrder(r);branches.cancelOrder(r);assertThat(store.status("rollback")).isEqualTo("CANCELED");}
}
