create table if not exists user_points(
    user_id varchar(64) primary key,
    available int not null,
    frozen int not null
);
merge into user_points key(user_id) values('U1',1000,0);

create table if not exists points_reservation(
    order_id varchar(64) primary key,
    user_id varchar(64) not null,
    amount int not null,
    status varchar(16) not null
);

create table if not exists points_saga_deduction(
    order_id varchar(64) primary key,
    user_id varchar(64) not null,
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
