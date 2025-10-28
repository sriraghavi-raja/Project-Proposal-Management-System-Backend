-- =====================================================
-- Fix Missing User Names
-- =====================================================
-- This script updates users who don't have firstName/lastName populated
-- Use this if you encounter display issues in the AssignReviewersModal

-- Check current state
SELECT 
    id, 
    username, 
    email, 
    first_name, 
    last_name, 
    role 
FROM users 
WHERE first_name IS NULL OR first_name = '' 
   OR last_name IS NULL OR last_name = '';

-- Option 1: Set firstName to username and lastName to empty string
-- (Good for quick fix)
UPDATE users 
SET 
    first_name = username,
    last_name = 'User'
WHERE first_name IS NULL OR first_name = '';

-- Option 2: Extract name from email (if email is firstname.lastname@domain.com)
-- (More sophisticated)
UPDATE users 
SET 
    first_name = CONCAT(
        UPPER(SUBSTRING(SUBSTRING_INDEX(email, '@', 1), 1, 1)),
        SUBSTRING(SUBSTRING_INDEX(SUBSTRING_INDEX(email, '@', 1), '.', 1), 2)
    ),
    last_name = CONCAT(
        UPPER(SUBSTRING(SUBSTRING_INDEX(SUBSTRING_INDEX(email, '@', 1), '.', -1), 1, 1)),
        SUBSTRING(SUBSTRING_INDEX(SUBSTRING_INDEX(email, '@', 1), '.', -1), 2)
    )
WHERE 
    (first_name IS NULL OR first_name = '')
    AND email LIKE '%.%@%';

-- Option 3: Set default names for specific roles
UPDATE users 
SET 
    first_name = CASE 
        WHEN role = 'REVIEWER' THEN 'Reviewer'
        WHEN role = 'COMMITTEE_CHAIR' THEN 'Committee'
        WHEN role = 'ADMIN' THEN 'Admin'
        ELSE 'User'
    END,
    last_name = username
WHERE first_name IS NULL OR first_name = '';

-- Verify the update
SELECT 
    id, 
    username, 
    email, 
    first_name, 
    last_name, 
    role 
FROM users 
ORDER BY role, id;

-- =====================================================
-- Create Complete Test Users with All Fields
-- =====================================================
-- These users have all fields populated for testing

-- Delete old test users if they exist
DELETE FROM users WHERE username LIKE 'test_%';

-- Insert complete test users
INSERT INTO users (
    username, 
    email, 
    password_hash, 
    first_name, 
    last_name, 
    role, 
    is_active, 
    email_verified
) VALUES
    -- Committee Chair
    (
        'test_chair', 
        'chair@example.com', 
        '$2a$10$YourBcryptHashHere', 
        'Sarah', 
        'Johnson', 
        'COMMITTEE_CHAIR', 
        TRUE, 
        TRUE
    ),
    
    -- Reviewers
    (
        'test_reviewer1', 
        'reviewer1@example.com', 
        '$2a$10$YourBcryptHashHere', 
        'Michael', 
        'Chen', 
        'REVIEWER', 
        TRUE, 
        TRUE
    ),
    (
        'test_reviewer2', 
        'reviewer2@example.com', 
        '$2a$10$YourBcryptHashHere', 
        'Emily', 
        'Rodriguez', 
        'REVIEWER', 
        TRUE, 
        TRUE
    ),
    (
        'test_reviewer3', 
        'reviewer3@example.com', 
        '$2a$10$YourBcryptHashHere', 
        'David', 
        'Williams', 
        'REVIEWER', 
        TRUE, 
        TRUE
    ),
    (
        'test_reviewer4', 
        'reviewer4@example.com', 
        '$2a$10$YourBcryptHashHere', 
        'Jessica', 
        'Taylor', 
        'REVIEWER', 
        TRUE, 
        TRUE
    ),
    
    -- Principal Investigator
    (
        'test_pi', 
        'pi@example.com', 
        '$2a$10$YourBcryptHashHere', 
        'Robert', 
        'Anderson', 
        'PRINCIPAL_INVESTIGATOR', 
        TRUE, 
        TRUE
    );

-- Verify test users created
SELECT 
    id, 
    username, 
    email, 
    first_name, 
    last_name, 
    role 
FROM users 
WHERE username LIKE 'test_%';

-- =====================================================
-- Generate Proper Bcrypt Password Hashes
-- =====================================================
-- NOTE: You cannot generate bcrypt hashes directly in MySQL
-- Use one of these methods:

-- Method 1: Generate from Spring Boot application
-- Run this Java code snippet:
-- String hash = new BCryptPasswordEncoder().encode("password123");
-- System.out.println(hash);

-- Method 2: Use online bcrypt generator
-- Visit: https://bcrypt-generator.com/
-- Enter: password123
-- Rounds: 10
-- Copy the hash and replace '$2a$10$YourBcryptHashHere' above

-- Method 3: Create users through the API
-- POST /api/auth/register
-- Let the application handle password hashing

-- Example hash for "password123" (10 rounds):
-- $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- Update existing users with proper hash
-- UPDATE users 
-- SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
-- WHERE username LIKE 'test_%';

-- =====================================================
-- Clean Up Orphaned Assignments
-- =====================================================
-- Remove assignments if users were deleted

DELETE FROM proposal_reviewers 
WHERE reviewer_id NOT IN (SELECT id FROM users);

DELETE FROM proposal_reviewers 
WHERE proposal_id NOT IN (SELECT id FROM proposals);

-- Verify data integrity
SELECT 
    pr.id,
    pr.proposal_id,
    p.title AS proposal_title,
    pr.reviewer_id,
    CONCAT(u.first_name, ' ', u.last_name) AS reviewer_name,
    pr.status,
    pr.due_date
FROM proposal_reviewers pr
JOIN proposals p ON pr.proposal_id = p.id
JOIN users u ON pr.reviewer_id = u.id
ORDER BY pr.assigned_date DESC;
