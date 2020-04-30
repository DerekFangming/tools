create table posts (
	id serial primary key,
	title text,
	img_urls text,
	attachment text,
	html_reader_type text,
	html text,
	exception text,
	created timestamp without time zone not null,
	viewed timestamp without time zone,
	rank integer,
	category integer,
	flagged boolean
);

create table clips (
	id serial primary key,
	content text,
	created timestamp without time zone not null
);

create table emails (
	id serial primary key,
	from text,
	to text,
	subject text,
	content text,
	html boolean,
	sender_type text,
	address text,
	headers jsonb,
	query_params text,
	created timestamp without time zone not null,
	error text
);