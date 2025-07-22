# File Store Service

A flexible and operating-system-independent file management service built with Spring Boot.  
This project enables organizations to manage files and directories based on user permissions, while also tracking every file operation in a PostgreSQL database.

## Project Goal

To develop a robust, permission-aware, and OS-independent file management service for organizations. The system allows users to perform operations such as directory creation, file uploading, deleting, moving, and copying — all based on user-specific roles and access levels.

---

## Functional Requirements

- Organization users can create new directories.
- File and directory operations are restricted based on user permissions (read, write, delete).
- Users can:
  - Upload files into directories
  - Delete files and directories
  - Move files and directories
  - Copy files between directories
- For each file or directory added or updated, the following metadata is stored in the database:
  - File name
  - File type (extension)
  - File size
  - Creation date
  - Last modified date
  - Created by user
  - Modified by user
- Users can list directories and their subdirectories.
- Authorized users can move or copy files between directories.

---

## Technical Requirements

- Platform-independent (runs on any OS)
- Built using **Spring Boot**
- Exposes **RESTful APIs**
- File storage options:
  - Cloud-based storage service (e.g., S3)
  - Organization-defined file system
- **PostgreSQL** is used for data persistence

---

## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Swagger / OpenAPI
- Gradle

---

## Author

**Eda Kocaman**  
GitHub: [@EdaKocaman](https://github.com/EdaKocaman)

---
