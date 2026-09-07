-- Baseline schema for vinyl-exchange (Flyway V1).
-- Source: pg_dump --schema-only against live postgres-local (public schema),
--         captured after Hibernate ddl-auto materialized current entities.
-- Manual corrections vs raw dump:
--   1. refund_review_required: added DEFAULT false
--   2. payment_transactions status CHECK renamed to chk_payment_transactions_status
--   3. legacy public.listing (singular) table omitted — dead schema; app uses listings only
--   4. NOT NULL booleans: added DEFAULT false where entity/seed semantics are "off/unset"
--   5. Descriptive enums: provider DEFAULT LOCAL; country and media_info_format stay strict NOT NULL (no default)
--   6. CREATE SCHEMA public changed to CREATE SCHEMA IF NOT EXISTS public, so this
--      applies cleanly against providers where public already exists (e.g. Neon)
--   7. Squashed V2 (orphaned_cloud_assets) into this baseline, no migration has run
--      against any real database yet so this is safe, done once before first deploy
--   8. All PK/FK/UNIQUE/CHECK constraints inline in CREATE TABLE; dependency-ordered tables
--   9. All constraint names normalized to Postgres suffix convention
--      (table_column_fkey/_key/_check/_pkey); user_roles.roles_id renamed to role_id
--      with column order matching User.java @JoinTable declaration
-- Excludes: flyway_schema_history, pg_dump restrict markers, owners/privileges, public.listing.

--
-- PostgreSQL database dump
--


-- Dumped from database version 17.10 (Debian 17.10-1.pgdg13+1)
-- Dumped by pg_dump version 17.10 (Debian 17.10-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA IF NOT EXISTS public;


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'standard public schema';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    payout_info_verified boolean NOT NULL DEFAULT false,
    activated_at timestamp(6) without time zone,
    banned_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    deactivated_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    suspended_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    email character varying(255),
    iban_for_payout character varying(255),
    legal_name character varying(255),
    password character varying(255),
    public_id character varying(12) NOT NULL,
    status character varying(255) NOT NULL,
    username character varying(255),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACTIVE'::character varying, 'INACTIVE'::character varying, 'SUSPENDED'::character varying, 'BANNED'::character varying, 'DELETED'::character varying])::text[]))),
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT users_username_key UNIQUE (username),
    CONSTRAINT users_public_id_key UNIQUE (public_id)
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id uuid NOT NULL,
    name character varying(20) NOT NULL,
    description character varying(100),
    CONSTRAINT roles_name_check CHECK (((name)::text = ANY ((ARRAY['ROLE_USER'::character varying, 'ROLE_ADMIN'::character varying, 'ROLE_MODERATOR'::character varying])::text[]))),
    CONSTRAINT roles_pkey PRIMARY KEY (id),
    CONSTRAINT roles_name_key UNIQUE (name)
);


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_roles (
    user_id uuid NOT NULL,
    role_id uuid NOT NULL,
    CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id),
    CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id),
    CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);


--
-- Name: genres_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.genres_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: genres; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.genres (
    featured boolean NOT NULL DEFAULT false,
    local_flavor boolean NOT NULL DEFAULT false,
    id bigint NOT NULL,
    parent_id bigint,
    name character varying(255) NOT NULL,
    CONSTRAINT genres_pkey PRIMARY KEY (id),
    CONSTRAINT genres_name_key UNIQUE (name),
    CONSTRAINT genres_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.genres(id)
);


--
-- Name: listings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.listings (
    media_info_colored boolean,
    media_info_disc_count integer,
    media_info_picture_disc boolean,
    media_info_speed_rpm integer,
    on_hold boolean NOT NULL DEFAULT false,
    platform_fee_bp integer NOT NULL,
    promote boolean NOT NULL DEFAULT false,
    needs_image_migration boolean NOT NULL DEFAULT false,
    stock_quantity integer NOT NULL,
    track_count integer,
    tradeable boolean,
    year integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    original_price_kurus bigint NOT NULL,
    platform_cut_kurus bigint NOT NULL,
    price_kurus bigint NOT NULL,
    price_last_changed_at timestamp(6) without time zone,
    promoted_at timestamp(6) without time zone,
    seller_earnings_kurus bigint NOT NULL,
    trade_value bigint NOT NULL DEFAULT 0,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    mb_id uuid,
    owner_id uuid,
    promoted_by_id uuid,
    country character varying(30) NOT NULL,
    artist_id character varying(255),
    artist_name character varying(255),
    barcode character varying(255),
    condition character varying(255),
    description character varying(255),
    label_name character varying(255),
    main_image_url character varying(255),
    media_info_format character varying(255) NOT NULL,
    media_info_source_format_raw character varying(255),
    media_info_vinyl_size character varying(255),
    media_info_vinyl_subtype character varying(255),
    packaging character varying(255),
    preferred_shipment_company character varying(255),
    public_id character varying(12) NOT NULL,
    promoted_by character varying(255),
    sale_type character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    title character varying(255),
    CONSTRAINT listings_country_check CHECK (((country)::text = ANY ((ARRAY['ANDORRA'::character varying, 'UNITED_ARAB_EMIRATES'::character varying, 'AFGHANISTAN'::character varying, 'ANTIGUA_BARBUDA'::character varying, 'ANGUILLA'::character varying, 'ALBANIA'::character varying, 'ARMENIA'::character varying, 'ANGOLA'::character varying, 'ANTARCTICA'::character varying, 'ARGENTINA'::character varying, 'AMERICAN_SAMOA'::character varying, 'AUSTRIA'::character varying, 'AUSTRALIA'::character varying, 'ARUBA'::character varying, 'ALAND_ISLANDS'::character varying, 'AZERBAIJAN'::character varying, 'BOSNIA_HERZEGOVINA'::character varying, 'BARBADOS'::character varying, 'BANGLADESH'::character varying, 'BELGIUM'::character varying, 'BURKINA_FASO'::character varying, 'BULGARIA'::character varying, 'BAHRAIN'::character varying, 'BURUNDI'::character varying, 'BENIN'::character varying, 'ST_BARTHELEMY'::character varying, 'BERMUDA'::character varying, 'BRUNEI'::character varying, 'BOLIVIA'::character varying, 'CARIBBEAN_NETHERLANDS'::character varying, 'BRAZIL'::character varying, 'BAHAMAS'::character varying, 'BHUTAN'::character varying, 'BOUVET_ISLAND'::character varying, 'BOTSWANA'::character varying, 'BELARUS'::character varying, 'BELIZE'::character varying, 'CANADA'::character varying, 'COCOS_KEELING_ISLANDS'::character varying, 'CONGO_KINSHASA'::character varying, 'CENTRAL_AFRICAN_REPUBLIC'::character varying, 'CONGO_BRAZZAVILLE'::character varying, 'SWITZERLAND'::character varying, 'COTE_D_IVOIRE'::character varying, 'COOK_ISLANDS'::character varying, 'CHILE'::character varying, 'CAMEROON'::character varying, 'CHINA'::character varying, 'COLOMBIA'::character varying, 'COSTA_RICA'::character varying, 'CUBA'::character varying, 'CAPE_VERDE'::character varying, 'CURACAO'::character varying, 'CHRISTMAS_ISLAND'::character varying, 'CYPRUS'::character varying, 'CZECHIA'::character varying, 'GERMANY'::character varying, 'DJIBOUTI'::character varying, 'DENMARK'::character varying, 'DOMINICA'::character varying, 'DOMINICAN_REPUBLIC'::character varying, 'ALGERIA'::character varying, 'ECUADOR'::character varying, 'ESTONIA'::character varying, 'EGYPT'::character varying, 'WESTERN_SAHARA'::character varying, 'ERITREA'::character varying, 'SPAIN'::character varying, 'ETHIOPIA'::character varying, 'FINLAND'::character varying, 'FIJI'::character varying, 'FALKLAND_ISLANDS'::character varying, 'MICRONESIA'::character varying, 'FAROE_ISLANDS'::character varying, 'FRANCE'::character varying, 'GABON'::character varying, 'UNITED_KINGDOM'::character varying, 'GRENADA'::character varying, 'GEORGIA'::character varying, 'FRENCH_GUIANA'::character varying, 'GUERNSEY'::character varying, 'GHANA'::character varying, 'GIBRALTAR'::character varying, 'GREENLAND'::character varying, 'GAMBIA'::character varying, 'GUINEA'::character varying, 'GUADELOUPE'::character varying, 'EQUATORIAL_GUINEA'::character varying, 'GREECE'::character varying, 'SOUTH_GEORGIA_SOUTH_SANDWICH_ISLANDS'::character varying, 'GUATEMALA'::character varying, 'GUAM'::character varying, 'GUINEA_BISSAU'::character varying, 'GUYANA'::character varying, 'HONG_KONG_SAR_CHINA'::character varying, 'HEARD_MCDONALD_ISLANDS'::character varying, 'HONDURAS'::character varying, 'CROATIA'::character varying, 'HAITI'::character varying, 'HUNGARY'::character varying, 'INDONESIA'::character varying, 'IRELAND'::character varying, 'ISRAEL'::character varying, 'ISLE_OF_MAN'::character varying, 'INDIA'::character varying, 'BRITISH_INDIAN_OCEAN_TERRITORY'::character varying, 'IRAQ'::character varying, 'IRAN'::character varying, 'ICELAND'::character varying, 'ITALY'::character varying, 'JERSEY'::character varying, 'JAMAICA'::character varying, 'JORDAN'::character varying, 'JAPAN'::character varying, 'KENYA'::character varying, 'KYRGYZSTAN'::character varying, 'CAMBODIA'::character varying, 'KIRIBATI'::character varying, 'COMOROS'::character varying, 'ST_KITTS_NEVIS'::character varying, 'NORTH_KOREA'::character varying, 'SOUTH_KOREA'::character varying, 'KUWAIT'::character varying, 'CAYMAN_ISLANDS'::character varying, 'KAZAKHSTAN'::character varying, 'LAOS'::character varying, 'LEBANON'::character varying, 'ST_LUCIA'::character varying, 'LIECHTENSTEIN'::character varying, 'SRI_LANKA'::character varying, 'LIBERIA'::character varying, 'LESOTHO'::character varying, 'LITHUANIA'::character varying, 'LUXEMBOURG'::character varying, 'LATVIA'::character varying, 'LIBYA'::character varying, 'MOROCCO'::character varying, 'MONACO'::character varying, 'MOLDOVA'::character varying, 'MONTENEGRO'::character varying, 'ST_MARTIN'::character varying, 'MADAGASCAR'::character varying, 'MARSHALL_ISLANDS'::character varying, 'NORTH_MACEDONIA'::character varying, 'MALI'::character varying, 'MYANMAR_BURMA'::character varying, 'MONGOLIA'::character varying, 'MACAO_SAR_CHINA'::character varying, 'NORTHERN_MARIANA_ISLANDS'::character varying, 'MARTINIQUE'::character varying, 'MAURITANIA'::character varying, 'MONTSERRAT'::character varying, 'MALTA'::character varying, 'MAURITIUS'::character varying, 'MALDIVES'::character varying, 'MALAWI'::character varying, 'MEXICO'::character varying, 'MALAYSIA'::character varying, 'MOZAMBIQUE'::character varying, 'NAMIBIA'::character varying, 'NEW_CALEDONIA'::character varying, 'NIGER'::character varying, 'NORFOLK_ISLAND'::character varying, 'NIGERIA'::character varying, 'NICARAGUA'::character varying, 'NETHERLANDS'::character varying, 'NORWAY'::character varying, 'NEPAL'::character varying, 'NAURU'::character varying, 'NIUE'::character varying, 'NEW_ZEALAND'::character varying, 'OMAN'::character varying, 'PANAMA'::character varying, 'PERU'::character varying, 'FRENCH_POLYNESIA'::character varying, 'PAPUA_NEW_GUINEA'::character varying, 'PHILIPPINES'::character varying, 'PAKISTAN'::character varying, 'POLAND'::character varying, 'ST_PIERRE_MIQUELON'::character varying, 'PITCAIRN_ISLANDS'::character varying, 'PUERTO_RICO'::character varying, 'PALESTINIAN_TERRITORIES'::character varying, 'PORTUGAL'::character varying, 'PALAU'::character varying, 'PARAGUAY'::character varying, 'QATAR'::character varying, 'REUNION'::character varying, 'ROMANIA'::character varying, 'SERBIA'::character varying, 'RUSSIA'::character varying, 'RWANDA'::character varying, 'SAUDI_ARABIA'::character varying, 'SOLOMON_ISLANDS'::character varying, 'SEYCHELLES'::character varying, 'SUDAN'::character varying, 'SWEDEN'::character varying, 'SINGAPORE'::character varying, 'ST_HELENA'::character varying, 'SLOVENIA'::character varying, 'SVALBARD_JAN_MAYEN'::character varying, 'SLOVAKIA'::character varying, 'SIERRA_LEONE'::character varying, 'SAN_MARINO'::character varying, 'SENEGAL'::character varying, 'SOMALIA'::character varying, 'SURINAME'::character varying, 'SOUTH_SUDAN'::character varying, 'SAO_TOME_PRINCIPE'::character varying, 'EL_SALVADOR'::character varying, 'SINT_MAARTEN'::character varying, 'SYRIA'::character varying, 'ESWATINI'::character varying, 'TURKS_CAICOS_ISLANDS'::character varying, 'CHAD'::character varying, 'FRENCH_SOUTHERN_TERRITORIES'::character varying, 'TOGO'::character varying, 'THAILAND'::character varying, 'TAJIKISTAN'::character varying, 'TOKELAU'::character varying, 'TIMOR_LESTE'::character varying, 'TURKMENISTAN'::character varying, 'TUNISIA'::character varying, 'TONGA'::character varying, 'TURKEY'::character varying, 'TRINIDAD_TOBAGO'::character varying, 'TUVALU'::character varying, 'TAIWAN'::character varying, 'TANZANIA'::character varying, 'UKRAINE'::character varying, 'UGANDA'::character varying, 'U_S_OUTLYING_ISLANDS'::character varying, 'UNITED_STATES'::character varying, 'URUGUAY'::character varying, 'UZBEKISTAN'::character varying, 'VATICAN_CITY'::character varying, 'ST_VINCENT_GRENADINES'::character varying, 'VENEZUELA'::character varying, 'BRITISH_VIRGIN_ISLANDS'::character varying, 'U_S_VIRGIN_ISLANDS'::character varying, 'VIETNAM'::character varying, 'VANUATU'::character varying, 'WALLIS_FUTUNA'::character varying, 'SAMOA'::character varying, 'YEMEN'::character varying, 'MAYOTTE'::character varying, 'SOUTH_AFRICA'::character varying, 'ZAMBIA'::character varying, 'ZIMBABWE'::character varying])::text[]))),
    CONSTRAINT listings_media_info_format_check CHECK (((media_info_format)::text = ANY ((ARRAY['VINYL'::character varying, 'CASSETTE'::character varying, 'CD'::character varying, 'EIGHT_TRACK'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT listings_media_info_vinyl_subtype_check CHECK (((media_info_vinyl_subtype)::text = ANY ((ARRAY['LP'::character varying, 'EP'::character varying, 'SINGLE'::character varying, 'MAXI_SINGLE'::character varying])::text[]))),
    CONSTRAINT listings_sale_type_check CHECK (((sale_type)::text = ANY ((ARRAY['FIXED_PRICE'::character varying, 'TRADE'::character varying])::text[]))),
    CONSTRAINT listings_status_check CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'ARCHIVED'::character varying, 'SOLD'::character varying, 'RESERVED'::character varying, 'DELETED'::character varying])::text[]))),
    CONSTRAINT listings_pkey PRIMARY KEY (id),
    CONSTRAINT listings_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.users(id)
);


--
-- Name: listing_genre; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.listing_genre (
    genre_id bigint NOT NULL,
    listing_id uuid NOT NULL,
    CONSTRAINT listing_genre_pkey PRIMARY KEY (genre_id, listing_id),
    CONSTRAINT listing_genre_listing_id_fkey FOREIGN KEY (listing_id) REFERENCES public.listings(id),
    CONSTRAINT listing_genre_genre_id_fkey FOREIGN KEY (genre_id) REFERENCES public.genres(id)
);


--
-- Name: listing_images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.listing_images (
    "position" integer NOT NULL,
    uploaded_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    listing_id uuid NOT NULL,
    provider character varying(255) NOT NULL DEFAULT 'LOCAL'::character varying,
    public_id character varying(255),
    secure_url character varying(255),
    fallback_url character varying(255),
    CONSTRAINT listing_images_provider_check CHECK (((provider)::text = ANY ((ARRAY['CLOUDINARY'::character varying, 'LOCAL'::character varying, 'EXTERNAL'::character varying])::text[]))),
    CONSTRAINT listing_images_pkey PRIMARY KEY (id),
    CONSTRAINT listing_images_listing_id_fkey FOREIGN KEY (listing_id) REFERENCES public.listings(id)
);


--
-- Name: listing_price_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.listing_price_history (
    fee_bp_at_change integer NOT NULL,
    new_platform_cut_kurus bigint NOT NULL,
    new_price_kurus bigint NOT NULL,
    new_seller_earnings_kurus bigint NOT NULL,
    occurred_at timestamp(6) without time zone NOT NULL,
    old_platform_cut_kurus bigint,
    old_price_kurus bigint,
    old_seller_earnings_kurus bigint,
    id uuid NOT NULL,
    listing_id uuid NOT NULL,
    note character varying(500),
    changed_by character varying(255) NOT NULL,
    CONSTRAINT listing_price_history_pkey PRIMARY KEY (id)
);


--
-- Name: trade_prefs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.trade_prefs (
    extra_amount double precision NOT NULL,
    id uuid NOT NULL,
    listing_id uuid NOT NULL,
    desired_item character varying(255) NOT NULL,
    payment_direction character varying(255) NOT NULL,
    CONSTRAINT trade_prefs_payment_direction_check CHECK (((payment_direction)::text = ANY ((ARRAY['NO_EXTRA'::character varying, 'PAY'::character varying, 'RECEIVE'::character varying])::text[]))),
    CONSTRAINT trade_prefs_pkey PRIMARY KEY (id),
    CONSTRAINT trade_prefs_listing_id_fkey FOREIGN KEY (listing_id) REFERENCES public.listings(id)
);


--
-- Name: cart; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cart (
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT cart_pkey PRIMARY KEY (id),
    CONSTRAINT cart_user_id_key UNIQUE (user_id)
);


--
-- Name: cart_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cart_items (
    committed boolean NOT NULL DEFAULT true,
    order_quantity integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    cart_id uuid NOT NULL,
    cart_item_id uuid NOT NULL,
    listing_id uuid NOT NULL,
    CONSTRAINT cart_items_order_quantity_check CHECK ((order_quantity >= 1)),
    CONSTRAINT cart_items_pkey PRIMARY KEY (cart_item_id),
    CONSTRAINT cart_items_cart_id_listing_id_key UNIQUE (cart_id, listing_id),
    CONSTRAINT cart_items_cart_id_fkey FOREIGN KEY (cart_id) REFERENCES public.cart(id)
);


--
-- Name: favorites; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.favorites (
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    listing_id uuid NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT favorites_pkey PRIMARY KEY (id),
    CONSTRAINT favorites_user_id_listing_id_key UNIQUE (user_id, listing_id)
);


--
-- Name: conversations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conversations (
    created_at timestamp(6) without time zone NOT NULL,
    initiator_unread_count bigint NOT NULL,
    last_message_at timestamp(6) without time zone NOT NULL,
    participant_unread_count bigint NOT NULL,
    id uuid NOT NULL,
    initiator_id uuid NOT NULL,
    participant_id uuid NOT NULL,
    related_listing_id uuid NOT NULL,
    public_id character varying(21) NOT NULL,
    initiator_username character varying(255) NOT NULL,
    participant_username character varying(255) NOT NULL,
    CONSTRAINT conversations_initiator_id_participant_id_related_listing_id_key UNIQUE (initiator_id, participant_id, related_listing_id),
    CONSTRAINT conversations_public_id_key UNIQUE (public_id),
    CONSTRAINT conversations_pkey PRIMARY KEY (id)
);


--
-- Name: messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages (
    is_read boolean NOT NULL DEFAULT false,
    id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    "timestamp" timestamp(6) without time zone NOT NULL,
    conversation_id uuid NOT NULL,
    receiver_id uuid,
    sender_id uuid NOT NULL,
    content character varying(2000) NOT NULL,
    message_type character varying(255) NOT NULL,
    receiver_username character varying(255),
    sender_username character varying(255),
    CONSTRAINT messages_message_type_check CHECK (((message_type)::text = ANY ((ARRAY['TEXT'::character varying, 'TRADE_OFFER'::character varying, 'TRADE_ACCEPTED'::character varying, 'TRADE_DECLINED'::character varying, 'SYSTEM'::character varying])::text[]))),
    CONSTRAINT messages_pkey PRIMARY KEY (id)
);


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    is_read boolean NOT NULL DEFAULT false,
    created_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    related_listing uuid,
    user_id uuid,
    message character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    type character varying(255),
    CONSTRAINT notifications_type_check CHECK (((type)::text = ANY ((ARRAY['INFO'::character varying, 'WARNING'::character varying, 'ALERT'::character varying, 'LISTING_SOLD'::character varying, 'WISHLIST_ITEM_AVAILABLE'::character varying, 'ORDER'::character varying])::text[]))),
    CONSTRAINT notifications_pkey PRIMARY KEY (id)
);


--
-- Name: order_number_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.order_number_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.orders (
    payout_pending boolean NOT NULL DEFAULT false,
    auto_confirm_deadline timestamp(6) without time zone,
    shipment_label_generated_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    delivered_at timestamp(6) without time zone,
    expected_delivery_date timestamp(6) without time zone,
    order_number bigint NOT NULL,
    paid_at timestamp(6) without time zone,
    payment_expires_at timestamp(6) without time zone,
    payout_sent_at timestamp(6) without time zone,
    platform_cut bigint,
    seller_earnings bigint,
    shipping_deadline timestamp(6) without time zone,
    total_price bigint,
    updated_at timestamp(6) without time zone NOT NULL,
    buyer_id uuid NOT NULL,
    id uuid NOT NULL,
    seller_id uuid NOT NULL,
    shipment_barcode character varying(255),
    shipment_handler_code character varying(255),
    shipment_label_url character varying(255),
    shipment_order_id character varying(255),
    shipment_tracking_number character varying(255),
    shipping_address_snapshot text,
    billing_address_snapshot text,
    seller_address_snapshot text,
    payout_sent_by character varying(255),
    sale_type character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT orders_sale_type_check CHECK (((sale_type)::text = ANY ((ARRAY['FIXED_PRICE'::character varying, 'TRADE'::character varying])::text[]))),
    CONSTRAINT orders_status_check CHECK (((status)::text = ANY ((ARRAY['AWAITING_PAYMENT'::character varying, 'PAID'::character varying, 'AWAITING_SHIPMENT'::character varying, 'SHIPPED'::character varying, 'IN_TRANSIT'::character varying, 'OUT_FOR_DELIVERY'::character varying, 'DELIVERED'::character varying, 'DISPUTED'::character varying, 'COMPLETED'::character varying, 'RETURNING'::character varying, 'RETURNED'::character varying, 'REFUNDED'::character varying, 'CANCELLED'::character varying, 'LOST'::character varying])::text[]))),
    CONSTRAINT orders_pkey PRIMARY KEY (id),
    CONSTRAINT orders_order_number_key UNIQUE (order_number)
);


--
-- Name: order_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_items (
    quantity integer,
    created_at timestamp(6) without time zone NOT NULL,
    sub_total bigint,
    unit_price bigint,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    listing_id uuid,
    order_id uuid,
    seller_id uuid,
    listing_main_image_url character varying(255),
    listing_title character varying(255),
    CONSTRAINT order_items_pkey PRIMARY KEY (id),
    CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id)
);


--
-- Name: order_status_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_status_history (
    occurred_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    order_id uuid NOT NULL,
    note character varying(500),
    from_status character varying(255),
    to_status character varying(255) NOT NULL,
    triggered_by character varying(255) NOT NULL,
    CONSTRAINT order_status_history_from_status_check CHECK (((from_status)::text = ANY ((ARRAY['AWAITING_PAYMENT'::character varying, 'PAID'::character varying, 'AWAITING_SHIPMENT'::character varying, 'SHIPPED'::character varying, 'IN_TRANSIT'::character varying, 'OUT_FOR_DELIVERY'::character varying, 'DELIVERED'::character varying, 'DISPUTED'::character varying, 'COMPLETED'::character varying, 'RETURNING'::character varying, 'RETURNED'::character varying, 'REFUNDED'::character varying, 'CANCELLED'::character varying, 'LOST'::character varying])::text[]))),
    CONSTRAINT order_status_history_to_status_check CHECK (((to_status)::text = ANY ((ARRAY['AWAITING_PAYMENT'::character varying, 'PAID'::character varying, 'AWAITING_SHIPMENT'::character varying, 'SHIPPED'::character varying, 'IN_TRANSIT'::character varying, 'OUT_FOR_DELIVERY'::character varying, 'DELIVERED'::character varying, 'DISPUTED'::character varying, 'COMPLETED'::character varying, 'RETURNING'::character varying, 'RETURNED'::character varying, 'REFUNDED'::character varying, 'CANCELLED'::character varying, 'LOST'::character varying])::text[]))),
    CONSTRAINT order_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT order_status_history_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id)
);


--
-- Name: cancel_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cancel_requests (
    created_at timestamp(6) without time zone NOT NULL,
    dispute_window_deadline timestamp(6) without time zone,
    requested_at timestamp(6) without time zone,
    reviewed_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    order_id uuid NOT NULL,
    requested_by uuid,
    reviewed_by uuid,
    reason character varying(500),
    review_note character varying(500),
    dispute_reason character varying(255),
    status character varying(255) NOT NULL,
    CONSTRAINT cancel_requests_dispute_reason_check CHECK (((dispute_reason)::text = ANY ((ARRAY['ITEM_NOT_RECEIVED'::character varying, 'ITEM_NOT_AS_DESCRIBED'::character varying, 'ITEM_DAMAGED'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT cancel_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'WITHDRAWN'::character varying])::text[]))),
    CONSTRAINT cancel_requests_pkey PRIMARY KEY (id),
    CONSTRAINT cancel_requests_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id)
);


--
-- Name: payment_transactions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payment_transactions (
    fraud_status integer,
    refund_review_required boolean NOT NULL DEFAULT false,
    amount_kurus bigint NOT NULL,
    auto_confirm_deadline timestamp(6) without time zone,
    captured_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    payout_approved_at timestamp(6) without time zone,
    shipped_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    order_id uuid NOT NULL,
    seller_id uuid NOT NULL,
    dispute_reason character varying(500),
    dispute_resolution_note character varying(500),
    auth_code character varying(255),
    host_reference character varying(255),
    provider_checkout_token character varying(255),
    provider_internal_payment_id character varying(255),
    provider_payout_id character varying(255),
    status character varying(255) NOT NULL,
    CONSTRAINT payment_transactions_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING_PAYMENT'::character varying, 'HELD'::character varying, 'RELEASED'::character varying, 'COMPLETED'::character varying, 'REFUNDED'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT payment_transactions_pkey PRIMARY KEY (id),
    CONSTRAINT payment_transactions_order_id_key UNIQUE (order_id),
    CONSTRAINT payment_transactions_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id)
);


--
-- Name: payment_status_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payment_status_history (
    occurred_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    payment_transaction_id uuid NOT NULL,
    note character varying(500),
    from_status character varying(255),
    to_status character varying(255) NOT NULL,
    triggered_by character varying(255) NOT NULL,
    CONSTRAINT payment_status_history_from_status_check CHECK (((from_status)::text = ANY ((ARRAY['PENDING_PAYMENT'::character varying, 'HELD'::character varying, 'RELEASED'::character varying, 'COMPLETED'::character varying, 'REFUNDED'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT payment_status_history_to_status_check CHECK (((to_status)::text = ANY ((ARRAY['PENDING_PAYMENT'::character varying, 'HELD'::character varying, 'RELEASED'::character varying, 'COMPLETED'::character varying, 'REFUNDED'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT payment_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT payment_status_history_payment_transaction_id_fkey FOREIGN KEY (payment_transaction_id) REFERENCES public.payment_transactions(id)
);


--
-- Name: user_addresses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_addresses (
    is_default boolean NOT NULL DEFAULT false,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    label character varying(255) NOT NULL,
    full_name text NOT NULL,
    phone text NOT NULL,
    address_line text NOT NULL,
    district text NOT NULL,
    city text NOT NULL,
    postal_code text NOT NULL,
    country character varying(255) NOT NULL DEFAULT 'TR',
    address_type character varying(255) NOT NULL,
    CONSTRAINT user_addresses_pkey PRIMARY KEY (id),
    CONSTRAINT user_addresses_address_type_check CHECK (((address_type)::text = ANY ((ARRAY['SHIPPING'::character varying, 'BILLING'::character varying])::text[]))),
    CONSTRAINT user_addresses_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);


--
-- Name: user_status_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_status_history (
    changed_at timestamp(6) without time zone NOT NULL,
    changed_by_id uuid NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    changed_by character varying(255) NOT NULL,
    metadata character varying(255),
    previous_status character varying(255),
    reason character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT user_status_history_previous_status_check CHECK (((previous_status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACTIVE'::character varying, 'INACTIVE'::character varying, 'SUSPENDED'::character varying, 'BANNED'::character varying, 'DELETED'::character varying])::text[]))),
    CONSTRAINT user_status_history_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACTIVE'::character varying, 'INACTIVE'::character varying, 'SUSPENDED'::character varying, 'BANNED'::character varying, 'DELETED'::character varying])::text[]))),
    CONSTRAINT user_status_history_pkey PRIMARY KEY (id)
);


--
-- Name: wishlist_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.wishlist_items (
    year integer,
    added_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    user_id uuid,
    artist character varying(255),
    barcode character varying(255),
    country character varying(255),
    external_cover_url character varying(255),
    format character varying(255),
    label character varying(255),
    title character varying(255),
    CONSTRAINT wishlist_items_pkey PRIMARY KEY (id),
    CONSTRAINT wishlist_items_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);


--
-- Name: pages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pages (
    id uuid NOT NULL,
    background_color character varying(255),
    background_image_path character varying(255),
    header character varying(255),
    page_type character varying(255),
    text_content_path character varying(255),
    CONSTRAINT pages_page_type_check CHECK (((page_type)::text = ANY ((ARRAY['ABOUT'::character varying, 'CONTACT'::character varying])::text[]))),
    CONSTRAINT pages_pkey PRIMARY KEY (id)
);


-- Orphaned Cloudinary assets (no FK — survives listing/listing_image deletion).

--
-- Name: orphaned_cloud_assets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.orphaned_cloud_assets (
    created_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    public_id character varying(255) NOT NULL,
    CONSTRAINT orphaned_cloud_assets_pkey PRIMARY KEY (id)
);


--
-- Name: idx_listing_genre_genre_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_listing_genre_genre_id ON public.listing_genre USING btree (genre_id);


--
-- Name: idx_listing_genre_listing_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_listing_genre_listing_id ON public.listing_genre USING btree (listing_id);


--
-- Name: idx_listings_condition; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_listings_condition ON public.listings USING btree (condition);


--
-- Name: idx_listings_country; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_listings_country ON public.listings USING btree (country);


--
-- Name: idx_listings_media_info_format; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_listings_media_info_format ON public.listings USING btree (media_info_format);


--
-- Name: idx_listings_price_kurus; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_listings_price_kurus ON public.listings USING btree (price_kurus);


--
-- Name: idx_listings_year; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_listings_year ON public.listings USING btree (year);


--
-- Name: idx_listings_public_id; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_listings_public_id ON public.listings USING btree (public_id);


--
-- Name: idx_user_address_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_address_user_id ON public.user_addresses USING btree (user_id);


--
-- PostgreSQL database dump complete
--

