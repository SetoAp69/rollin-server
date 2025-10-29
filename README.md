# Rollin Server

Backend server for the Rollin Attendance Apps project — part of my thesis work on an Attendance Management System.

This repository contains the server-side implementation that provides APIs and services used by the Rollin mobile and web clients. It is implemented in Kotlin and includes static front-end assets and a Dockerfile for containerized deployments.

Key goals (thesis context)
- Serve as the central backend for recording and managing attendance data.
- Demonstrate a scalable, secure architecture as part of an academic thesis on attendance management systems.
- Provide endpoints for authentication, attendance recording, reporting, and administration.

Core features
- RESTful API for attendance operations (check-in, check-out, session management)
- User and role management (admins, instructors, students)
- Reporting endpoints for attendance summaries and exports
- JWT-based authentication (configurable secret)
- Containerization support via Docker

Tech stack
- Kotlin (server)
- HTML (static assets)
- Docker (deployment)

Quick start (development)
Prerequisites: JDK 11+ or 17+, Docker (optional), and a database (Postgres or other supported DB).

1. Set environment variables (example):
   - DATABASE_URL=postgres://user:password@host:5432/dbname
   - JWT_SECRET=your_jwt_secret_here
   - PORT=8080

2. Build and run locally (Gradle):
   ./gradlew build
   java -jar build/libs/rollin-server.jar

3. With Docker:
   docker build -t rollin-server .
   docker run -e DATABASE_URL="$DATABASE_URL" -e JWT_SECRET="$JWT_SECRET" -p 8080:8080 rollin-server

Testing
- Run tests with: ./gradlew test

Configuration
- Place configuration in environment variables or in a config file as your environment requires. Common variables: DATABASE_URL, JWT_SECRET, PORT.

Development notes
- This repository is maintained as part of an academic thesis. If you are reading this and want to reproduce results from the thesis, check the thesis document or contact the author for dataset and experiment details.

Contributing
- Contributions are welcome. Please open issues to discuss proposed changes before submitting pull requests.

License
- Add your chosen license here (e.g., MIT). If this is part of thesis work with restrictions, note them here.

Contact
- Author: SetoAp69 (https://github.com/SetoAp69)
- Thesis supervisor or contact details can be added here if desired.
