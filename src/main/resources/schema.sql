create table if not exists MEMBER
(
    member_no  int auto_increment primary key,
    name      varchar(100) not null,
    type      enum ('READY', 'COMPLETE') default 'READY',
    created_at datetime                   default now()
);
