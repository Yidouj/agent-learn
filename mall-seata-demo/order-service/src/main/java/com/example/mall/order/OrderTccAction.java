package com.example.mall.order;
import io.seata.rm.tcc.api.*;
@LocalTCC public interface OrderTccAction {
 @TwoPhaseBusinessAction(name="createOrder",commitMethod="confirm",rollbackMethod="cancel",useTCCFence=true) boolean prepare(BusinessActionContext context,@BusinessActionContextParameter(paramName="orderId") String orderId);
 boolean confirm(BusinessActionContext context); boolean cancel(BusinessActionContext context);
}
