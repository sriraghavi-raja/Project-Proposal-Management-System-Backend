-- Sample data for Project Proposal Management System

-- Insert sample departments
INSERT INTO departments (name, code, description, location, phone, email, is_active, created_date) VALUES
('Computer Science', 'CS', 'Department of Computer Science and Engineering', 'Building A, Floor 3', '123-456-7890', 'cs@university.edu', true, NOW()),
('Electrical Engineering', 'EE', 'Department of Electrical Engineering', 'Building B, Floor 2', '123-456-7891', 'ee@university.edu', true, NOW()),
('Mechanical Engineering', 'ME', 'Department of Mechanical Engineering', 'Building C, Floor 1', '123-456-7892', 'me@university.edu', true, NOW()),
('Biology', 'BIO', 'Department of Biology', 'Building D, Floor 2', '123-456-7893', 'bio@university.edu', true, NOW()),
('Chemistry', 'CHEM', 'Department of Chemistry', 'Building E, Floor 3', '123-456-7894', 'chem@university.edu', true, NOW());

-- Insert sample users
INSERT INTO users (username, email, password_hash, first_name, last_name, role, department, organization_id, phone_number, office_location, expertise_areas, created_date, is_active, email_verified, two_factor_enabled) VALUES
('admin', 'admin@university.edu', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'System', 'Administrator', 'ADMIN', 'Administration', 'ADM001', '123-456-0001', 'Admin Building', 'System Administration', NOW(), true, true, false),
('john.doe', 'john.doe@university.edu', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'John', 'Doe', 'PRINCIPAL_INVESTIGATOR', 'Computer Science', 'CS001', '123-456-0002', 'CS Building 301', 'Machine Learning, AI', NOW(), true, true, false),
('jane.smith', 'jane.smith@university.edu', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Jane', 'Smith', 'REVIEWER', 'Electrical Engineering', 'EE001', '123-456-0003', 'EE Building 201', 'Signal Processing, IoT', NOW(), true, true, false),
('bob.wilson', 'bob.wilson@university.edu', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Bob', 'Wilson', 'DEPARTMENT_HEAD', 'Computer Science', 'CS002', '123-456-0004', 'CS Building 305', 'Computer Networks, Security', NOW(), true, true, false),
('alice.brown', 'alice.brown@university.edu', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Alice', 'Brown', 'PROJECT_MANAGER', 'Administration', 'PM001', '123-456-0005', 'Admin Building 205', 'Project Management', NOW(), true, true, false),
('charlie.davis', 'charlie.davis@university.edu', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Charlie', 'Davis', 'FINANCE', 'Finance', 'FIN001', '123-456-0006', 'Finance Building 101', 'Financial Analysis', NOW(), true, true, false);

-- Insert sample proposals
INSERT INTO proposals (title, abstract, principal_investigator_id, co_investigators, project_type, funding_agency, requested_amount, project_duration_months, submission_deadline, submission_date, status, project_stage, priority_level, department_id, created_by, last_modified) VALUES
('AI-Powered Student Learning Analytics Platform', 'Development of an intelligent analytics platform to track and improve student learning outcomes using machine learning algorithms.', 2, 'Dr. Sarah Johnson, Prof. Mike Chen', 'RESEARCH', 'National Science Foundation', 150000.00, 24, '2024-03-15', NOW(), 'SUBMITTED', 'Initial Review', 'HIGH', 1, 2, NOW()),
('Smart Grid Optimization using IoT Sensors', 'Research project to optimize electrical grid performance using Internet of Things sensors and real-time data analysis.', 3, 'Dr. Tom Wilson, Dr. Lisa Zhang', 'RESEARCH', 'Department of Energy', 200000.00, 36, '2024-04-20', NOW(), 'UNDER_REVIEW', 'Technical Review', 'MEDIUM', 2, 3, NOW()),
('Sustainable Manufacturing Process Innovation', 'Development of eco-friendly manufacturing processes to reduce industrial waste and energy consumption.', 2, 'Prof. Emily Clark', 'DEVELOPMENT', 'Environmental Protection Agency', 120000.00, 18, '2024-02-28', NOW(), 'DRAFT', 'Preparation', 'MEDIUM', 3, 2, NOW());

-- Insert sample budget items
INSERT INTO budget_items (proposal_id, category, description, amount, justification, year_number, created_date) VALUES
-- Budget for AI Learning Analytics Platform (Proposal ID: 1)
(1, 'PERSONNEL', 'Principal Investigator (50% effort)', 45000.00, '50% effort for 24 months at $45k/year', 1, NOW()),
(1, 'PERSONNEL', 'Graduate Research Assistant (100% effort)', 30000.00, 'Full-time GRA for research and development', 1, NOW()),
(1, 'EQUIPMENT', 'High-performance computing cluster', 25000.00, 'Required for machine learning model training', 1, NOW()),
(1, 'SUPPLIES', 'Software licenses and cloud services', 15000.00, 'ML frameworks and cloud computing resources', 1, NOW()),
(1, 'TRAVEL', 'Conference presentations', 5000.00, 'Present research findings at 2 conferences', 1, NOW()),
-- Year 2 budget items
(1, 'PERSONNEL', 'Principal Investigator (50% effort)', 45000.00, '50% effort for second year', 2, NOW()),
(1, 'PERSONNEL', 'Graduate Research Assistant (100% effort)', 30000.00, 'Full-time GRA for second year', 2, NOW());

-- Insert sample evaluations
INSERT INTO evaluations (proposal_id, reviewer_id, evaluation_stage, overall_score, technical_score, innovation_score, feasibility_score, budget_score, impact_score, comments, recommendation, evaluation_date, is_final, conflict_of_interest) VALUES
(1, 3, 'Initial Technical Review', 8.5, 9.0, 8.0, 8.5, 8.0, 9.0, 'Excellent technical approach with strong innovation potential. Budget is reasonable for the scope of work.', 'APPROVE', NOW(), true, false),
(2, 2, 'Technical Evaluation', 7.5, 8.0, 7.0, 7.5, 8.0, 7.5, 'Good project with practical applications. Some concerns about timeline feasibility.', 'MINOR_REVISIONS', NOW(), false, false);

-- Insert sample projects (for approved proposals)
INSERT INTO projects (proposal_id, project_number, start_date, end_date, actual_start_date, status, completion_percentage, budget_utilized, created_date) VALUES
(1, 'PROJ-2024-001', '2024-01-01', '2025-12-31', '2024-01-15', 'ACTIVE', 25.50, 35000.00, NOW());

-- Insert sample notifications
INSERT INTO notifications (user_id, title, message, type, is_read, related_proposal_id, created_date) VALUES
(2, 'Proposal Submitted Successfully', 'Your proposal "AI-Powered Student Learning Analytics Platform" has been submitted for review.', 'SUCCESS', false, 1, NOW()),
(3, 'New Proposal for Review', 'A new proposal "Smart Grid Optimization using IoT Sensors" has been assigned to you for review.', 'INFO', false, 2, NOW()),
(4, 'Budget Review Required', 'Budget review is required for proposal ID: 1', 'WARNING', false, 1, NOW());

-- Insert sample workflows
INSERT INTO workflows (proposal_id, current_stage, assigned_to, due_date, status, comments, created_date) VALUES
(1, 'Technical Review', 3, '2024-01-30 17:00:00', 'COMPLETED', 'Technical review completed with approval recommendation', NOW()),
(1, 'Budget Review', 6, '2024-02-15 17:00:00', 'IN_PROGRESS', 'Currently under budget review', NOW()),
(2, 'Initial Review', 3, '2024-02-20 17:00:00', 'PENDING', 'Assigned for initial technical review', NOW());

-- Insert sample documents
INSERT INTO documents (proposal_id, file_name, file_path, file_size, file_type, document_type, description, uploaded_by, uploaded_date, is_active) VALUES
(1, 'proposal_technical_details.pdf', '/documents/proposals/1/technical_details.pdf', 2048576, 'application/pdf', 'PROPOSAL_DOCUMENT', 'Technical specifications and methodology', 2, NOW(), true),
(1, 'budget_breakdown.xlsx', '/documents/proposals/1/budget.xlsx', 512000, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'BUDGET_DOCUMENT', 'Detailed budget breakdown by category and year', 2, NOW(), true),
(2, 'iot_architecture_diagram.pdf', '/documents/proposals/2/architecture.pdf', 1024000, 'application/pdf', 'TECHNICAL_DOCUMENT', 'System architecture and IoT sensor placement diagram', 3, NOW(), true);

-- Insert sample audit logs
INSERT INTO audit_logs (user_id, action, table_name, record_id, new_values, ip_address, user_agent, created_date) VALUES
(2, 'CREATE', 'proposals', 1, '{"title":"AI-Powered Student Learning Analytics Platform","status":"DRAFT"}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', NOW()),
(2, 'UPDATE', 'proposals', 1, '{"status":"SUBMITTED"}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', NOW()),
(3, 'CREATE', 'evaluations', 1, '{"proposal_id":1,"overall_score":8.5,"recommendation":"APPROVE"}', '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', NOW());