-- ============================================
-- Digital Notice Board - Database Setup
-- ============================================

CREATE DATABASE IF NOT EXISTS digital_notice_board;
USE digital_notice_board;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'student') NOT NULL,
    student_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notice Table
CREATE TABLE IF NOT EXISTS notices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category ENUM('General', 'Academic', 'Event', 'Exam', 'Holiday', 'Sports', 'Other') DEFAULT 'General',
    priority ENUM('Low', 'Medium', 'High', 'Urgent') DEFAULT 'Medium',
    attachment_name VARCHAR(255),
    attachment_data LONGBLOB,
    attachment_type VARCHAR(100),
    posted_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (posted_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Insert default admin account (password: admin123)
INSERT INTO users (name, email, password, role) VALUES
('Administrator', 'admin@noticeboard.com', 'admin123', 'admin')
ON DUPLICATE KEY UPDATE name=name;

-- Insert a sample student (password: student123)
INSERT INTO users (name, email, password, role, student_id) VALUES
('John Student', 'john@student.com', 'student123', 'student', 'STU001')
ON DUPLICATE KEY UPDATE name=name;

-- Insert sample notices
INSERT INTO notices (title, content, category, priority, posted_by) VALUES
('Welcome to Digital Notice Board', 'Welcome to our new Digital Notice Board system. All important announcements will be posted here.', 'General', 'High', 1),
('Mid-Semester Exams Schedule', 'Mid-semester examinations will begin from next Monday. Please check the detailed schedule on the website.', 'Exam', 'Urgent', 1),
('Annual Sports Day', 'Annual Sports Day will be held on 15th December. All students are encouraged to participate.', 'Sports', 'Medium', 1),
('Holiday Notice', 'The college will remain closed on account of National Holiday. Normal schedule resumes the next working day.', 'Holiday', 'Low', 1),
('Library Extended Hours', 'Library will remain open until 10 PM during examination period for student convenience.', 'Academic', 'Medium', 1);
