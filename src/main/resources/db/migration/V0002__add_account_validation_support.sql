-- By default accounts will not be considered verified, but existing ones will be set to true
alter table account
    add column verified bool default true;

alter table account
    alter column verified set default false;


create table account_verification
(
    account   varchar(50) primary key references account (username) on delete cascade,
    token     varchar(50) not null,
    issued_at date default now()
);
