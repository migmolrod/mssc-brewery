# MSSC Brewery

### Build status

| Develop | [![CircleCI](https://dl.circleci.com/status-badge/img/gh/migmolrod/mssc-brewery/tree/develop.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/migmolrod/mssc-brewery/tree/develop) |
|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Master  | [![CircleCI](https://dl.circleci.com/status-badge/img/gh/migmolrod/mssc-brewery/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/migmolrod/mssc-brewery/tree/master)   |

MSSC (Micro Services with Spring Cloud) project, to follow along Udemy course and serve as a kind of PoC (Proof of
Concept) for future projects based on microservices.

## Services

| Service name                                                          | Port |
|-----------------------------------------------------------------------|------|
| [mssc-beer-service](./services/mssc-beer-service)                     | 9031 |
| [mssc-beer-order-service](./services/mssc-beer-order-service)         | 9032 |
| [mssc-beer-inventory-service](./services/mssc-beer-inventory-service) | 9033 |

## Infrastructure

| Service name                                         | Port |
|------------------------------------------------------|------|
| [mssc-brewery-gateway](./cloud/mssc-brewery-gateway) | 9090 |


## Database

Run `docker run --name mssc-brewery-database -e MYSQL_ROOT_PASSWORD=1234 -d mysql:8` to use a local MySQL database.

## Message

Run `docker run -d --name activemq-artemis -p 61616:61616 -p 8161:8161 -e ARTEMIS_USER=admin -e ARTEMIS_PASSWORD=admin makyo/activemq-artemis` to use a local ActiveMQ Artemis instance.
