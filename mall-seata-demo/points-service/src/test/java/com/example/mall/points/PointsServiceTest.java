package com.example.mall.points;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(properties = {
        "seata.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:points;DB_CLOSE_DELAY=-1"
})
class PointsServiceTest {
    @Autowired
    private PointsService service;

    @Test
    void tccConfirmsAndCancelsIdempotently() {
        service.prepare(null, "x", "U1", 30);
        service.confirm("x");
        service.confirm("x");
        assertThat(service.available("U1")).isEqualTo(970);
        assertThat(service.frozen("U1")).isZero();

        service.prepare(null, "y", "U1", 20);
        service.cancel("y");
        service.cancel("y");
        assertThat(service.available("U1")).isEqualTo(970);
        assertThat(service.frozen("U1")).isZero();
    }

    @Test
    void sagaDeductsImmediatelyAndRestoresIdempotently() {
        service.sagaDeduct("saga-points", "U1", 40);
        service.sagaDeduct("saga-points", "U1", 40);
        assertThat(service.available("U1")).isEqualTo(960);
        assertThat(service.frozen("U1")).isZero();
        assertThat(service.sagaStatus("saga-points")).isEqualTo("DEDUCTED");

        service.sagaRestore("saga-points");
        service.sagaRestore("saga-points");
        assertThat(service.available("U1")).isEqualTo(1000);
        assertThat(service.frozen("U1")).isZero();
        assertThat(service.sagaStatus("saga-points")).isEqualTo("RESTORED");
    }

    @Test
    void rejectsInsufficientPoints() {
        assertThatThrownBy(() -> service.sagaDeduct("z", "U1", 1001))
                .isInstanceOf(IllegalStateException.class);
    }
}
