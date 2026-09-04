package com.example.mall.points;
import io.seata.rm.tcc.api.*;
@LocalTCC
public interface PointsTccAction {
 @TwoPhaseBusinessAction(name="reservePoints",commitMethod="confirm",rollbackMethod="cancel",useTCCFence=true)
 boolean prepare(BusinessActionContext context,@BusinessActionContextParameter(paramName="orderId") String orderId,@BusinessActionContextParameter(paramName="userId") String userId,@BusinessActionContextParameter(paramName="points") int points);
 boolean confirm(BusinessActionContext context);
 boolean cancel(BusinessActionContext context);
}
