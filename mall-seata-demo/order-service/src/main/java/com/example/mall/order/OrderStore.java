package com.example.mall.order;
import io.seata.rm.tcc.api.BusinessActionContext;import org.springframework.dao.DuplicateKeyException;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
@Service public class OrderStore implements OrderTccAction {
 private final JdbcTemplate jdbc; public OrderStore(JdbcTemplate jdbc){this.jdbc=jdbc;}
 @Override @Transactional public boolean prepare(BusinessActionContext c,String id){jdbc.update("merge into mall_order key(order_id) values(?,'PENDING')",id);return true;}
 @Override @Transactional public boolean confirm(BusinessActionContext c){return complete(value(c));}
 @Transactional public boolean complete(String id){jdbc.update("update mall_order set status='CONFIRMED' where order_id=? and status='PENDING'",id);return true;}
 @Override @Transactional public boolean cancel(BusinessActionContext c){return compensate(value(c));}
 @Transactional public boolean compensate(String id){jdbc.update("update mall_order set status='CANCELED' where order_id=? and status in ('PENDING','CONFIRMED')",id);return true;}
 @Transactional public boolean sagaCreate(String id){try{jdbc.update("insert into mall_order(order_id,status) select ?,'PENDING' where not exists (select 1 from mall_order where order_id=?)",id,id);}catch(DuplicateKeyException ignored){}return true;} public boolean sagaConfirm(String id){return complete(id);} public boolean sagaCancel(String id){return compensate(id);}
 public String status(String id){return jdbc.queryForObject("select status from mall_order where order_id=?",String.class,id);}
 private String value(BusinessActionContext c){return String.valueOf(c.getActionContext("orderId"));}
}
