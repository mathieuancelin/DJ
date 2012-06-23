# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table song (
  id                        bigint not null,
  filepath                  varchar(1000) not null,
  name                      varchar(1000) not null,
  artist                    varchar(1000) not null,
  album                     varchar(1000) not null,
  likeit                    bigint not null,
  dontlikeit                bigint not null,
  played                    bigint not null,
  constraint pk_song primary key (id))
;

create table queue (
  id                        bigint not null,
  id_song                   bigint not null,
  prequeued                 bigint,
  constraint pk_queue primary key (id))
;

create table playlist (
  id                        bigint not null,
  name                      varchar(1000) not null,
  likeit                    bigint not null,
  dontlikeit                bigint not null,
  played                    bigint not null,
  constraint pk_playlist primary key (id))
;

create table playlistsongs (
  id_playlist               bigint not null,
  id_song                   bigint not null,
  constraint pk_playlistsongs primary key (id_playlist))
;

create sequence song_seq;
create sequence queue_seq;
create sequence playlist_seq;
create sequence playlistsongs_seq;


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists song;
drop table if exists queue;
drop table if exists playlist;
drop table if exists playlistsongs;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists song_seq;
drop sequence if exists queue_seq;
drop sequence if exists playlist_seq;
drop sequence if exists playlistsongs_seq;
