create table user_profile
(
    username    varchar(50) primary key references account (username) on delete cascade,
    first_name  varchar(50) not null,
    last_name   varchar(50),
    description text,
    image_url   text
);
