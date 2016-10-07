create table task (
  id                            integer auto_increment not null,
  work_day_id                   integer not null,
  task_id                       varchar(255),
  start_time                    time,
  end_time                      time,
  comment                       varchar(255),
  min_per_task                  bigint,
  constraint pk_task primary key (id)
);

create table time_logger (
  id                            integer auto_increment not null,
  constraint pk_time_logger primary key (id)
);

create table work_day (
  id                            integer auto_increment not null,
  work_month_id                 integer not null,
  required_min_per_day          bigint,
  extra_min_per_day             bigint,
  actual_day                    date,
  sum_per_day                   bigint,
  constraint pk_work_day primary key (id)
);

create table work_month (
  id                            integer auto_increment not null,
  time_logger_id                integer not null,
  sum_per_month                 bigint,
  required_min_per_month        bigint,
  extra_min_per_month           bigint,
  month_date                    varchar(255),
  constraint pk_work_month primary key (id)
);

alter table task add constraint fk_task_work_day_id foreign key (work_day_id) references work_day (id) on delete restrict on update restrict;
create index ix_task_work_day_id on task (work_day_id);

alter table work_day add constraint fk_work_day_work_month_id foreign key (work_month_id) references work_month (id) on delete restrict on update restrict;
create index ix_work_day_work_month_id on work_day (work_month_id);

alter table work_month add constraint fk_work_month_time_logger_id foreign key (time_logger_id) references time_logger (id) on delete restrict on update restrict;
create index ix_work_month_time_logger_id on work_month (time_logger_id);

