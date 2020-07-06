## 有多少个库，就在多少库中执行
create database easy_id_generator default charset utf8mb4;
use easy_id_generator;
create table if not exists segment
(
    id            bigint unsigned auto_increment primary key comment '自增主键',
    version       bigint      default 0  not null comment '版本号',
    business_type varchar(63) default '' not null comment '业务类型，唯一',
    max_id        bigint      default 0  not null comment '当前最大id',
    step          int         default 0  null comment '步长',
    increment     int         default 1  not null comment '每次id增量',
    remainder     int         default 0  not null comment '余数',
    created_at    bigint unsigned        not null comment '创建时间',
    updated_at    bigint unsigned        not null comment '更新时间',
    constraint uniq_business_type unique (business_type)
) charset = utf8mb4
  engine Innodb comment '号段表';

# db1中执行
insert into easy_id_generator.segment
(version, business_type, max_id, step, increment, remainder, created_at, updated_at)
values (1, 'order_business', 1000, 1000, 2, 0, now(), now());
# db2中执行
insert into easy_id_generator.segment
(version, business_type, max_id, step, increment, remainder, created_at, updated_at)
values (1, 'order_business', 1000, 1000, 2, 1, now(), now());
## 如果有N个库,需要在每个库执行插入一条记录
insert into easy_id_generator.segment
(version, business_type, max_id, step, increment, remainder, created_at, updated_at)
values (1, 'order_business', 1000, 1000, N, 取值为[0, N - 1], now(), now());

-- increment和remainder的关系： 当需要10个库时，increment为10，remainder的值依次为0-9
