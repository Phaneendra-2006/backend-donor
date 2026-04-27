-- Food Donation System database setup
-- Run this in MySQL Workbench using an admin/root connection.

CREATE DATABASE IF NOT EXISTS food_donation_db;

CREATE USER IF NOT EXISTS 'foodapp_user'@'localhost' IDENTIFIED BY 'FoodApp@123';

GRANT ALL PRIVILEGES ON food_donation_db.* TO 'foodapp_user'@'localhost';
FLUSH PRIVILEGES;

-- Optional verification
SHOW DATABASES LIKE 'food_donation_db';
SHOW GRANTS FOR 'foodapp_user'@'localhost';
