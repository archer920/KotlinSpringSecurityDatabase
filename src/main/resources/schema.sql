/* See https://docs.spring.io/spring-security/site/docs/current/reference/html/appendix-schema.html */

DROP TABLE IF EXISTS persistent_logins;
DROP TABLE IF EXISTS group_members;
DROP TABLE IF EXISTS group_authorities;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS users;

create table users(
  username varchar_ignorecase(50) not null primary key,
  password varchar_ignorecase(50) not null,
  enabled boolean not null
);

create table authorities (
  username varchar_ignorecase(50) not null,
  authority varchar_ignorecase(50) not null,
  constraint fk_authorities_users foreign key(username) references users(username)
);

create unique index ix_auth_username on authorities (username,authority);

create table groups (
  id bigint generated by default as identity(start with 0) primary key,
  group_name varchar_ignorecase(50) not null
);

create table group_authorities (
  group_id bigint not null,
  authority varchar(50) not null,
  constraint fk_group_authorities_group foreign key(group_id) references groups(id)
);

create table group_members (
  id bigint generated by default as identity(start with 0) primary key,
  username varchar(50) not null,
  group_id bigint not null,
  constraint fk_group_members_group foreign key(group_id) references groups(id)
);

create table persistent_logins (
  username varchar(64) not null,
  series varchar(64) primary key,
  token varchar(64) not null,
  last_used timestamp not null
);

insert into users values('bob_belcher', 'burger_bob', true);
insert into authorities values ('bob_belcher', 'user');