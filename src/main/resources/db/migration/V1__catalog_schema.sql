-- V1 — Catalog schema
--
-- Faithful migration of the legacy Java Pet Store catalog schema, derived from
-- src/apps/petstore/src/docroot/populate/PopulateSQL.xml in the 1.3.1 distribution.
--
-- Fidelity notes:
--  * The legacy Cloudscape DDL used CHAR(10) for ids; the shipped Oracle DDL used VARCHAR(10).
--    We adopt VARCHAR to avoid CHAR space-padding pitfalls in key comparisons (documented
--    deviation — see docs/adr/0004). Column names (catid, descn, attrN, locale) are preserved.
--  * i18n is modeled exactly as the legacy design: a base table plus a localized *_details
--    side-table keyed by (id, locale).

create table category (
    catid varchar(10) not null,
    constraint pk_category primary key (catid)
);

create table category_details (
    catid  varchar(10)  not null,
    name   varchar(80)  not null,
    image  varchar(255),
    descn  varchar(255),
    locale varchar(10)  not null,
    constraint pk_category_details primary key (catid, locale),
    constraint fk_category_details_1 foreign key (catid) references category (catid)
);

create table product (
    productid varchar(10) not null,
    catid     varchar(10) not null,
    constraint pk_product primary key (productid),
    constraint fk_product_1 foreign key (catid) references category (catid)
);

create table product_details (
    productid varchar(10)  not null,
    locale    varchar(10)  not null,
    name      varchar(80)  not null,
    image     varchar(255),
    descn     varchar(255),
    constraint pk_product_details primary key (productid, locale),
    constraint fk_product_details_1 foreign key (productid) references product (productid)
);

create table item (
    itemid    varchar(10) not null,
    productid varchar(10) not null,
    constraint pk_item primary key (itemid),
    constraint fk_item_1 foreign key (productid) references product (productid)
);

create table item_details (
    itemid    varchar(10)    not null,
    listprice decimal(10, 2) not null,
    unitcost  decimal(10, 2) not null,
    locale    varchar(10)    not null,
    image     varchar(255),
    descn     varchar(255),
    attr1     varchar(80),
    attr2     varchar(80),
    attr3     varchar(80),
    attr4     varchar(80),
    attr5     varchar(80),
    constraint pk_item_details primary key (itemid, locale),
    constraint fk_item_details_1 foreign key (itemid) references item (itemid)
);

-- Read-path indexes for the browse hierarchy (category -> products -> items).
create index idx_product_catid on product (catid);
create index idx_item_productid on item (productid);
