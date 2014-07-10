use test;

drop table if exists coefs;
drop table if exists rawlog;

create table rawlog(trantype integer, start integer, duration integer);
create table coefs(windowid integer, trantype integer, trancount integer, durationavg real);

load data infile 'UVW/coefsXYZ.log' into table rawlog fields terminated by ',' lines terminated by '\n' ignore 3 lines;

insert into coefs select floor(start/1000000), trantype, count(*), avg(duration) from rawlog group by  floor( start/1000000),trantype;

SELECT * INTO OUTFILE 'UVW/coefsXYZ.raw' FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' FROM coefs ORDER BY windowid asc, trantype asc;

drop table if exists coefs;
drop table if exists rawlog;


18 1 20 20 20 20 20 0 0 0 0 0 0
18 1 20 20 20 20 20 0 0 0 0 0 0
