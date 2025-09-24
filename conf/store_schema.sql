create database if not exists inventory;

use inventory;

create table if not exists stock (
    sku varchar(255) not null primary key,
    quantity int not null
);

create table if not exists outbox_event (
    id bigint not null auto_increment primary key,
    store_id int not null,
    sku varchar(255) not null,
    quantity int not null,
    created_at timestamp not null default current_timestamp
);

create table if not exists store_availability (
    store_id int primary key,
    last_check timestamp not null
);

insert into store_availability (store_id, last_check) values (1, current_timestamp);
insert into stock (sku, quantity)
values ('SKU-1', 100), ('SKU-2', 150), ('SKU-3', 200);

insert into outbox_event (store_id, sku, quantity)
values (1, 'SKU-1', 100), (1, 'SKU-2', 150), (1, 'SKU-3', 200);