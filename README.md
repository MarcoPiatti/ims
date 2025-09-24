## Inventory Management System

------

https://github.com/MarcoPiatti/ims

This repository contains a simplified protoype of a distributed Inventory Management System.

It demonstrates the use of distributed architectures, event-driven communication, 
and database change data capture (CDC) using Kafka and Debezium.

------
### The problem
- A chain of retail stores mantains a legacy inventory management system.
- Each store operates independently with its own database, which is periodically synchronized to a central database.
- The synchronization takes place every 15 minutes, which leads to eventual consistency issues.
- Customers can view the global stock and make online reservations, using the central database as source of truth.
- The current system is a monolithic backend and the frontend is a legacy web application.

This stock discrepancy issue is causing customer dissatisfaction and lost sales.

------
### The objective

To prototype an improved inventory management system, with a distributed architecture,
that will improve inventory consistency, reduce stock update latency, lower operational costs,
while ensuring security and observability.

------
### Architecture Overview

![Architecture Diagram](./docs/Architecture-diagram.png)

The Inventory Management System is intended for a retail store chain with multiple geographically distant locations
operating independently but sharing a web-based purchase/reservation portal.

Each local store operates idependently, with it's own database, and it's own service to keep it updated.
It is assumed that the store's POS system has the responsibility of notifying the update service on any
real-time purchase or restock.

To be able to see the global stock from a website, all local store stock updates are streamed to a centralized database
using Kafka as the message broker. The data is read by Debezium directly from the database binlog, and sent to kafka.
A separate service reads the messages from Kafka and updates the central database.

Each local system also updates a heartbeat regularly to provide reasurance at the central database
that the data is still fresh, and not stale because of some issue throughout the data path.

On the other side a central service reads the global stock from the central database and provides
an API for the web portal to query the stock.
This service also provides an API for the web portal to make online stock reservations.
These reservations are then asynchronously fulfilled by the relevant local store system, and
a confirmation is sent back to the central service when stock has been properly updated.

----
## Rationale

- Each local store must guarantee continuity of operation, on any network condition.
  - Each local store has it's own database and service to keep it updated.


- The central system should be up to date with the local stores as fast as possible, without impacting on the operation.
  - A message broker is used to stream local updates to the central system.


- The stock synchronization should be reliable and fault tolerant.
  - KafkaConnect/Debezium is used to read the database binlog and stream changes to Kafka.
  - Manual streaming from the local service could present persistence issues if the service goes down.


- The central system should guarantee that the stock information is fresh and not stale.
    - Each local system updates a regular heartbeat through the same data path to provide reasurance at the central database
        that the data is still fresh.
    - The central service defines a SLA on how old the data can be to be considered fresh.
    - The web portal can query all the stock and know what data is fresh or stale.
    - This in turn could be used to inform the customer that the stock 'might not be available'.


- The central system should be able to handle a large number of reservations without impacting on local stores or generating contention.
  - Reservation creation should not be synchronous (recieving request, forwarding it to local store, blocking until response).
  - Concurrent synchronous attempts to reserve the same stock item would lead to contention over database resources.
  - Reservations are created asynchronously, pushed to a topic partitioned by store id, and processed by each local store.
  - Each local store confirms the reservation when stock has been properly updated.


- The central system should minimize overselling stock (reservations that end up cancelled due to lack of stock in store).
  - Overselling happens because many reservations are attempted on a certain amount of stock, unaware of other reservations. 
    - Central service discounts pending reservations from the available stock when queried by the web portal, to account for the expected stock.
  - Overselling happens because in-site purchases can remove stock between reservation creation and fulfillment.
    - Central service discounts a determined amount of the available stock when queried by the web portal, to leave a safety margin for local purchases.

- The developed services should allow for horizontal scaling, improving availability if needed.

----
### Technology Stack
- Services built on Scala 3.7.3, with Typelevel libraries:
  - Cats-Effect for IO
  - Doobie for database access
  - Http4s for http server
  - Fs2-Kafka for kafka client
- MySQL 8.0 as the database engine
- Debezium 2.3 as the CDC tool
- Kafka 3.5 as the message broker
- Docker and Docker-Compose for containerization and orchestration

----
### Project Structure
- `local-store-service`: project folder for each store's service
- `central-query-service`: project folder for the central service, querying global stock and creating reservations.
- `central-update-service`: project folder for the kafka consumer that updates the central database from CDCs.
- `conf`: db schema and debezium configuration files
- `docs`: other documentation files
- `compose.yaml`: docker-compose file to run the project
- `run.md`: run instructions
- `prompts.md`: prompts used during the project

----
### Other reading:
- https://sudhir.io/the-big-little-guide-to-message-queues
- https://medium.com/nagoya-foundation/simple-cdc-with-debezium-kafka-a27b28d8c3b8
- http://debezium.io/blog/2019/02/19/reliable-microservices-data-exchange-with-the-outbox-pattern/
- https://fd4s.github.io/fs2-kafka/docs/consumers
