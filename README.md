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
| [mssc-config-server](./cloud/mssc-config-server)       | 9091 |

## External services

| Service type         | Vendor           | Port         | Development credentials |
|----------------------|------------------|--------------|-------------------------|
| Database             | MySQL            | 3306         | root/1234               |
| Messaging            | ActiveMQ Artemis | 8161 (61616) | admin/admin             |
| Distributed tracing  | Zipkin           | 9411         |                         |
| Monitoring           | Prometheus       |              |                         |
| Consolidated tracing | ELK              |              |                         |
|                      |                  |              |                         |

## Database (MySQL)

Run `docker run -d --name mssc-brewery-database -p 3306:3306 -e MYSQL_ROOT_PASSWORD=1234 mysql:8` to use a local MySQL database.

## Messaging (ActiveMQ Artemis)

Run `docker run -d --name mssc-brewery-queue -p 61616:61616 -p 8161:8161 -e ARTEMIS_USER=admin -e ARTEMIS_PASSWORD=admin makyo/activemq-artemis` to use a local ActiveMQ Artemis instance.

## Distributed tracing (Zipkin)

Run `docker run -d --name mssc-brewery-logging -p 9411:9411 openzipkin/zipkin` to use a local Zipkin server.
