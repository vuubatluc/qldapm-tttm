-- V2: Mặt bằng, khách thuê, hợp đồng (Module 1 - mục 2.1 thiết kế CSDL, bản rút gọn)

CREATE TABLE floor (
    id          UUID PRIMARY KEY,
    floor_code  VARCHAR(20)  NOT NULL,
    name        VARCHAR(120) NOT NULL,
    level_no    INTEGER      NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_floor_code UNIQUE (floor_code)
);

CREATE TABLE zone (
    id          UUID PRIMARY KEY,
    floor_id    UUID         NOT NULL REFERENCES floor (id),
    zone_code   VARCHAR(20)  NOT NULL,
    name        VARCHAR(120) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_zone_floor_code UNIQUE (floor_id, zone_code)
);

CREATE TABLE unit (
    id                UUID PRIMARY KEY,
    zone_id           UUID          NOT NULL REFERENCES zone (id),
    unit_code         VARCHAR(30)   NOT NULL,
    area_m2           DECIMAL(10,2) NOT NULL,
    status            VARCHAR(20)   NOT NULL,
    planned_category  VARCHAR(100),
    ref_rent          DECIMAL(18,2),
    created_at        TIMESTAMPTZ   NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uq_unit_code UNIQUE (unit_code),
    CONSTRAINT ck_unit_area CHECK (area_m2 > 0),
    CONSTRAINT ck_unit_status CHECK (status IN ('AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE'))
);

CREATE INDEX ix_unit_zone_status ON unit (zone_id, status);

CREATE TABLE unit_status_log (
    id          UUID PRIMARY KEY,
    unit_id     UUID        NOT NULL REFERENCES unit (id),
    old_status  VARCHAR(20) NOT NULL,
    new_status  VARCHAR(20) NOT NULL,
    changed_at  TIMESTAMPTZ NOT NULL,
    actor_id    UUID REFERENCES user_account (id),
    reason      TEXT
);

CREATE SEQUENCE tenant_code_seq START 1;

CREATE TABLE tenant (
    id              UUID PRIMARY KEY,
    tenant_code     VARCHAR(30)  NOT NULL,
    legal_name      VARCHAR(200) NOT NULL,
    brand_name      VARCHAR(200) NOT NULL,
    id_type         VARCHAR(20)  NOT NULL,
    id_number       VARCHAR(20)  NOT NULL,
    representative  VARCHAR(150),
    contact_email   VARCHAR(254) NOT NULL,
    phone           VARCHAR(30)  NOT NULL,
    address         TEXT,
    category        VARCHAR(100),
    status          VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_tenant_code UNIQUE (tenant_code),
    CONSTRAINT uq_tenant_identity UNIQUE (id_type, id_number),
    CONSTRAINT ck_tenant_id_type CHECK (id_type IN ('TAX_CODE', 'CITIZEN_ID')),
    CONSTRAINT ck_tenant_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

ALTER TABLE user_account
    ADD CONSTRAINT fk_user_account_tenant FOREIGN KEY (tenant_id) REFERENCES tenant (id);

CREATE TABLE contract (
    id                    UUID PRIMARY KEY,
    contract_no           VARCHAR(40)   NOT NULL,
    tenant_id             UUID          NOT NULL REFERENCES tenant (id),
    status                VARCHAR(24)   NOT NULL,
    signed_on             DATE,
    payment_cycle_months  SMALLINT      NOT NULL,
    deposit_amount        DECIMAL(18,2) NOT NULL,
    note                  TEXT,
    created_at            TIMESTAMPTZ   NOT NULL,
    updated_at            TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uq_contract_no UNIQUE (contract_no),
    CONSTRAINT ck_contract_cycle CHECK (payment_cycle_months > 0),
    CONSTRAINT ck_contract_deposit CHECK (deposit_amount >= 0),
    CONSTRAINT ck_contract_status CHECK (status IN ('DRAFT', 'ACTIVE', 'PENDING_TERMINATION', 'TERMINATED', 'CANCELLED'))
);

CREATE INDEX ix_contract_tenant_status ON contract (tenant_id, status);

CREATE TABLE contract_unit (
    id              UUID PRIMARY KEY,
    contract_id     UUID          NOT NULL REFERENCES contract (id),
    unit_id         UUID          NOT NULL REFERENCES unit (id),
    lease_from      DATE          NOT NULL,
    lease_to_excl   DATE          NOT NULL,
    charge_area_m2  DECIMAL(10,2) NOT NULL,
    rent_rate       DECIMAL(18,2) NOT NULL,
    service_rate    DECIMAL(18,2) NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uq_contract_unit UNIQUE (contract_id, unit_id),
    CONSTRAINT ck_contract_unit_period CHECK (lease_to_excl > lease_from),
    CONSTRAINT ck_contract_unit_area CHECK (charge_area_m2 > 0),
    CONSTRAINT ck_contract_unit_rates CHECK (rent_rate >= 0 AND service_rate >= 0)
);

CREATE INDEX ix_contract_unit_period ON contract_unit (unit_id, lease_from, lease_to_excl);
