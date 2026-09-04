create table if not exists product_stock(
    product_id varchar(64) primary key,
    available int not null,
    frozen int not null
);
merge into product_stock key(product_id) values('P1',100,0);

create table if not exists stock_reservation(
    order_id varchar(64) primary key,
    product_id varchar(64) not null,
    amount int not null,
    status varchar(16) not null
);

create table if not exists stock_saga_deduction(
    order_id varchar(64) primary key,
    product_id varchar(64) not null,
    amount int not null,
    status varchar(16) not null
);

create table if not exists tcc_fence_log(
    xid varchar(128) not null,
    branch_id bigint not null,
    action_name varchar(64) not null,
    status tinyint not null,
    gmt_create timestamp(3) not null,
    gmt_modified timestamp(3) not null,
    primary key (xid, branch_id)
);
create index if not exists idx_tcc_fence_gmt_modified on tcc_fence_log(gmt_modified);
create index if not exists idx_tcc_fence_status on tcc_fence_log(status);
