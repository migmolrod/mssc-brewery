# MSSC Brewery

### Build status

MSSC (Micro Services with Spring Cloud) project, to follow along Udemy course and serve as a kind of PoC (Proof of
Concept) for future projects based on microservices.

## Microservices

| Service name                                                                            | Port |
|-----------------------------------------------------------------------------------------|------|
| [mssc-beer-service](./services/mssc-beer-service)                                       | 9031 |
| [mssc-beer-order-service](./services/mssc-beer-order-service)                           | 9032 |
| [mssc-beer-inventory-service](./services/mssc-beer-inventory-service)                   | 9033 |



## Failover services

| Service name                                                            | Port |
|-------------------------------------------------------------------------|------|
| [mssc-beer-inventory-failover](./services/mssc-beer-inventory-failover) | 9063 |

## Infrastructure

| Service name                                           | Port |
|--------------------------------------------------------|------|
| [mssc-service-registry](./cloud/mssc-service-registry) | 8761 |
| [mssc-brewery-gateway](./cloud/mssc-api-gateway)       | 9090 |


## Database

Run `docker run --name mssc-brewery-database -e MYSQL_ROOT_PASSWORD=1234 -d mysql:8` to use a local MySQL database.

## Message

Run `docker run -d --name activemq-artemis -p 61616:61616 -p 8161:8161 -e ARTEMIS_USER=admin -e ARTEMIS_PASSWORD=admin makyo/activemq-artemis` to use a local ActiveMQ Artemis instance.
