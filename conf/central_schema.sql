create database if not exists inventory;

use inventory;

create table if not exists stock (
    sku varchar(255) not null,
    store_id int not null,
    quantity int not null
);

create table if not exists transactions (
    store_id int not null,
    sku varchar(255) not null,
    id int not null,
    quantity int not null,
    created_at timestamp not null,
    primary key (store_id, sku, id)
);
