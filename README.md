# Order Service – spring-order-docker

## Overview

The Order Service is one of the core microservices in a supply chain management system. It is responsible for managing customer orders and ensuring the integrity and accuracy of order data by coordinating with other services. Specifically, it validates product availability, verifies inventory levels, calculates order totals, and updates stock levels upon successful order placement.

This service follows a microservices architecture and communicates with two other independent services: the **Product Service** and the **Inventory Service**. It is built using Spring Boot and deployed within a Docker container along with a dedicated MySQL database.

---

## Features

- Full CRUD (Create, Read, Update, Delete) functionality for customer orders.
- Validation of product existence via the Product Service.
- Validation of inventory quantity via the Inventory Service.
- Automatic calculation of total order amount (price × quantity).
- Inventory update to reflect stock deduction after a successful order.

---

## Architecture & Design

This microservice follows the **database-per-service** pattern and uses a dedicated MySQL database (`orderdb`) running in a separate Docker container. Communication with Product and Inventory services is done via REST APIs using Docker container names, enabled through a shared Docker network (`spring-shared-network`).

Each service is independently deployable, making the system scalable and loosely coupled.

---

## Technologies Used

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- MySQL
- Docker

---

## Environment Variables

The following environment variables must be set when running this service in a Docker container:

- `MYSQL_HOST` – Hostname of the MySQL container (e.g., `mysqldb2`)
- `MYSQL_PORT` – MySQL port (default: `3306`)
- `MYSQL_USER` – MySQL username (e.g., `root`)
- `MYSQL_PASSWORD` – MySQL password (e.g., `123456`)

---

## Docker Usage

### Build the Docker Image

```bash
docker build -t spring-order-docker .
