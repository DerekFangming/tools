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
	from_addr text,
	replacement_from_addr text,
	to_addr text,
	subject text,
	content text,
	html boolean,
	attachment text,
	sender_type text,
	replacement_sender_type text,
	request_address  text,
	request_headers jsonb,
	request_params jsonb,
	created timestamp without time zone not null,
	error text,
	read boolean default false
);
