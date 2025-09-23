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

create table store_availability (
    store_id int primary key,
    last_check timestamp not null
);