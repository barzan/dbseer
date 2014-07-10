use test;

drop table if exists coefs;
drop table if exists rawlog;

create table rawlog(trantype integer, start integer, duration integer);
create table coefs(windowid integer, trantype integer, trancount integer);

load data infile 'ABC/coefs-XYZ' into table rawlog fields terminated by ',' lines terminated by '\n' ignore 4 lines;

insert into coefs select floor(start/1000000), trantype, count(*) from rawlog group by  floor( start/1000000),trantype;

SELECT * INTO OUTFILE 'UVW/coefs-XYZ.raw' FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' FROM coefs ORDER BY windowid asc, trantype asc;

drop table if exists coefs;
drop table if exists rawlog;


18 1 20 20 20 20 20 0 0 0 0 0 0
18 1 20 20 20 20 20 0 0 0 0 0 0
