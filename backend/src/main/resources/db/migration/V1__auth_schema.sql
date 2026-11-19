-- V1: Tài khoản, phân quyền, nhật ký đăng nhập (UC-TK01, FR-TK-01..03)

CREATE TABLE user_account (
    id               UUID PRIMARY KEY,
    email            VARCHAR(254) NOT NULL,
    password_hash    VARCHAR(100) NOT NULL,
    full_name        VARCHAR(150) NOT NULL,
    phone            VARCHAR(30),
    role             VARCHAR(20)  NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    failed_attempts  INTEGER      NOT NULL DEFAULT 0,
    locked_until     TIMESTAMPTZ,
    last_login_at    TIMESTAMPTZ,
    tenant_id        UUID,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_user_account_email UNIQUE (email),
    CONSTRAINT ck_user_account_role CHECK (role IN ('ADMIN', 'ACCOUNTANT', 'TECHNICIAN', 'TENANT')),
    CONSTRAINT ck_user_account_status CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED')),
    CONSTRAINT ck_user_account_tenant CHECK (tenant_id IS NULL OR role = 'TENANT')
);

-- Mỗi khách thuê có tối đa một tài khoản cổng thông tin
CREATE UNIQUE INDEX uq_user_account_tenant ON user_account (tenant_id) WHERE tenant_id IS NOT NULL;

CREATE TABLE login_audit_log (
    id              UUID PRIMARY KEY,
    user_id         UUID REFERENCES user_account (id),
    email           VARCHAR(254) NOT NULL,
    success         BOOLEAN      NOT NULL,
    failure_reason  VARCHAR(50),
    ip_address      VARCHAR(64),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_login_audit_log_user ON login_audit_log (user_id, created_at DESC);
