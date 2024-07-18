create table tl_posts (
	id serial primary key,
	title text,
	img_urls text,
	attachment text,
	html_reader_type text,
	exception text,
	created timestamp without time zone not null,
	viewed timestamp without time zone,
	rank integer,
	category integer,
	flagged boolean,
	saved boolean
);

create table tl_clips (
	id serial primary key,
	content text,
	created timestamp without time zone not null
);

create table tl_emails (
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

create table tl_images (
    id serial primary key,
	url text,
	created timestamp without time zone not null
);

create table tl_ktv_songs (
    id serial primary key,
	title text,
	requested boolean
);

create table tl_spending_accounts (
    id serial primary key,
	name text,
	identifier text,
	icon text,
	owner text
);

create table tl_spending_transactions (
    id serial primary key,
    account_id integer references tl_spending_accounts,
    identifier text UNIQUE not null,
	name text,
	amount text,
	category text,
	location text,
	date date
);

CREATE TABLE logs (
  id serial primary key,
  service text not null,
  level text not null,
  source text,
  message text not null,
  stacktrace text,
  created timestamp without time zone not null
);

CREATE TABLE configurations (
  key text primary key,
  value text not null
);

CREATE INDEX logs_service_index ON logs (service);
CREATE INDEX logs_level_index ON logs (level);
CREATE INDEX logs_created_index ON logs (created);
