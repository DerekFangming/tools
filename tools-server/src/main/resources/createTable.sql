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

create table crl_equipments (
	id serial primary key,
	name text,
    description text,
	picture text,
	serial_number text,
	created timestamp without time zone not null,
	borrower text
);

create table crl_borrower_logs (
	id serial primary key,
	equipment_id integer references crl_equipments,
	name text,
	ut_eid text,
	borrow_date timestamp without time zone,
	return_date timestamp without time zone
);
