create table t_user
(
    id          int auto_increment primary key,
    user_name   varchar,
    nick        varchar,
    create_time datetime default now()
);

insert into t_user (user_name, nick)
values ('fly', 'flyinwind')
     , ('test', 'test_nick');
