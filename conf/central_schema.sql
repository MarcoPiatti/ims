create database if not exists inventory;

use inventory;

create table if not exists stock (
    sku varchar(255) not null,
    store_id int not null,
    quantity int not null
);

create table if not exists tombstone (
    id varchar(255) not null primary key
);
