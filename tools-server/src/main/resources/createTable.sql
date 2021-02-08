create table tl_posts (
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

create table tl_crl_equipments (
	id serial primary key,
	name text,
    description text,
	picture text,
	serial_number text,
	created timestamp without time zone not null,
	borrower text
);

create table tl_crl_borrower_logs (
	id serial primary key,
	equipment_id integer references tl_crl_equipments,
	name text,
	ut_eid text,
	borrow_date timestamp without time zone,
	return_date timestamp without time zone
);

create table tl_discord_guilds (
	id text primary key,
	name text,
	welcome_enabled boolean,
	welcome_title text,
    welcome_description text,
    welcome_thumbnail text,
    welcome_footer text,
    welcome_color text,
    welcome_channel_id text,
    welcome_role_id text,
    debug_channel_id text,
	birthday_enabled boolean,
	birthday_message text,
	birthday_role_id text,
	birthday_channel_id text,
	role_enabled boolean,
	role_level_requirement integer,
	role_name_blacklist text,
	role_color_blacklist text,
	role_level_rank_role_id text,
	role_boost_rank_role_id text
);

create table tl_discord_users (
	id text primary key,
	name text,
	guild_id text,
	nickname text,
	avatar_id text,
	roles text,
	created_date timestamp without time zone,
	joined_date timestamp without time zone,
	boosted_date timestamp without time zone,
	apex_id text,
	birthday text
);

create table tl_discord_roles (
	id text primary key,
	guild_id text,
	name text,
	color text,
	position integer,
	created timestamp without time zone
);

create table tl_discord_user_logs (
    id serial primary key,
	guild_id text,
	user_id text,
	name text,
	action text,
	created timestamp without time zone not null
);

create table tl_discord_role_mappings (
    id serial primary key,
	guild_id text,
	role_id text,
	enabled boolean,
	code text,
	type text,
	owner_id text,
	approver_id text,
	created timestamp without time zone not null
);

create table tl_images (
    id serial primary key,
	url text,
	created timestamp without time zone not null
);
