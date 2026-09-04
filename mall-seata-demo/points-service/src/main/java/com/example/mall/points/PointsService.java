package com.example.mall.points;

import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointsService implements PointsTccAction {
    private final JdbcTemplate jdbc;

    public PointsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public boolean prepare(BusinessActionContext context, String orderId, String userId, int points) {
        validate(points);
        Integer exists = jdbc.queryForObject(
                "select count(*) from points_reservation where order_id=?", Integer.class, orderId);
        if (exists != null && exists > 0) {
            return true;
        }
        int changed = jdbc.update(
                "update user_points set available=available-?, frozen=frozen+? where user_id=? and available>=?",
                points, points, userId, points);
        if (changed != 1) {
            throw new IllegalStateException("insufficient points");
        }
        jdbc.update("insert into points_reservation(order_id,user_id,amount,status) values(?,?,?,'TRYING')",
                orderId, userId, points);
        return true;
    }

    @Override
    @Transactional
    public boolean confirm(BusinessActionContext context) {
        return confirm(actionValue(context));
    }

    @Transactional
    public boolean confirm(String orderId) {
        int changed = jdbc.update(
                "update points_reservation set status='CONFIRMED' where order_id=? and status='TRYING'", orderId);
        if (changed == 1) {
            jdbc.update("update user_points set frozen=frozen-(select amount from points_reservation where order_id=?) "
                            + "where user_id=(select user_id from points_reservation where order_id=?)",
                    orderId, orderId);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean cancel(BusinessActionContext context) {
        return cancel(actionValue(context));
    }

    @Transactional
    public boolean cancel(String orderId) {
        int changed = jdbc.update(
                "update points_reservation set status='CANCELED' where order_id=? and status='TRYING'", orderId);
        if (changed == 1) {
            jdbc.update("update user_points set available=available+(select amount from points_reservation where order_id=?), "
                            + "frozen=frozen-(select amount from points_reservation where order_id=?) "
                            + "where user_id=(select user_id from points_reservation where order_id=?)",
                    orderId, orderId, orderId);
        }
        return true;
    }

    @Transactional
    public boolean sagaDeduct(String orderId, String userId, int points) {
        validate(points);
        Integer exists = jdbc.queryForObject(
                "select count(*) from points_saga_deduction where order_id=?", Integer.class, orderId);
        if (exists != null && exists > 0) {
            return true;
        }
        int changed = jdbc.update(
                "update user_points set available=available-? where user_id=? and available>=?",
                points, userId, points);
        if (changed != 1) {
            throw new IllegalStateException("insufficient points");
        }
        jdbc.update("insert into points_saga_deduction(order_id,user_id,amount,status) values(?,?,?,'DEDUCTED')",
                orderId, userId, points);
        return true;
    }

    @Transactional
    public boolean sagaRestore(String orderId) {
        int changed = jdbc.update(
                "update points_saga_deduction set status='RESTORED' where order_id=? and status='DEDUCTED'", orderId);
        if (changed == 1) {
            jdbc.update("update user_points set available=available+(select amount from points_saga_deduction where order_id=?) "
                            + "where user_id=(select user_id from points_saga_deduction where order_id=?)",
                    orderId, orderId);
        }
        return true;
    }

    public int available(String userId) {
        return jdbc.queryForObject("select available from user_points where user_id=?", Integer.class, userId);
    }

    public int frozen(String userId) {
        return jdbc.queryForObject("select frozen from user_points where user_id=?", Integer.class, userId);
    }

    public String sagaStatus(String orderId) {
        return jdbc.queryForObject("select status from points_saga_deduction where order_id=?", String.class, orderId);
    }

    private void validate(int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("points must be positive");
        }
    }

    private String actionValue(BusinessActionContext context) {
        return String.valueOf(context.getActionContext("orderId"));
    }
}
