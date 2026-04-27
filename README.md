# Food Donation System - Backend

A Spring Boot application for managing food donations and requests between donors and NGOs.

## Features

- User authentication and authorization using JWT
- Role-based access control (Admin, Donor, NGO, User)
- Food donation management
- Request tracking and fulfillment
- Delivery management
- Analytics and reporting
- Email notifications with OTP verification
- Google OAuth integration
- File upload support

## Tech Stack

- **Framework**: Spring Boot
- **Database**: MySQL
- **Security**: Spring Security + JWT
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven
- **APIs**: RESTful APIs

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- MySQL 8.0+

## Installation

1. Clone the repository
```bash
git clone https://github.com/Phaneendra-2006/backend-donor.git
cd backend-donor
```

2. Configure the database in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/food_donation
spring.datasource.username=root
spring.datasource.password=your_password
```

3. Build the project
```bash
mvn clean install
```

4. Run the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Database Setup

Run the SQL script to initialize the database:
```bash
mysql -u root -p < create_database.sql
```

## API Documentation

The API endpoints are organized by controller:
- **Auth Controller**: Authentication and user registration
- **Donor Controller**: Donation management
- **Admin Controller**: Administrative operations
- **NGO Controller**: NGO-specific operations
- **Analytics Controller**: Analytics and reporting

## Project Structure

```
src/main/java/com/foodwaste/
├── config/              # Configuration classes
├── controller/          # REST controllers
├── dto/                 # Data transfer objects
├── entity/              # JPA entities
├── exception/           # Custom exceptions
├── repository/          # Data access layer
├── security/            # Security and JWT configuration
├── service/             # Business logic
└── util/                # Utility classes
```

## Configuration Files

- `pom.xml`: Maven dependencies and build configuration
- `application.properties`: Spring Boot application properties
- `.gitignore`: Git ignore rules

## Contributing

1. Create a feature branch
2. Make your changes
3. Commit your changes
4. Push to the repository
5. Create a Pull Request

## License

This project is licensed under the MIT License.
