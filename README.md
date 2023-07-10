# MSSC Shopping

MSSC (Micro Services with Spring Cloud) project, to follow along Udemy course and serve as a kind of PoC (Proof of
Concept) for future projects based on microservices.

## Services

| Service name                                                          | Port |
|-----------------------------------------------------------------------|------|
| [mssc-beer-service](./services/mssc-beer-service)                     | 9031 |
| [mssc-beer-order-service](./services/mssc-beer-order-service)         | 9032 |
| [mssc-beer-inventory-service](./services/mssc-beer-inventory-service) | 9033 |

## Database

Run `docker run --name mssc-brewery-database -e MYSQL_ROOT_PASSWORD=1234 -d mysql:8` to use a local MySQL database.

## Message

Run `docker run --name mssc-brewery-queue -d activemq:latest` to use a local ActiveMQ Artemis instance.
