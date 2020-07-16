create table account
(
    id         serial primary key,
    username   varchar(50) unique not null,
    email      varchar(50) unique not null,
    password   varchar            not null,
    registered date               not null
);
