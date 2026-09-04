# Mall Seata demo

Minimal Java 8 / Spring Boot 2.7.18 / Seata 1.7.1 example containing exactly three independently deployable services and three H2 databases:

- `product-service` (`8081`): stock TCC branch plus idempotent Saga deduct/restore actions.
- `points-service` (`8082`): points TCC branch plus idempotent Saga deduct/restore actions.
- `order-service` (`8080`): TCC transaction entry point, order branch, and explicitly configured Seata Saga state-machine engine.

There is no common module and no other mall capability.

## Local verification

```bash
mvn -U -f D:/gitProject/agent-learn/mall-seata-demo/pom.xml clean test
```

The tests run without a Seata Transaction Coordinator (TC). They prove:

- all three H2 schemas initialize, including the Seata 1.7.1 `tcc_fence_log` table;
- local TCC branch state transitions are idempotent;
- Saga stock/points forward actions consume available resources directly and their compensations restore them without leaving frozen resources;
- the state-machine JSON parses, is loaded by a real `StateMachineEngine`, and executes forward and compensation paths in-process.

The Maven tests do **not** prove cross-process TCC registration, TC-driven confirm/cancel callbacks, network recovery, or production durability. The configured Saga engine is deliberately nonpersistent (`IsPersist: false`); a process restart cannot resume an in-flight Saga.

## Cross-process demonstration

Start Seata Server, then product, points, and order in separate shells:

```bash
docker compose -f D:/gitProject/agent-learn/mall-seata-demo/seata/compose.yml up -d
mvn -f D:/gitProject/agent-learn/mall-seata-demo/pom.xml -pl product-service spring-boot:run
mvn -f D:/gitProject/agent-learn/mall-seata-demo/pom.xml -pl points-service spring-boot:run
mvn -f D:/gitProject/agent-learn/mall-seata-demo/pom.xml -pl order-service spring-boot:run
```

Each service uses its own `./data/*.mv.db` file. Direct TCC HTTP calls propagate Seata's `TX_XID` header. The Saga is coordinated locally by the order process and invokes idempotent HTTP actions; it does not use the TC for Saga persistence.

## Requests

```bash
curl -X POST http://localhost:8080/orders/tcc -H 'Content-Type: application/json' -d '{"orderId":"T1","productId":"P1","userId":"U1","quantity":2,"points":20}'
curl -X POST http://localhost:8080/orders/saga -H 'Content-Type: application/json' -d '{"orderId":"S1","productId":"P1","userId":"U1","quantity":2,"points":20}'
```

Set `"failAfterOrder": true` to request TCC rollback or to fail the separate Saga state after `CreateOrder`, when compensation for the completed order action is already registered.
