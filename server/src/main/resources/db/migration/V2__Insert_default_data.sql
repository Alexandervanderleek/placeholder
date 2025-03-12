-- Insert default roles
INSERT INTO roles (id, name, description) VALUES
    (uuid_generate_v4(), 'ADMIN', 'Administrator with full access'),
    (uuid_generate_v4(), 'DEVELOPER', 'Software developer role'),
    (uuid_generate_v4(), 'PRODUCT_OWNER', 'Product owner role'),
    (uuid_generate_v4(), 'SCRUM_MASTER', 'Scrum master role'),
    (uuid_generate_v4(), 'BUSINESS_ANALYST', 'Business analyst role'),
    (uuid_generate_v4(), 'QA', 'Quality assurance role');

-- Insert default task statuses
INSERT INTO task_statuses (id, name, display_order) VALUES
    (uuid_generate_v4(), 'BACKLOG', 1),
    (uuid_generate_v4(), 'TODO', 2),
    (uuid_generate_v4(), 'IN_PROGRESS',  3),
    (uuid_generate_v4(), 'REVIEW',  4),
    (uuid_generate_v4(), 'DONE',  5);

-- Insert default task priorities
INSERT INTO task_priorities (id, name, value) VALUES
    (uuid_generate_v4(), 'LOW',  1),
    (uuid_generate_v4(), 'MEDIUM',  2),
    (uuid_generate_v4(), 'HIGH',  3),
    (uuid_generate_v4(), 'CRITICAL',  4);
