create type friendship_status as enum ('REQUESTED', 'ACCEPTED', 'REJECTED');

create table friendship
(
    from_username varchar(50) references account (username) on delete cascade,
    to_username   varchar(50) references account (username) on delete cascade,
    status        friendship_status not nulL,
    request_time  timestamp default now(),
    response_time timestamp,
    primary key (from_username, to_username)
);
