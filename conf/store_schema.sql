create table if not exists stock (
    sku varchar(255) not null primary key,
    quantity int not null
);

create table if not exists outbox_event (
    id BIGINT not null auto_increment primary key,
    aggregate_type varchar(255) not null,
    aggregate_id varchar(255) not null,
    type varchar(255) not null,
    payload JSON not null,
    created_at timestamp not null default current_timestamp
);
