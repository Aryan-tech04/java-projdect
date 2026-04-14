create table if not exists users (
    id bigint auto_increment primary key,
    full_name varchar(120) not null,
    email varchar(120) not null unique,
    password_hash varchar(255) not null,
    role varchar(50) not null
);

create table if not exists assets (
    id bigint auto_increment primary key,
    asset_name varchar(120) not null,
    ticker_symbol varchar(20) not null,
    category varchar(50) not null,
    quantity int not null,
    buy_price decimal(12, 2) not null,
    current_price decimal(12, 2) not null,
    risk_level varchar(20) not null,
    notes varchar(255),
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp
);
