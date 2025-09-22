create database if not exists inventory;

use inventory;

create table if not exists stock (
    store_id int not null,
    sku varchar(255) not null,
    quantity int not null,
    primary key (store_id, sku)
);

create table if not exists transactions (
    store_id int not null,
    sku varchar(255) not null,
    id int not null,
    quantity int not null,
    created_at varchar(255) not null,
    primary key (store_id, sku, id)
);
