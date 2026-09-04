package com.example.mall.product;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import io.seata.rm.tcc.api.BusinessActionContextParameter;

@LocalTCC
public interface ProductTccAction {
    @TwoPhaseBusinessAction(name = "reserveStock", commitMethod = "confirm", rollbackMethod = "cancel", useTCCFence = true)
    boolean prepare(BusinessActionContext context,
                    @BusinessActionContextParameter(paramName = "orderId") String orderId,
                    @BusinessActionContextParameter(paramName = "productId") String productId,
                    @BusinessActionContextParameter(paramName = "quantity") int quantity);
    boolean confirm(BusinessActionContext context);
    boolean cancel(BusinessActionContext context);
}
