package com.example.mall.order;

import io.seata.core.context.RootContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BranchClient {
    private final RestTemplate http;

    public BranchClient() {
        this(new RestTemplate());
    }

    BranchClient(RestTemplate http) {
        this.http = http;
        this.http.getInterceptors().add((request, body, execution) -> {
            String xid = RootContext.getXID();
            if (xid != null) {
                request.getHeaders().add(RootContext.KEY_XID, xid);
            }
            return execution.execute(request, body);
        });
    }

    public boolean product(String path, String orderId, String resourceId, int amount) {
        return post("http://localhost:8081/internal/product/" + path,
                new BranchRequest(orderId, resourceId, amount));
    }

    public boolean points(String path, String orderId, String resourceId, int amount) {
        return post("http://localhost:8082/internal/points/" + path,
                new BranchRequest(orderId, resourceId, amount));
    }

    public boolean restoreProduct(String orderId) {
        return post("http://localhost:8081/internal/product/saga/restore/" + orderId, null);
    }

    public boolean restorePoints(String orderId) {
        return post("http://localhost:8082/internal/points/saga/restore/" + orderId, null);
    }

    private boolean post(String url, Object body) {
        Boolean result = http.postForObject(url, body, Boolean.class);
        return Boolean.TRUE.equals(result);
    }
}
