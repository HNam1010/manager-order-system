--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: order_details; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.order_details (
    order_detail_id bigint NOT NULL,
    product_brand character varying(100),
    product_id bigint NOT NULL,
    product_image_path character varying(255),
    product_name character varying(255) NOT NULL,
    product_price numeric(19,4) NOT NULL,
    quantity integer NOT NULL,
    total_price numeric(19,4) NOT NULL,
    order_id bigint NOT NULL
);


ALTER TABLE public.order_details OWNER TO postgres;

--
-- Name: order_details_order_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.order_details_order_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.order_details_order_detail_id_seq OWNER TO postgres;

--
-- Name: order_details_order_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.order_details_order_detail_id_seq OWNED BY public.order_details.order_detail_id;


--
-- Name: order_status; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.order_status (
    serial_id integer NOT NULL,
    description text,
    status_code character varying(50) NOT NULL
);


ALTER TABLE public.order_status OWNER TO postgres;

--
-- Name: order_status_serial_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.order_status_serial_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.order_status_serial_id_seq OWNER TO postgres;

--
-- Name: order_status_serial_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.order_status_serial_id_seq OWNED BY public.order_status.serial_id;


--
-- Name: orders; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.orders (
    serial_id bigint NOT NULL,
    customer_name character varying(100) NOT NULL,
    email character varying(255),
    order_code character varying(50),
    order_date timestamp(6) with time zone NOT NULL,
    order_notes text,
    payment_method character varying(50) NOT NULL,
    phone_number character varying(20) NOT NULL,
    shipping_address text NOT NULL,
    total_amount numeric(19,4) NOT NULL,
    updated_at timestamp(6) with time zone,
    user_id bigint,
    status_id integer NOT NULL,
    guest_token character varying(36),
    guest_token_expires_at timestamp(6) with time zone
);


ALTER TABLE public.orders OWNER TO postgres;

--
-- Name: orders_serial_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.orders_serial_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.orders_serial_id_seq OWNER TO postgres;

--
-- Name: orders_serial_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.orders_serial_id_seq OWNED BY public.orders.serial_id;


--
-- Name: product_types; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.product_types (
    serial_id integer NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    name character varying(100) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL
);


ALTER TABLE public.product_types OWNER TO postgres;

--
-- Name: product_types_serial_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.product_types_serial_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.product_types_serial_id_seq OWNER TO postgres;

--
-- Name: product_types_serial_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.product_types_serial_id_seq OWNED BY public.product_types.serial_id;


--
-- Name: products; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.products (
    serial_id bigint NOT NULL,
    brand character varying(100),
    created_at timestamp(6) with time zone NOT NULL,
    description text,
    image_path character varying(255),
    name character varying(255) NOT NULL,
    price numeric(19,4) NOT NULL,
    quantity bigint NOT NULL,
    updated_at timestamp(6) with time zone,
    user_id bigint,
    product_type_id integer NOT NULL,
    size character varying(50)
);


ALTER TABLE public.products OWNER TO postgres;

--
-- Name: products_serial_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.products_serial_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.products_serial_id_seq OWNER TO postgres;

--
-- Name: products_serial_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.products_serial_id_seq OWNED BY public.products.serial_id;


--
-- Name: products_serial_id_seq1; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.products ALTER COLUMN serial_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.products_serial_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    serial_id integer NOT NULL,
    name character varying(50) NOT NULL,
    priority character varying(20)
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: roles_serial_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.roles_serial_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.roles_serial_id_seq OWNER TO postgres;

--
-- Name: roles_serial_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.roles_serial_id_seq OWNED BY public.roles.serial_id;


--
-- Name: shopping_carts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.shopping_carts (
    cart_item_id bigint NOT NULL,
    added_at timestamp(6) with time zone,
    guest_id character varying(255),
    product_id bigint NOT NULL,
    quantity integer NOT NULL,
    user_id bigint
);


ALTER TABLE public.shopping_carts OWNER TO postgres;

--
-- Name: shopping_carts_cart_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.shopping_carts_cart_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.shopping_carts_cart_item_id_seq OWNER TO postgres;

--
-- Name: shopping_carts_cart_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.shopping_carts_cart_item_id_seq OWNED BY public.shopping_carts.cart_item_id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    serial_id bigint NOT NULL,
    address text,
    birth_day character varying(50),
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    email character varying(255),
    pass_word character varying(255) NOT NULL,
    phone character varying(20),
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    user_name character varying(255) NOT NULL,
    role_id integer NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_serial_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_serial_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_serial_id_seq OWNER TO postgres;

--
-- Name: users_serial_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_serial_id_seq OWNED BY public.users.serial_id;


--
-- Name: order_details order_detail_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_details ALTER COLUMN order_detail_id SET DEFAULT nextval('public.order_details_order_detail_id_seq'::regclass);


--
-- Name: order_status serial_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_status ALTER COLUMN serial_id SET DEFAULT nextval('public.order_status_serial_id_seq'::regclass);


--
-- Name: orders serial_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders ALTER COLUMN serial_id SET DEFAULT nextval('public.orders_serial_id_seq'::regclass);


--
-- Name: product_types serial_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_types ALTER COLUMN serial_id SET DEFAULT nextval('public.product_types_serial_id_seq'::regclass);


--
-- Name: roles serial_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles ALTER COLUMN serial_id SET DEFAULT nextval('public.roles_serial_id_seq'::regclass);


--
-- Name: shopping_carts cart_item_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.shopping_carts ALTER COLUMN cart_item_id SET DEFAULT nextval('public.shopping_carts_cart_item_id_seq'::regclass);


--
-- Name: users serial_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN serial_id SET DEFAULT nextval('public.users_serial_id_seq'::regclass);


--
-- Data for Name: order_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.order_details VALUES (1, 'StyleCo', 3, '71a81e8b-048a-44ec-b983-d70a2a45270a.jpg', 'ÁO HOODIE NGẮN TAY NAM BASIC CAO CẤP', 95000.0000, 1, 95000.0000, 1);
INSERT INTO public.order_details VALUES (2, 'Nike ', 2, '0a4b59e9-131b-44cd-a8ae-c7d47269d899.jpg', 'Áo hoodie Nike chính hãng mũ zip bassic form rộng nam nữ unisex, khoác nỉ hoodie basic oversize LOIX', 250000.0000, 1, 250000.0000, 2);
INSERT INTO public.order_details VALUES (3, 'Unisex', 1, 'b7e4033d-32bd-494e-a2e4-7292cfe06e65.jpg', 'Áo Hoodie Nỉ Chữ Ngực Nhỏ ellon arc Form Rộng Tay Phồng, Áo Hoodie Màu Đen Unisex', 50000.0000, 1, 50000.0000, 3);
INSERT INTO public.order_details VALUES (4, 'Three PreFac', 8, '20e40a14-6e7b-426f-b237-f396f44d5d2f.jpg', 'Áo Khoác Hoodie Three PreFact 3 sao MỚI form rộng tay dài Unisex Oversize, Áo hoodie Nam Nữ 3 sao Thêu', 144000.0000, 1, 144000.0000, 3);
INSERT INTO public.order_details VALUES (5, 'StyleCo', 3, '71a81e8b-048a-44ec-b983-d70a2a45270a.jpg', 'ÁO HOODIE NGẮN TAY NAM BASIC CAO CẤP', 95000.0000, 1, 95000.0000, 4);
INSERT INTO public.order_details VALUES (6, 'ANYOUNG', 4, '7043e5cf-4e1a-419e-a6cf-37c3f4410d16.jpg', 'Áo hoodie nam nữ ANYOUNGcổ cao dáng rộng phối khóa thời trang AN 656', 199000.0000, 1, 199000.0000, 4);
INSERT INTO public.order_details VALUES (7, 'StyleCo', 3, '71a81e8b-048a-44ec-b983-d70a2a45270a.jpg', 'ÁO HOODIE NGẮN TAY NAM BASIC CAO CẤP', 95000.0000, 1, 95000.0000, 5);
INSERT INTO public.order_details VALUES (8, 'Three PreFac', 8, '20e40a14-6e7b-426f-b237-f396f44d5d2f.jpg', 'Áo Khoác Hoodie Three PreFact 3 sao MỚI form rộng tay dài Unisex Oversize, Áo hoodie Nam Nữ 3 sao Thêu', 144000.0000, 1, 144000.0000, 5);
INSERT INTO public.order_details VALUES (9, 'wwyn signature', 23, '29fb4b35-219c-4974-a324-9091ce6abaf8.jpg', '"XẢ KHO"Quần short dù WWYNSTU logo wwyn signature black / grey dù gió nhăn cao cấp 2 lớp hot nhất hiện nay', 20000.0000, 1, 20000.0000, 5);
INSERT INTO public.order_details VALUES (10, 'kaki ', 6, '8bb571ea-2650-4658-87d7-d7c4a1daa3aa.jpg', 'Áo khoác kaki nam xanh cao cấp (AKN-158)', 300000.0000, 1, 300000.0000, 6);
INSERT INTO public.order_details VALUES (11, 'kaki ', 6, '8bb571ea-2650-4658-87d7-d7c4a1daa3aa.jpg', 'Áo khoác kaki nam xanh cao cấp (AKN-158)', 300000.0000, 1, 300000.0000, 7);
INSERT INTO public.order_details VALUES (12, 'kaki ', 6, '8bb571ea-2650-4658-87d7-d7c4a1daa3aa.jpg', 'Áo khoác kaki nam xanh cao cấp (AKN-158)', 300000.0000, 1, 300000.0000, 8);


--
-- Data for Name: order_status; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.order_status VALUES (1, 'Chờ Xác Nhận', 'PENDING_CONFIRMATION');
INSERT INTO public.order_status VALUES (2, 'Đã Xác Nhận', 'CONFIRMED');
INSERT INTO public.order_status VALUES (3, 'Đang Giao Hàng', 'IN_DELIVERY');
INSERT INTO public.order_status VALUES (4, 'Hoàn Thành', 'COMPLETED');
INSERT INTO public.order_status VALUES (5, 'Đã Hủy', 'CANCELLED');
INSERT INTO public.order_status VALUES (6, 'Đã Chuyển Khoản', 'AW_BANK_TRANSFER');


--
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.orders VALUES (2, 'trần hoàng văn', 'gdfgdfgdf@gmail.com', 'OR1746350923619EC76A7', '2025-05-04 16:28:43.619502+07', 'giao lẹ ', 'BANK_TRANSFER', '0338630402', 'admin123', 250000.0000, '2025-05-04 16:28:43.619502+07', NULL, 1, '0db5b6b4-5009-4658-95aa-4da3f2b1330c', '2025-05-05 16:28:43.619502+07');
INSERT INTO public.orders VALUES (3, 'Trần nhung', 'ngohoangnam99999@gmail.com', 'OR1746350982452A36D30', '2025-05-04 16:29:42.452503+07', NULL, 'COD', '0338637895', 'P. Tân Thuận, Quận 7, tp HCM', 194000.0000, '2025-05-04 16:29:42.452503+07', NULL, 1, 'eabfa4fb-d871-4c05-8087-93eab6a3a664', '2025-05-05 16:29:42.452503+07');
INSERT INTO public.orders VALUES (4, 'Nguyễn Vĩnh', 'nguyenvinh@gmail.com', 'OR174635106791114826F', '2025-05-04 16:31:07.91109+07', NULL, 'BANK_TRANSFER', '0987541230', '5 ấp phú hiệp, hiệp xương ag', 294000.0000, '2025-05-04 16:31:07.91109+07', 3, 1, NULL, NULL);
INSERT INTO public.orders VALUES (5, 'nguyen ngân', 'nguyenngan@gmail.com', 'OR1746351101435CF5305', '2025-05-04 16:31:41.435499+07', NULL, 'COD', '0338630402', 'sdgfsdgdfbsd dfbdbd', 259000.0000, '2025-05-04 16:31:41.435499+07', 3, 1, NULL, NULL);
INSERT INTO public.orders VALUES (1, 'Ngohoangnam', 'nguuduan@gmail.com', 'OR17463508813425FF5C6', '2025-05-04 16:28:01.342058+07', 'cần mua gấp giao nhanh không thì boom hàng ', 'COD', '0338630402', 'admin123', 95000.0000, '2025-05-04 16:32:32.527755+07', NULL, 2, '681a87aa-3b89-4b28-893c-13dba23214e9', '2025-05-05 16:28:01.344063+07');
INSERT INTO public.orders VALUES (6, 'Ngohoangnam', 'ngohoangnam101002@gmail.com', 'OR1746440869853598E1B', '2025-05-05 17:27:49.853454+07', 'Giao nhanh giúp em', 'BANK_TRANSFER', '0338630402', '1999 tô 5 hiệp hưng, xã hiệp xương, phú tân AG', 300000.0000, '2025-05-05 17:29:11.236538+07', 1, 4, NULL, NULL);
INSERT INTO public.orders VALUES (7, 'Trần nhung', 'nguuduan@gmail.com', 'OR174646067625835971C', '2025-05-05 22:57:56.258099+07', 'giao lẹ ní', 'BANK_TRANSFER', '0338637895', 'P. Tân Thuận, Quận 7, tp HCM', 300000.0000, '2025-05-05 22:57:56.258099+07', 1, 1, NULL, NULL);
INSERT INTO public.orders VALUES (8, 'Nguyễn Duẫn', 'nguuduan@gmail.com', 'OR17464622225807AD558', '2025-05-05 23:23:42.580105+07', NULL, 'BANK_TRANSFER', '0338630402', 'P. Tân Thuận, Quận 7, tp HCM', 300000.0000, '2025-05-05 23:23:54.849218+07', 1, 2, NULL, NULL);


--
-- Data for Name: product_types; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.product_types VALUES (1, '2025-04-27 16:59:01.064217+07', 'Áo thun', '2025-04-27 16:59:01.064217+07');
INSERT INTO public.product_types VALUES (2, '2025-04-27 16:59:01.064217+07', 'Áo sơ mi', '2025-04-27 16:59:01.064217+07');
INSERT INTO public.product_types VALUES (3, '2025-04-27 16:59:01.064217+07', 'Quần jean', '2025-04-27 16:59:01.064217+07');
INSERT INTO public.product_types VALUES (4, '2025-04-27 16:59:01.064217+07', 'Quần short', '2025-04-27 16:59:01.064217+07');
INSERT INTO public.product_types VALUES (5, '2025-04-27 16:59:01.064217+07', 'Váy đầm', '2025-04-27 16:59:01.064217+07');
INSERT INTO public.product_types VALUES (6, '2025-04-27 16:59:01.064217+07', 'Áo khoác', '2025-04-27 16:59:01.064217+07');
INSERT INTO public.product_types VALUES (7, '2025-04-27 16:59:01.064217+07', 'Áo hoodie', '2025-04-27 16:59:01.064217+07');
INSERT INTO public.product_types VALUES (8, '2025-04-27 16:59:01.064217+07', 'Quần legging', '2025-04-27 16:59:01.064217+07');
INSERT INTO public.product_types VALUES (9, '2025-04-27 16:59:01.064217+07', 'Bộ đồ thể thao', '2025-04-27 16:59:01.064217+07');
INSERT INTO public.product_types VALUES (10, '2025-04-27 16:59:01.064217+07', 'Phụ kiện', '2025-04-27 16:59:01.064217+07');


--
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (1, 'Unisex', '2025-04-29 15:49:36.464889+07', 'Mô tả sản phẩm
hoodie nỉ chữ ngực

Chất liệu : nỉ lót bông

Size : Free size < 60kg', 'b7e4033d-32bd-494e-a2e4-7292cfe06e65.jpg', 'Áo Hoodie Nỉ Chữ Ngực Nhỏ ellon arc Form Rộng Tay Phồng, Áo Hoodie Màu Đen Unisex', 50000.0000, 100, '2025-04-29 15:51:45.679816+07', 1, 7, NULL);
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (2, 'Nike ', '2025-04-29 15:53:39.344763+07', 'áo hoodie nike chuẩn, tem mác chuẩn chính hãng.
- Chất liệu áo hoodie Nike: Nỉ cao cấp. vải mềm, vải mịn, không xù lông.
- áo khoác hoodie có đường may chuẩn chỉnh, tỉ mỉ, chắc chắn.

- Mặc ở nhà, mặc đi chơi hoặc khi vận động thể thao. Phù hợp khi mix đồ với nhiều loại.

- áo khoác hoodie Nike thiết kế hiện đại, trẻ trung, năng động. Dễ phối đồ
', '0a4b59e9-131b-44cd-a8ae-c7d47269d899.jpg', 'Áo hoodie Nike chính hãng mũ zip bassic form rộng nam nữ unisex, khoác nỉ hoodie basic oversize LOIX', 250000.0000, 399, '2025-04-29 15:54:23.180299+07', 1, 7, NULL);
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (3, 'StyleCo', '2025-04-29 15:56:34.606152+07', 'ÁO HOODIE NGẮN TAY BASIC với Chất liệu Cotton Ngoại tốt; mang phong cách thời trang thời thượng các bạn trẻ; đặc biệt không những giúp bạn giữ ấm trong mùa lạnh mà còn có thể chống nắng, chống gió, chống bụi, chống rét, chống tia UV cực tốt, rất tiện lợi nhé!!! (Được sử dụng nhiều trong dịp Lễ hội, Đi chơi, Da ngoại, Dạo phố, Du lịch...)', '71a81e8b-048a-44ec-b983-d70a2a45270a.jpg', 'ÁO HOODIE NGẮN TAY NAM BASIC CAO CẤP', 95000.0000, 400, '2025-04-29 15:57:23.732743+07', 1, 7, NULL);
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (4, 'ANYOUNG', '2025-04-29 15:58:39.280455+07', 'Áo hoodie nam nữ ANYOUNG áo hoodie cổ cao dáng rộng phối khóa kéo thời trang kiểu Ninja, áo nỉ hoodie khóa zip AN 656

🌟 𝐓𝐇𝐎̂𝐍𝐆 𝐓𝐈𝐍 𝐒𝐀̉𝐍 𝐏𝐇𝐀̂̉𝐌:

💥 Áo hoodie chữ thập cổ cao dáng rộng phối khóa kéo với chất liệu nỉ bông dày dặn mang phong cách thời trang thời thượng cho các bạn trẻ, đặc biệt không những giúp bạn giữ ấm trong mùa lạnh mà còn có thể chống nắng, chống gió, chống bụi, chống rét, chống tia UV cực tốt, rất tiện lợi nhé!!! (được sử dụng nhiều trong dịp lễ hội, đi chơi, da ngoại, dạo phố, du lịch...)', '7043e5cf-4e1a-419e-a6cf-37c3f4410d16.jpg', 'Áo hoodie nam nữ ANYOUNGcổ cao dáng rộng phối khóa thời trang AN 656', 199000.0000, 99, '2025-04-29 16:50:44.846772+07', 1, 7, 'S');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (5, 'Shop áo thu đông nam', '2025-04-29 18:33:49.10647+07', 'Hôm nay Aothudong xin giới thiệu 1 sản phẩm mới "Áo khoác gió nam xanh lịch lãm" mã số ATD-244, đây là mẫu áo khoác gió chính hãng mới cho dịp thu đông năm nay. Áo được thiết kế theo phong cách tối giản lịch lãm, giúp phù hợp với mọi lứa tuổi.

Áo làm từ chất liệu gió đặc biệt chống gió và giữ ấm tốt nhưng vẫn thoáng mồ hôi cho người mặc cảm giác thoải mái. Sản phẩm có 2 lớp dày dặn, với đường may tinh xảo, khoác là từ hợp kim không rỉ tạo nên chất lượng cho sản phẩm. Với màu xanh navy nam tính cho các bạn nam thêm mạnh mẽ.

Sản phẩm rất hợp với những chuyến đi chơi, tụ tập bạn bè cho mùa thu đông này thêm ấm áp. Hãy cùng chiêm ngưỡng thêm 1 số hình ảnh về sản phẩm và đặt mua ngay cho mình 1 chiếc nhé !!!
', '5d10c69b-ba4b-408f-aec8-8df3bcccff3d.jpg', 'Áo khoác gió nam xanh lịch lãm (ATD-244)', 299000.0000, 60, '2025-04-29 18:33:49.10647+07', 1, 6, 'M');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (8, 'Three PreFac', '2025-04-29 18:53:23.513682+07', 'LƯU Ý: Để đảm bảo quyền lợi của bản thân vui lòng QUAY VIDEO KHI BÓC HÀNG, shop chỉ giải quyết các vấn đề khi có Video! Mong KH phối hợp giúp Shop nha!

HƯỚNG DẪN GIỮ LÂU MÀU HÌNH IN VÀ SẢN PHẨM NHÀ BỐNG STORE
- Nhận hàng về nếu bạn có giặt thì mình giặt sơ bằng nước lạnh rồi phơi luôn nhé.
- Hoặc giặt sau 3 ngày nhận áo ( để hình in được dính chặt hơn trên sợi vải) ---> giữ hình in đẹp hơn.
- Các bạn tuyệt đối không ngâm áo với chất tẩy.

- Hạn chế giặt máy nhiều nha ít nhất trong 2 tuần đầu nè.
- Các bạn cũng đừng ngâm áo hoặc quần quá lâu trong nước giặt và nước xả nha.
- À, QUAN TRỌNG HƠN LÀ NHỚ LỘN ÁO TRƯỚC KHI PHƠI NÈ.', '20e40a14-6e7b-426f-b237-f396f44d5d2f.jpg', 'Áo Khoác Hoodie Three PreFact 3 sao MỚI form rộng tay dài Unisex Oversize, Áo hoodie Nam Nữ 3 sao Thêu', 144000.0000, 50, '2025-04-29 18:53:23.513682+07', 1, 6, 'S');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (9, 'D.E.R.O.R.R.E', '2025-04-29 18:57:34.150603+07', 'mô tả phẩm : Áo khoác gió nam nữ D.E.R.O.R.R.E chữ to in lưng FROM RÔNG,nam nữ đều mặc được ạ,hnn.

* Chất vải:  cotton dầy mịn thấm hút mồ hôi,không bị xù lông hay bai nhão

*  Thiết kế : Phù hợp phong cách giới trẻ hiện đại

* loại shop in là loại mực cao cấp không bong chóc hay phai màu dù giặt tay hay giặt máy.

*loại shop in 5d cao cấp không bong chóc hay phai màu dù giặt tay hay giặt mấy

Kiểu dáng gọn nhẹ, năng động.

* Phù hợp nhiều hoàn cảnh: mặc nhà, đi học, đi chơi, du lịch...                                                       

* Xuất xứ: Việt Nam                                                                                                                                                                                                                                                                                                                 *Đến với gian hàng bạn hãy bấm vào theo dõi gian hàng để giảm giá thêm nhé. Sau đó bạn bấm vào mã giảm giá để thu thập các mã giảm giá để giảm phí vận chuyển nhé.', 'fcdcb67f-f5cb-4b88-b037-ce01a066cb73.jpg', 'Áo khoác gió nam nữ D.E.R.O.R.R.E chữ to in lưng FROM RÔNG,nam nữ đều mặc được ạ', 800000.0000, 2, '2025-04-29 18:57:34.150603+07', 1, 6, NULL);
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (10, 'Ralph Lauren Harrington', '2025-04-29 18:58:46.519456+07', 'HƯỚNG DẪN CHỌN SIZE (BẢNG SIZE CHI TIẾT ẢNH CUỐI SẢN PHẨM)

SIZE S: Dài 64cm - Rộng 57,5cm - Tay Áo: 56cm - Fit Size 1M55-1M65 & 55KG-65KG

SIZE M: Dài 66,5cm - Rộng 59,5cm - Tay Áo: 57cm - Fit Size 1M65-1M75 & 65KG-75KG

SIZE L: Dài 69cm - Rộng 61,5cm - Tay Áo: 58cm - Fit Size 1M75-1M85 & 75KG-90KG (MAX: 95KG)



𝗧𝗛𝗢̂𝗡𝗚 𝗧𝗜𝗡 𝗦𝗔̉𝗡 𝗣𝗛𝗔̂̉𝗠 

- 𝗙𝗼𝗿𝗺 𝗔́𝗼:  Crop

- 𝗟𝗼𝗮̣𝗶 𝗔́𝗼:  Thêu Kỹ Thuật Số

- 𝗣𝗵𝘂̣ 𝗸𝗶𝗲̣̂𝗻: Tem Tag, Dây Treo, Bao Bì Chuẩn Hãng', 'a3b48517-860c-4029-801c-c4d1a219816c.jpg', 'Áo Khoác Ralph Lauren Harrington Chất Vải Cotton Kaki Dày Mịn Form Châu Âu Oversize', 400000.0000, 20, '2025-04-29 18:58:46.519456+07', 1, 6, 'M');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (11, '98WEAR ', '2025-04-29 18:59:48.894871+07', '"Chúng tôi cam kết đảm bảo chất lượng và Tiến độ từng đơn đặt hàng"

Áo sơ mi nam dài tay cao cấp 98WEAR Chất liệu lụa mềm mịn , kiểu dáng Hàn Quốc không nhăn, không xù , thấm hút mồ hôi.

1. Tên sản phẩm: Áo Sơ Mi Unisex

Dáng áo: Dáng rộng

Trọng lượng: 250 g

Chất vải: Vải lụa dãn nhẹ

Họa Tiết: Trơn

2. Hướng dẫn sử dụng Áo sơ mi form rộng:

Có thể giặt tay hoặc giặt máy

Không giặt chung với những sản phẩm ra mầu

Không dùng chất tẩy rửa, không ngâm quá lâu ngày

Không phơi dưới nhiệt độ cao, nắng gắt

Nên là ủi thường xuyên.

3. Chính sách bán hàng tại 98WEAR :

Sản phẩm chúng tôi mang tính chất : "Bán lẻ với giá bán sỉ". Lợi nhuận mỗi đơn hàng rất thấp, vì thế chúng tôi chỉ hoàn tiền, nhận trả hàng nếu sản phẩm của chúng tôi có lỗi, quý khách có video rõ ràng. Những trường hợp bị chật, bị rộng chúng tôi sẽ không hỗ trợ chi phí đổi trả được cho quý khách nếu như đặt hàng không đúng bảng size.

4. Lưu ý khi mua hàng:

Quý khách nên áp mã voucher giảm giá phí vận chuyển

Nên mua thêm các sản phẩm khách để hưởng thêm nhiều giảm giá tận gốc', '30a80f41-b626-4074-b537-d026e1e7d2e5.jpg', 'Áo Sơ mi nam Dài Tay Cao Cấp 98WEAR Chất Lụa Mềm Mịn , kiểu dáng Hàn Quốc không nhăn, không xù', 50000.0000, 999, '2025-04-29 18:59:48.894871+07', 1, 2, 'L');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (12, 'Mcoco ', '2025-04-29 19:10:27.881363+07', 'Áo Sơ Mi Nam Nữ , Áo Sơ Mi Thêu trái TIM trước ngực chất liệu lụa Cao Cấp cực đẹp cực xinh hot 2025



CAM KẾT CỦA SHOP:



✔️ Với kinh nghiệm 10 năm kinh doanh online đi đầu ngành thời trang, shop cam kết phục vụ:



- Sản phẩm chuẩn form mẫu

- 100% giống mô tả 

- Ship COD toàn quốc 

- Đổi trả miễn phí trong vòng 7 ngày, điều kiện: 

+ Hàng hoá vẫn còn mới, chưa qua sử dụng, còn nguyên tem mác 

+ Hàng không đúng size, kiểu dáng như quý khách đặt hàng 

+ Không đủ số lượng, không đủ bộ như trong đơn hàng.', '93fb6382-a024-4656-95de-cb0cacf2391b.png', 'Áo khoác sơ mi nữ dài tay khoác ngoài - Mcoco - Kiểu áo sơ mi trắng nữ form rộng vải đũi mát mẻ - thấm hút mồ hôi', 65000.0000, 789, '2025-04-29 19:10:27.881363+07', 1, 2, 'L');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (13, 'WETREND', '2025-04-29 19:14:55.971629+07', 'Áo Sơ Mi Kẻ Sọc Xanh Hồng Dài Tay Vải Cotton Lụa Cao Cấp Mẫu Hot dành cho các bạn nữ cực xinh cực đẹp năm  HOT TREND 2024
CAM KẾT CỦA SHOP:
✔️ Với kinh nghiệm 10 năm kinh doanh online đi đầu ngành thời trang, shop cam kết phục vụ:
- Sản phẩm chuẩn form mẫu
- 100% giống mô tả 
- Ship COD toàn quốc 

- Đổi trả miễn phí trong vòng 7 ngày, điều kiện: 
+ Hàng hoá vẫn còn mới, chưa qua sử dụng, còn nguyên tem mác 
+ Hàng không đúng size, kiểu dáng như quý khách đặt hàng 
+ Không đủ số lượng, không đủ bộ như trong đơn hàng.', '25e48a47-3d1d-48c4-ae6e-b4235f013433.jpg', 'Áo Sơ Mi Kẻ Sọc Xanh Hồng Dài Tay Vải Cotton Lụa Cao Cấp Mẫu Hot dành cho các bạn nữ cực xinh cực đẹp năm HOT 2024', 70000.0000, 300, '2025-04-29 19:14:55.971629+07', 1, 2, 'S');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (14, 'WETREND', '2025-04-29 19:19:52.475811+07', '- Sản phẩm chuẩn form mẫu

- 100% giống mô tả 

- Ship COD toàn quốc 

- Đổi trả miễn phí trong vòng 7 ngày, điều kiện: 

+ Hàng hoá vẫn còn mới, chưa qua sử dụng, còn nguyên tem mác 

+ Hàng không đúng size, kiểu dáng như quý khách đặt hàng 

+ Không đủ số lượng, không đủ bộ như trong đơn hàng.', 'fa15aa1e-31f5-4c17-b677-9ac197599ed4.png', 'Áo Sơ Mi Kẻ Sọc Xanh Hồng Dài Tay Vải Cotton Lụa Cao Cấp Mẫu Hot dành cho các bạn nữ cực xinh cực đẹp năm HOT 2024', 70000.0000, 300, '2025-04-29 19:19:52.475811+07', 1, 2, 'S');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (15, ' jodan', '2025-04-29 19:21:45.158618+07', '- Chất liệu: cotton

- Xuất xứ: Việt Nam

Bảng size xưởng may hoài mỡ

+ Size S : 42-50 kg cao 1m5-1m55 

+ Size M : 50 - 60 kg cao 1m55 - 1m65 

+ Size L : 60 - 67kg cao 1m65 - 1m80 

+ Size XL : 68- 75kg cao 1m65 - 1m80 

+ Size XXL : 76 - 85kg cao 1m65 - 1m85 ', '6fd67294-8185-4bdb-acbf-5c5aefe0dcf3.jpg', 'Bộ thể thao nam joda8n mùa hè mặc đi chơi bộ vải cotton kèm quần short đủ size thoáng mát otisstore.vn', 6000.0000, 100, '2025-04-29 19:21:45.158618+07', 1, 9, 'S');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (16, 'BO21', '2025-04-29 19:29:44.716418+07', 'Bộ Thể Thao Nam Nữ Mùa Hè Logo Cao Su Xịn Xò, Bộ Quần Áo Nam Mùa Hè Chất Xốp Thái Hàng Dày Dặn BO21 - ICON

🔶 Ở NGOÀI KIA CÓ VÔ VÀN SỰ LỰA CHỌN. VÔ CÙNG BIẾT ƠN QUÝ KHÁCH HÀNG ĐÃ LỰA CHỌN ICON STORE!



🍀 ICON STORE CAM KẾT

– Hàng cao cấp, tỉ mỉ, chắc chắn và tinh tế qua từng đường chỉ.

– Sản phẩm đảm bảo y hình và đẹp hơn hình.

– Đổi trả hàng nếu có bất cứ lỗi gì từ nhà sản xuất', 'affbaac6-4808-4488-8754-28edd2b2a071.jpg', 'Bộ Thể Thao Nam Nữ Mùa Hè Logo Cao Su Xịn Xò, Bộ Quần Áo Nam Mùa Hè Chất Xốp Thái Hàng Dày Dặn BO21 - ICON', 124000.0000, 60, '2025-04-29 19:29:44.716418+07', 1, 9, 'M');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (17, 'Mmestore', '2025-04-29 19:30:39.045287+07', 'Mô tả sản phẩm: 

- Chất liệu: Thun Poly, thông thoáng khiến cơ thể bạn không bị bí bức do tiết mồ hôi. 

- Chất dày dặn, co giãn 4 chiều mặc lên siêu ôm và tôn dáng

- Bộ tập gym nữ, quần áo tập gym yoga aerobic nữ của shop thiết kế đơn giản nhưng trẻ trung, năng động và sexy, phù hợp với hoạt động tập gym, vận động thể 

thao trong nhà hoặc ngoài trời, hay tập yoga… ', '543a0a50-e2bf-41fb-b84f-c7fcba7c3412.jpg', 'Độ tập gym nữ, bộ tập nhảy erobic nữ, quần 2 lớp áo cộc cao cấp chất thun mát thấm mồ hôi. ', 99000.0000, 141, '2025-04-29 19:30:39.045287+07', 1, 9, 'S');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (18, ' Bellken Menswear', '2025-04-29 19:32:31.055568+07', '✅ Mô tả sản phẩm:

📣 Vật liệu：denim

📣Phong cách: Hyundai Hàn Quốc

📣 Vòng eo cao: eo giữa

📣 Loại: lỏng lẻo', '520f22ff-be96-4aba-b65a-a56ae15ccc83.jpg', 'Quần jean nam ống đứng chun ống rộng vải jeans bò xanh quần bò nam Bền Màu Form Trẻ Trung hottrend 2023', 66666.0000, 99, '2025-04-29 19:32:31.055568+07', 1, 3, 'M');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (28, 'NOBICO', '2025-04-29 21:15:05.903342+07', '- Tên sản phẩm: Đầm Body Nữ

- Size: Freesize <60kg

-Chất liệu: Thun gân tăm cao cấp 

- Đường may tinh tế, tỉ mỉ trong từng chi tiết 

- Phong cách: trẻ trung, thanh lịch, tự tin, xinh đẹp.. 

• Kiểu dáng ôm body hack dáng xinh 

• Sản phẩm năng động đi chơi, du lịch mặc nhà đều hợp', 'b03ddc03-d13f-4940-9a2b-369a05e25f0b.jpg', 'Đầm Body Nữ Tay Ngắn Dáng Dài Có Khóa Kéo, Váy Body Nữ Đi Chơi Dự Tiệc Sang Chảnh NOBICO D120', 40000.0000, 200, '2025-04-29 21:15:05.903342+07', 1, 5, 'M');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (19, 'Retrostyle', '2025-04-29 19:33:45.285659+07', 'Quần bò jean ống xuông ống rộng đủ màu



Mã hàng 7224 Premium :SMLXL : Mã chất jean cao cấp mềm hơn, màu đẹp hơn chuẩn fom tôn dáng.



7224 chất đẹp mềm mịn , nhấn ly sâu hơn .Màu nhạt hơn và lên dáng chuẩn hơn. Chiều dài và rộng quần đều hơn bản thường 9304.



7225 chỉ khác 7224 ở phần chiết ly : 7224 có chiết ly còn 7225 không có chiết ly.

Mã 9302-9303-9304', '34f7effc-e10e-4669-afe0-237bad11c312.png', 'Quần bò jean ống xuông ống rộng đủ màu Retrostyle', 90000.0000, 50, '2025-04-29 19:33:45.285659+07', 1, 3, 'L');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (20, 'GT Clothes', '2025-04-29 21:03:37.205062+07', '* Mô tả chi tiết:

- Cực hack dáng, tôn dáng cho các nàng

- Chất liệu vải đẹp, thun siêu co giãn 4 chiều thoáng mát, ôm sát cơ thể mà không bị nóng, bí.

- Cạp cao ôm bụng che giấu các khuyết điểm và tạo thành đường cong thon gọn.

- Nâng cao mông giúp mông tròn đều không bị chảy xệ.

- Ôm bó sát nhưng k lo bị lộ vùng nhạy cảm.

- Phù hợp với mọi nhu cầu: đi chơi, đi tập gym, yoga, earobic...

- Kết hợp được với nhiều loại trang phục: áo phông, áo sơ mi, bra tập thể thao…', 'b000b87c-60bd-4a88-91f7-fa0d6cf910af.jpg', 'Quần Legging Nữ Ống Loe Lưng Cao Chất Co Giãn 4 Chiều Cao Cấp Nâng Mông Hack dáng GT Clothes', 123000.0000, 300, '2025-04-29 21:03:37.205062+07', 1, 8, 'L');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (21, 'Mira storee', '2025-04-29 21:04:36.778789+07', 'THÔNG TIN SẢN PHẨM : 

- Tên sản phẩm: Quần legging nữ có túi cạp cao dáng dài, quần ống loe nâng mông chất thun mịn co giãn

- Màu sắc: Đen

- Size: S - 5XL

- Chiều dài: 96cm

- Chất liệu: vải cotton

*(vui lòng inbox chiều cao và cân nặng để được tư vấn size phù hợp)', 'a2bd4f8d-7e9f-4d93-80aa-bfe041974f6e.jpg', 'Quần legging nữ có túi cạp cao dáng dài, quần ống loe nâng mông chất thun mịn co giãn - Mira storee', 60000.0000, 350, '2025-04-29 21:04:36.778789+07', 1, 8, 'M');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (22, 'KAKI ', '2025-04-29 21:05:50.210438+07', '- Quần phom vừa, dáng thanh niên trẻ trung, nếu khách thích mặc thoải mái có thể lấy tăng 1 size hoăc chat với shop để được tư vẫn size phù hợp.

- Bảng size chỉ mang tính chất tương đối, nếu kh cao, thấp... nên chat với shop để được tư vấn size phù hợp nhất



- Quần short có chất liệu KAKI, vải dày dặn mát mịn, thoải mái trong từng chuyển động khi  di chuyển, đứng lên/ ngồi xuống.

-Giặt tay hay giặt máy thoải mái không sợ ra màu, nhăn , mất form', '27fa9a47-0313-41f6-84f2-50f9a42d77d8.jpg', 'Quần short nam, quần ngắn nam KAKI [LOẠI ĐẸP] cạp khuy trẻ trung năng động', 60000.0000, 500, '2025-04-29 21:05:50.210438+07', 1, 4, 'L');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (23, 'wwyn signature', '2025-04-29 21:07:08.03446+07', ' QUẦN SHORT DÙ Sport BASIC WWYN LOGO
MÀU : ĐEN / XÁM 
CHẤT LIỆU : DÙ GIÓ CÓ LÓT
SIZE: M/L/XL
Form quần được fit size theo form và yêu cầu kha khá của người việt nam Nam, nếu khách hàng đang suy xét giữa hai size, nên lựa chọn cỡ to hơn.
Size M : cao dưới 1m70 - nặng dưới 60kg
Size L : cao từ 1m70 đến 1m80 - nặng dưới 80kg
Size XL : nhích cao hơn 1m80 - nặng dưới 95kg', '29fb4b35-219c-4974-a324-9091ce6abaf8.jpg', '"XẢ KHO"Quần short dù WWYNSTU logo wwyn signature black / grey dù gió nhăn cao cấp 2 lớp hot nhất hiện nay', 20000.0000, 450, '2025-04-29 21:08:20.13583+07', 1, 4, 'M');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (25, 'Emitabloom', '2025-04-29 21:11:50.412493+07', 'Tất cả các sản phẩm của chúng tôi là tảo bẹ, bán trực tiếp cho người mua, chất lượng tốt.

Persenan sẽ được chuyển đến sajrone trong 24 giờ nữa.



Sẵn có

Tỷ lệ phần trăm mới và chất lượng cao

Phải xem

Key largo:

Màu sắc có thể là một chút khác nhau, bởi vì màn hình của tất cả mọi người là không giống nhau, cảm ơn bạn đã hiểu biết của bạn!

Nếu bạn hài lòng với sản phẩm của chúng tôi, xin vui lòng cho chúng tôi một phản hồi tích cực (5 SAO).

Nếu cô thích cái váy này, hãy mua nó đúng lúc. Vì các vật liệu nhân tạo có chi phí khác nhau, chi phí sản phẩm sẽ tăng lên

Có sự khác biệt nhỏ giữa các lô.

Đề nghị dùng nước lạnh để làm sạch. Nó sẽ giúp duy trì hình dạng của vật thể.

Mua sắm vui vẻ, hâm mộ tôi, biết hàng hóa của tôi.', '462c89e9-2e61-4677-9a3e-29bf7235f345.jpg', 'Đầm hoa nhí dáng dài 2 dây trễ vai Đầm tiểu thư dạ hội dài nàng thơ Váy liền chữ A xinh nhẹ nhàng', 66000.0000, 60, '2025-04-29 21:11:50.412493+07', 1, 5, 'M');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (24, 'SBL LADY', '2025-04-29 21:10:34.513869+07', 'SBL Lady mong rằng sẽ mang đến cho các nàng một trải nghiệm mua sắm đáng tin cậy <3



Đầm có kèm quần mặc cùng nhen các nàng, không có mút ngực nha ạ



  🌱 Chất liệu: Thun đẹp co dãn 

  🌱 Kích thước:      

              Size S: Dài đầm 43 tà ngắn - 97 tà dài, Ngực 80-90 đổ lại, Eo 60-70 (cm)

              Size M: Dài đầm 44 tà ngắn - 98 tà dài, Ngực 90 - 99 đổ lại, Eo 70-80 (cm)



  🌱 Cam kết chất lượng đảm bảo, sản phẩm giống ảnh 

  🌱 Sản phẩm có sẵn, inbox SBL để được tư vấn nhiệt tình nhennnn', '91e4db19-7d8b-41f6-896d-d776e2dac8de.jpg', 'Đầm Body Sexy Đen Dáng ngắn Dự tiệc Chữ A Quây ngực Có lót quần Thiết kế Váy Đầm body Quyến rũ Gợi cảm Trễ vai Hàn Quốc', 266000.0000, 99, '2025-04-29 21:12:19.809824+07', 1, 5, 'L');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (26, 'Tafta ', '2025-04-29 21:13:19.47246+07', 'THÔNG TIN SẢN PHẨM

 - Tên sản phẩm: Váy tafta đính kết hoa ngực _ Mặc đi tiệc đi làm rất sang , lich sự. Phom dáng che khuyết điểm tôn dáng tôn eo.

- Xuất xứ: Việt Nam

- Chất liệu: Tafta

- Màu sắc: Kem , Đen, hồng

- Họa tiết: Xếp ly vùng sườn, Hoa Đính kết.

- Size: S - M - L – XL', 'e12cdb61-c4c6-4119-840a-90893afb11d0.jpg', 'Váy Tafta Đính Kết Hoa Ngực Phối Vạt Chéo Cách ĐiệuDành Cho Chị Em Đi Tiệc Cưới, Đi Làm , Mặc Che Khuyết Điểm Tôn Dáng.', 120000.0000, 90, '2025-04-29 21:13:19.47246+07', 1, 5, 'M');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (27, 'DOSHOP', '2025-04-29 21:14:07.214798+07', 'Đầm thiết kế thanh lịch, váy body chất tơ ánh nhũ mềm mát sang trọng TX6) DOSHOP

THÔNG TIN SẢN PHẨM (Mã SP: TX6)

-        Màu sắc: trắng, đen

-        Chất liệu: Tafta phối tơ

-        Thiết kế: 2 lớp, may bọc lót kĩ và thấm hút tốt ( có mút)

-        Form dáng: Váy thiết kế chuẩn size ,đánh eo cao dưới chân ngực tạo cảm giác thon gọn, che khuyết điểm vòng 2

* BẢNG SIZE              

Size S: Từ 40-47kg                             (V1 <85cm, Eo <66cm)                                           

Size M: Từ 48-53kg                             (V1 <88cm, Eo <70cm

Size L:  Từ 54-58kg                             (V1 < 92cm, Eo <75cm)     

Size Xl:  Từ 59-64kg                             (v1 <96cm, Eo dưới <80cm)                         

Tuỳ chiều cao, chọn chính xác theo số đo eo và vòng 1

* Thông số trên là thông số chuẩn, tùy thuộc vào số đo cơ thể mỗi người và chất liệu vải khác nhau sẽ có sự chênh lệch nhất định từ 1 - 2cm.', 'a78036ee-e417-444d-a6df-fa322289aa18.jpg', 'Đầm dự tiệc trễ vai sang chảnh chất tafta phối tơ Váy trễ vai đính hoa tiểu thư TX6 DOSHOP', 295000.0000, 200, '2025-04-29 21:14:07.214798+07', 1, 5, 'L');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (29, 'P&Q', '2025-04-29 21:16:04.064796+07', 'Chúng mình xin gửi mô tả sản phẩm để bạn tham khảo nhé:

- Chất liệu: vải kaki mềm

- Màu sắc: đen

- Form váy xòe chữ A trễ vai tiểu thư nhấn eo hack dáng, che khuyết điểm siu đỉnh.

- Size :XS | S | M | L | XL | XXL | 3XL | nhận may theo số đo (nàng hãy inbox cho shop nha).', 'cf78b01d-a984-4d2f-8d46-89d3a7a20462.jpg', 'P&Q vd36 | Đầm váy xòe mini dạo phố vải kaki mềm dự tiệc hai lớp có bigsize | P&Q vd36', 249000.0000, 333, '2025-04-29 21:16:04.064796+07', 1, 5, 'M');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (30, 'NEMY ', '2025-04-29 21:16:59.16934+07', '✔️ Sản phẩm được thiết kế và sản xuất duy nhất bởi nhãn hàng NEMY CLOTHING 

👉 Chất lượng được kiểm duyệt kỹ càng đem đến sản phẩm chất lượng nhất! 

👉 khách hàng nhận hàng kiểm tra thoải mái theo chính sách shopee 

 👉 Shop có hỗ trợ đổi size Free , khách có nhu cầu đổi size vui lòng chat shop để được hỗ trợ  😊', '181a246c-17d6-49c5-aa12-09474c10f8e7.jpg', 'Váy Đầm cổ tròn tay cộc thanh lịch sang trọng màu xanh bơ siêu đẹp NEMY V2556', 210000.0000, 300, '2025-04-29 21:16:59.16934+07', 1, 5, 'M');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (31, 'streetwear ', '2025-04-29 21:18:14.93964+07', 'Áo thun nam, áo thun nữ tay lỡ unisex, áo phông cotton form rộng oversize streetwear Phong cách Hàn Quốc

THÔNG TIN SẢN PHẨM:

•	Tên sản phẩm: Áo thun nam, áo thun nữ tay lỡ unisex SHIN PHG3

•	Kiểu dáng: Unisex, Freesize, Tay lỡ, Form rộng

•	Xuất sứ: Việt Nam

•	Chất liệu: Cotton

•	Họa tiết: In

•	Màu sắc: Đen, Trắng', 'bfc96887-75eb-4b56-8d66-ffc507b5e777.jpg', 'Áo thun nam nữ unisex tay lỡ A.R.0.1, áo phông cotton form rộng oversize streetwear Trend 2022', 60000.0000, 150, '2025-04-29 21:18:14.93964+07', 1, 1, 'S');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (32, 'unisex ', '2025-04-29 21:19:18.670966+07', 'HIPHOPPUNKS CAM KẾT:

﻿

◾ Chất liệu vải Cotton 100% co dãn 2 chiều, Định lượng cao 230gsm, 

﻿

◾ Vải chính phẩm đã qua xử lý co rút, và lông thừa

﻿

◾ chất vải mềm mịn dày nhưng cực kì mát và không xù

﻿

◾ Hoàn tiền nếu sản phẩm không giống với mô tả

﻿

◾ Nam và Nữ đều mặc được, form áo rộng chuẩn TAY LỠ UNISEX cực đẹp', '3545a24a-ee33-4039-9894-592855b91d62.jpg', 'HIP Áo thun ngắn tay local brand fashion áo phông nam nữ unisex bigsize vintage 230g cotton', 99000.0000, 300, '2025-04-29 21:19:18.670966+07', 1, 1, 'S');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (33, ' Acme', '2025-04-29 21:21:52.757319+07', 'Khẳng định phong cách cá tính cùng **Áo Thun Acme.dela.Vie In Nổi 5D Cao Cấp** – item thời trang đang "gây sốt" trong xu hướng **Hot Trend 2025**. Thiết kế **form rộng tay lỡ hiện đại**, phù hợp cho cả nam và nữ, mang lại vẻ ngoài năng động, trẻ trung.  



Chất vải **mềm mịn, co giãn nhẹ, thấm hút mồ hôi cực tốt**, cho cảm giác thoải mái suốt ngày dài hoạt động. Điểm nhấn độc đáo là họa tiết **in nổi 5D sắc nét, bền màu**, tạo nên sự nổi bật và đậm chất streetwear.  



Sản phẩm có **3 màu đa dạng**, dễ mix & match với mọi phong cách từ basic đến cá tính.', '3d0b7da3-dd18-4ff1-8e79-10bec9755327.jpg', 'Áo Thun Acme.dela.Vie In Nổi 5D Cao Cấp | Form Rộng Tay Lỡ, Mềm Mịn Thoáng Mát – Hot Trend 2025', 59000.0000, 300, '2025-04-29 21:21:52.757319+07', 1, 1, 'S');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (7, '20SILK', '2025-04-29 18:37:46.692504+07', NULL, '779be0be-5695-48cc-a145-330ff3eea45c.jpg', 'Áo khoác gió nam-nữ 2 lớp có túi trong, Áo khoác dù chất liệu vải gió cao cấp kháng nước full tem tag phụ kiện', 200000.0000, 99, '2025-04-30 10:48:56.069904+07', 1, 6, 'L');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (6, 'kaki ', '2025-04-29 18:35:24.943392+07', 'Bạn cần 1 chiếc áo khoác kaki 2 lớp dày dặn trẻ trung năng động mặc trong mọi điều kiện thời tiết? AoKhoacNam xin giới thiệu mẫu "Áo khoác kaki nam xanh cao cấp" mã số AKN-158. Áo thiết kế trẻ trung, form áo gọn gàng, với màu xanh navy nam tính mạnh mẽ.

Áo có 2 lớp, lớp ngoài làm từ vải kaki cao cấp có độ bền cao, không phai màu và bai xù khi giặt, lớp bên trong lót vải mềm gió thoáng mồ hôi và thoải mái khi mặc. Sản phẩm với đường may tinh xảo, khóa được làm từ hợp kim không rỉ giúp tăng độ bền và dễ dàng khi kéo. Áo có 2 túi ngoài có khóa và 1 túi trong tiện lợi khi mang theo đồ quan trọng. Áo có bo thun ở ống tay và đai dưới thân, cổ trụ cao giúp chắn gió tốt trong thời tiết thu đông.

Hãy cùng chúng tôi chiêm ngưỡng sản phẩm qua 1 số hình ảnh sau và liên hệ ngay để nhận được tư vấn đặt hàng miễn phí và các chương trình ưu đãi mới nhất về sản phẩm này các bạn nhé !!!', '8bb571ea-2650-4658-87d7-d7c4a1daa3aa.jpg', 'Áo khoác kaki nam xanh cao cấp (AKN-158)', 300000.0000, 1, '2025-05-05 23:28:56.092567+07', 1, 6, 'L');
INSERT INTO public.products OVERRIDING SYSTEM VALUE VALUES (34, 'LOOSE PLEAT', '2025-05-06 03:37:57.759405+07', NULL, '4c16244d-4100-4f96-b4ca-fe1461af2fc0.jpg', 'ALL JEANS LOOSE PLEAT - Quần Jeans Ống Rộng Nam Nữ Unisex Cạp Cao Wash Màu Kèm Dây Trẻ Trung, Năng Động Menswear', 91000.0000, 100, '2025-05-06 03:37:57.759405+07', 1, 3, 'S');


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.roles VALUES (1, 'ADMIN', 'High');
INSERT INTO public.roles VALUES (2, 'CUSTOMER', 'Medium');


--
-- Data for Name: shopping_carts; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.users VALUES (1, '123 Admin St', '1985-01-10', '2025-04-15 21:56:46.163065+07', 'admin@example.com', '$2a$10$igEwr1rS3cWCH6WRW4A7i.cfjjuUgVmZAczwUoGhpI5iwENIOd.v6', NULL, '2025-04-16 22:25:55.483816+07', 'admin', 1);
INSERT INTO public.users VALUES (2, 'admin123', '2003-06-16', '2025-04-15 22:02:07.440048+07', 'ngohoangnam99999@gmail.com', '$2a$10$erqWKSygevFcgaa91GufUeqoOu6ij6GvUtAD1gszycjMyrlRsxtve', '0338630402', '2025-04-16 13:47:34.271619+07', 'admin123', 2);
INSERT INTO public.users VALUES (3, NULL, '2005-05-09', '2025-05-04 16:19:27.358373+07', 'bhnam789@example.com', '$2a$10$EmutUhnBG8sZTEnI.WDc8eA1xVa08whhHI8HVtEQWS9L8UmXLY9Vq', NULL, '2025-05-04 16:19:55.985347+07', 'hnam', 2);
INSERT INTO public.users VALUES (6, NULL, '2000-05-08', '2025-05-04 16:22:06.668968+07', 'Kimngoc47848@gmail.com', '$2a$10$jOkq4Ott/PSG1wgkqELdKOl9Yja.33ERa27g5XWegWqVA65s/P/7C', NULL, '2025-05-04 16:22:30.843641+07', 'Kimngoc47848', 2);
INSERT INTO public.users VALUES (4, NULL, '2008-05-13', '2025-05-04 16:20:49.752915+07', 'nguuduan@gmail.com', '$2a$10$PemkxgfRo.smaTn2Tkk9Xu5OWcuIrFAtqrsljjqOdFf9f4DzzTLOm', NULL, '2025-05-04 16:22:44.7039+07', 'NguyenViet', 2);
INSERT INTO public.users VALUES (5, NULL, '2003-05-13', '2025-05-04 16:21:33.200412+07', 'ThiHoa789@gmail.com', '$2a$10$R64NgAFzHJEBq1BcTf206OJ9myH.x5ZXiTEjG8oBw.qQ0IqzFIGv2', NULL, '2025-05-04 16:22:55.377966+07', 'ThiHoa789', 2);


--
-- Name: order_details_order_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.order_details_order_detail_id_seq', 12, true);


--
-- Name: order_status_serial_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.order_status_serial_id_seq', 1, false);


--
-- Name: orders_serial_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.orders_serial_id_seq', 8, true);


--
-- Name: product_types_serial_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.product_types_serial_id_seq', 10, true);


--
-- Name: products_serial_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.products_serial_id_seq', 1, false);


--
-- Name: products_serial_id_seq1; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.products_serial_id_seq1', 34, true);


--
-- Name: roles_serial_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_serial_id_seq', 1, false);


--
-- Name: shopping_carts_cart_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.shopping_carts_cart_item_id_seq', 15, true);


--
-- Name: users_serial_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_serial_id_seq', 6, true);


--
-- Name: order_details order_details_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_details
    ADD CONSTRAINT order_details_pkey PRIMARY KEY (order_detail_id);


--
-- Name: order_status order_status_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_status
    ADD CONSTRAINT order_status_pkey PRIMARY KEY (serial_id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (serial_id);


--
-- Name: product_types product_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_types
    ADD CONSTRAINT product_types_pkey PRIMARY KEY (serial_id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (serial_id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (serial_id);


--
-- Name: shopping_carts shopping_carts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.shopping_carts
    ADD CONSTRAINT shopping_carts_pkey PRIMARY KEY (cart_item_id);


--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: product_types uk_6iopyn5hbyxusogmmwjr5ci2q; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_types
    ADD CONSTRAINT uk_6iopyn5hbyxusogmmwjr5ci2q UNIQUE (name);


--
-- Name: orders uk_dhk2umg8ijjkg4njg6891trit; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT uk_dhk2umg8ijjkg4njg6891trit UNIQUE (order_code);


--
-- Name: users uk_du5v5sr43g5bfnji4vb8hg5s3; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_du5v5sr43g5bfnji4vb8hg5s3 UNIQUE (phone);


--
-- Name: order_status uk_dxq9b5vgrv30g8gfrwplk1ws4; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_status
    ADD CONSTRAINT uk_dxq9b5vgrv30g8gfrwplk1ws4 UNIQUE (status_code);


--
-- Name: users uk_k8d0f2n7n88w1a16yhua64onx; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_k8d0f2n7n88w1a16yhua64onx UNIQUE (user_name);


--
-- Name: roles uk_ofx66keruapi6vyqpv6f2or37; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT uk_ofx66keruapi6vyqpv6f2or37 UNIQUE (name);


--
-- Name: orders uk_qhochqo7y66p216aes9cpi4rl; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT uk_qhochqo7y66p216aes9cpi4rl UNIQUE (guest_token);


--
-- Name: shopping_carts uq_guest_product; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.shopping_carts
    ADD CONSTRAINT uq_guest_product UNIQUE (guest_id, product_id);


--
-- Name: shopping_carts uq_user_product; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.shopping_carts
    ADD CONSTRAINT uq_user_product UNIQUE (user_id, product_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (serial_id);


--
-- Name: orders fk1abokg3ghque9pf2ujbxakng; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fk1abokg3ghque9pf2ujbxakng FOREIGN KEY (status_id) REFERENCES public.order_status(serial_id);


--
-- Name: order_details fkjyu2qbqt8gnvno9oe9j2s2ldk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_details
    ADD CONSTRAINT fkjyu2qbqt8gnvno9oe9j2s2ldk FOREIGN KEY (order_id) REFERENCES public.orders(serial_id);


--
-- Name: users fkp56c1712k691lhsyewcssf40f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fkp56c1712k691lhsyewcssf40f FOREIGN KEY (role_id) REFERENCES public.roles(serial_id);


--
-- Name: products fkrv6og3b2qlahvka0bxn7btyqd; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fkrv6og3b2qlahvka0bxn7btyqd FOREIGN KEY (product_type_id) REFERENCES public.product_types(serial_id);


--
-- PostgreSQL database dump complete
--

