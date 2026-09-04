package com.example.mall.product;

import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService implements ProductTccAction {
    private final JdbcTemplate jdbc;

    public ProductService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public boolean prepare(BusinessActionContext context, String orderId, String productId, int quantity) {
        validate(quantity);
        Integer exists = jdbc.queryForObject(
                "select count(*) from stock_reservation where order_id=?", Integer.class, orderId);
        if (exists != null && exists > 0) {
            return true;
        }
        int changed = jdbc.update(
                "update product_stock set available=available-?, frozen=frozen+? "
                        + "where product_id=? and available>=?",
                quantity, quantity, productId, quantity);
        if (changed != 1) {
            throw new IllegalStateException("insufficient stock");
        }
        jdbc.update("insert into stock_reservation(order_id,product_id,amount,status) "
                        + "values(?,?,?,'TRYING')", orderId, productId, quantity);
        return true;
    }

    @Override
    @Transactional
    public boolean confirm(BusinessActionContext context) {
        return confirm(actionValue(context, "orderId"));
    }

    @Transactional
    public boolean confirm(String orderId) {
        int changed = jdbc.update(
                "update stock_reservation set status='CONFIRMED' where order_id=? and status='TRYING'", orderId);
        if (changed == 1) {
            jdbc.update("update product_stock set frozen=frozen-(select amount from stock_reservation where order_id=?) "
                            + "where product_id=(select product_id from stock_reservation where order_id=?)",
                    orderId, orderId);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean cancel(BusinessActionContext context) {
        return cancel(actionValue(context, "orderId"));
    }

    @Transactional
    public boolean cancel(String orderId) {
        int changed = jdbc.update(
                "update stock_reservation set status='CANCELED' where order_id=? and status='TRYING'", orderId);
        if (changed == 1) {
            jdbc.update("update product_stock set available=available+(select amount from stock_reservation where order_id=?), "
                            + "frozen=frozen-(select amount from stock_reservation where order_id=?) "
                            + "where product_id=(select product_id from stock_reservation where order_id=?)",
                    orderId, orderId, orderId);
        }
        return true;
    }

    @Transactional
    public boolean sagaDeduct(String orderId, String productId, int quantity) {
        validate(quantity);
        Integer exists = jdbc.queryForObject(
                "select count(*) from stock_saga_deduction where order_id=?", Integer.class, orderId);
        if (exists != null && exists > 0) {
            return true;
        }
        int changed = jdbc.update(
                "update product_stock set available=available-? where product_id=? and available>=?",
                quantity, productId, quantity);
        if (changed != 1) {
            throw new IllegalStateException("insufficient stock");
        }
        jdbc.update("insert into stock_saga_deduction(order_id,product_id,amount,status) "
                        + "values(?,?,?,'DEDUCTED')", orderId, productId, quantity);
        return true;
    }

    @Transactional
    public boolean sagaRestore(String orderId) {
        int changed = jdbc.update(
                "update stock_saga_deduction set status='RESTORED' where order_id=? and status='DEDUCTED'", orderId);
        if (changed == 1) {
            jdbc.update("update product_stock set available=available+(select amount from stock_saga_deduction where order_id=?) "
                            + "where product_id=(select product_id from stock_saga_deduction where order_id=?)",
                    orderId, orderId);
        }
        return true;
    }

    public int available(String productId) {
        return jdbc.queryForObject("select available from product_stock where product_id=?", Integer.class, productId);
    }

    public int frozen(String productId) {
        return jdbc.queryForObject("select frozen from product_stock where product_id=?", Integer.class, productId);
    }

    public String sagaStatus(String orderId) {
        return jdbc.queryForObject("select status from stock_saga_deduction where order_id=?", String.class, orderId);
    }

    private void validate(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    private String actionValue(BusinessActionContext context, String name) {
        return String.valueOf(context.getActionContext(name));
    }
}
