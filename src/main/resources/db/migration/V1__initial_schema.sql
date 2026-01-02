-- =========================
-- PARTNER
-- =========================
CREATE TABLE partner (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         address TEXT NOT NULL,
                         partner_tier VARCHAR(50),
                         date_of_birth DATE NOT NULL,
                         registered_date DATE NOT NULL
);

-- =========================
-- COMMISSION RULE
-- =========================
CREATE TABLE commission_rule (
                                 id BIGSERIAL PRIMARY KEY,
                                 rule_name VARCHAR(255),
                                 partner_tier VARCHAR(50),
                                 category VARCHAR(100),
                                 min_amount NUMERIC(19,2),
                                 max_amount NUMERIC(19,2),
                                 percentage NUMERIC(5,2),
                                 fixed_bonus NUMERIC(19,2),
                                 valid_from TIMESTAMP,
                                 valid_to TIMESTAMP,
                                 priority INTEGER
);

-- =========================
-- TRANSACTION
-- =========================
CREATE TABLE transaction (
                             transaction_id BIGSERIAL PRIMARY KEY,
                             partner_id BIGINT NOT NULL,
                             amount NUMERIC(12,2) NOT NULL,
                             currency VARCHAR(10) NOT NULL,
                             category VARCHAR(100),
                             sale_date TIMESTAMP NOT NULL,
                             created_at TIMESTAMP,
                             updated_at TIMESTAMP,

                             CONSTRAINT fk_transaction_partner
                                 FOREIGN KEY (partner_id) REFERENCES partner(id)
);

CREATE INDEX idx_transaction_partner_id ON transaction(partner_id);
CREATE INDEX idx_transaction_sale_date ON transaction(sale_date);

-- =========================
-- INVOICE
-- =========================
CREATE TABLE invoice (
                         id UUID PRIMARY KEY,
                         invoice_number VARCHAR(255) NOT NULL UNIQUE,
                         partner_id BIGINT NOT NULL,
                         invoice_date DATE NOT NULL,
                         start_period DATE NOT NULL,
                         end_period DATE NOT NULL,
                         total_sales NUMERIC(19,2) NOT NULL,
                         commission NUMERIC(19,2) NOT NULL,
                         currency VARCHAR(10),
                         status VARCHAR(30),
                         pdf_url TEXT,
                         created_at TIMESTAMP,
                         updated_at TIMESTAMP,

                         CONSTRAINT fk_invoice_partner
                             FOREIGN KEY (partner_id) REFERENCES partner(id)
);

CREATE INDEX idx_invoice_partner_id ON invoice(partner_id);
CREATE INDEX idx_invoice_invoice_date ON invoice(invoice_date);

-- =========================
-- PARTNER COMMISSION
-- =========================
CREATE TABLE partner_commission (
                                    id BIGSERIAL PRIMARY KEY,
                                    partner_id BIGINT NOT NULL,
                                    transaction_id BIGINT NOT NULL,
                                    sale_amount NUMERIC(19,2) NOT NULL,
                                    commission_amount NUMERIC(19,2) NOT NULL,
                                    applied_rule_ids VARCHAR(255),
                                    sale_date TIMESTAMP NOT NULL,
                                    calculated_at TIMESTAMP NOT NULL,

                                    CONSTRAINT fk_partner_commission_partner
                                        FOREIGN KEY (partner_id) REFERENCES partner(id),

                                    CONSTRAINT fk_partner_commission_transaction
                                        FOREIGN KEY (transaction_id) REFERENCES transaction(transaction_id)
);

CREATE INDEX idx_partner_commission_partner_id ON partner_commission(partner_id);

-- =========================
-- PARTNER BILLING SUMMARY
-- =========================
CREATE TABLE partner_billing_summary (
                                         id BIGSERIAL PRIMARY KEY,
                                         partner_id BIGINT NOT NULL,
                                         billing_month DATE NOT NULL,
                                         total_sales NUMERIC(19,2) NOT NULL DEFAULT 0,
                                         total_commission NUMERIC(19,2) NOT NULL DEFAULT 0,
                                         invoice_status VARCHAR(20) NOT NULL,
                                         generated_at DATE NOT NULL,

                                         CONSTRAINT fk_billing_summary_partner
                                             FOREIGN KEY (partner_id) REFERENCES partner(id),

                                         CONSTRAINT uq_partner_month UNIQUE (partner_id, billing_month)
);

-- =========================
-- COMMISSION PARTIAL AGGREGATE
-- =========================
CREATE TABLE commission_partial_aggregate (
                                              id BIGSERIAL PRIMARY KEY,
                                              partner_id BIGINT NOT NULL,
                                              job_execution_id BIGINT NOT NULL,
                                              step_execution_id BIGINT NOT NULL,
                                              commission_amount NUMERIC(19,2) NOT NULL
);

CREATE INDEX idx_commission_partial_partner_id
    ON commission_partial_aggregate(partner_id);

-- =========================
-- SCHEDULED JOB RUN
-- =========================
CREATE TABLE scheduled_job_run (
                                   job_id UUID PRIMARY KEY,
                                   job_name VARCHAR(255) NOT NULL,
                                   last_run TIMESTAMP NOT NULL,
                                   job_status VARCHAR(30) NOT NULL
);
