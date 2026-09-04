package com.example.mall.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(properties = {
        "seata.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:product;DB_CLOSE_DELAY=-1"
})
class ProductServiceTest {
    @Autowired
    private ProductService service;

    @Test
    void tccConfirmsAndCancelsIdempotently() {
        service.prepare(null, "p-ok", "P1", 3);
        assertThat(service.available("P1")).isEqualTo(97);
        assertThat(service.frozen("P1")).isEqualTo(3);
        service.confirm("p-ok");
        service.confirm("p-ok");
        assertThat(service.available("P1")).isEqualTo(97);
        assertThat(service.frozen("P1")).isZero();

        service.prepare(null, "p-cancel", "P1", 2);
        service.cancel("p-cancel");
        service.cancel("p-cancel");
        assertThat(service.available("P1")).isEqualTo(97);
        assertThat(service.frozen("P1")).isZero();
    }

    @Test
    void sagaDeductsImmediatelyAndRestoresIdempotently() {
        service.sagaDeduct("saga-product", "P1", 4);
        service.sagaDeduct("saga-product", "P1", 4);
        assertThat(service.available("P1")).isEqualTo(96);
        assertThat(service.frozen("P1")).isZero();
        assertThat(service.sagaStatus("saga-product")).isEqualTo("DEDUCTED");

        service.sagaRestore("saga-product");
        service.sagaRestore("saga-product");
        assertThat(service.available("P1")).isEqualTo(100);
        assertThat(service.frozen("P1")).isZero();
        assertThat(service.sagaStatus("saga-product")).isEqualTo("RESTORED");
    }

    @Test
    void rejectsInsufficientStock() {
        assertThatThrownBy(() -> service.sagaDeduct("p-fail", "P1", 101))
                .isInstanceOf(IllegalStateException.class);
    }
}
