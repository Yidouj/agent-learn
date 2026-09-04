package com.example.mall.order;
import io.seata.spring.annotation.GlobalTransactional;import org.springframework.stereotype.Service;
@Service public class TccOrderService {
 private final OrderTccAction orders;private final BranchClient branches;
 public TccOrderService(OrderTccAction orders,BranchClient branches){this.orders=orders;this.branches=branches;}
 @GlobalTransactional(name="mall-tcc-order",rollbackFor=Exception.class)
 public void place(OrderRequest r){orders.prepare(null,r.getOrderId());branches.product("tcc/prepare",r.getOrderId(),r.getProductId(),r.getQuantity());branches.points("tcc/prepare",r.getOrderId(),r.getUserId(),r.getPoints());if(r.isFailAfterOrder())throw new IllegalStateException("requested TCC rollback");}
}
