## Prompts done during the project
-----

In this section are some of the prompts I used during the project development.

------

### Prompt 1
There is an inventory management system operating in a distributed environment. The goal is to optimize inventory consistency, reduce stock update latency and reduce operational costs, while ensuring security and observability. a company mantains an IMS for a chain of retail stores. Each store has a local database that periodically synchronizes with the main database. Customes can view stock online, but inconsistencies and latency in the updates have led to user experience issues and lost sales due to stock discrepancy. The current system has a monolithic backend and a legacy frontend. I am supposed to provide a new distributed architectural design that meets the goal and prioritizes consistency over availability if needed. 

I am thinking of this: 
1) Each store should preserve their own database to ensure local continuity of operations 
2) The databases should be backed up against a remote server but mainly for failover reasons 
3) To improve on the 15 minute synchronization window, each store's software that updates the local DB with increased or decreased stock (such as every purchase or restocking) should push the purchase or restock as a message through a queue, which will be consumed by the central system and pushed into the central DB, ensuring data is synchronized. This will make updates almost real time. 
4) To prevent stock discrepancies in case of a local node failure, or network failures, the central system should keep a conectivity check on the local nodes (like a healthcheck), and in case of no response, the central system should disable showing stock from that store from online customers. 

Caveats: There is no explanation on what type of database technology is currently used, i simply assumed a typical SQL RDBMS. 

I am thinking point 3) could also be resolved by setting up a cluster between databases, like galera for mariadb for example, that keeps all database nodes in sync in real time, but I've heard it has drawbacks. What considerations could i be missing? What technologies would be a good use for this situation?

------

### Prompt 2

I would be interested in clients being able to buy online, from a central system. 
Im thinking the central system should serve as a selector switch, whenever a purchase is made, the central system is told to discount stock from a specific store, sending the message to the store.

------

### Prompt 3

should i set up one kafka instance per store? or one kafka instance that will act as a broker for all stores

------

### Prompt 4

Should i implement outbox manually for my prototype or implement it with an existing solution?

------

### Prompt 5

this design must keep horizontal scaling in mind, understanding many instances of my consumer service will be running and reading from the same topic. is ordering still guaranteed?

------

### Prompt 6

I have this debezium configuration to spin up a debezium mysql container how can i configure debezium to use the outbox pattern?

------

### Prompt 7

i will use tombstone to make inserts into central database from messages received from kafka idempotent, checking if the id corresponds with the message

------

### Prompt 8

can i make the outbox simpler and just contain id, store_id, sku and quantity? all events will be updates (positive or negative) and the outbox id will be store_id-sku-increment

------

### Prompt 9

im back to the drawing board. Would it be better for the outbox events to represent the new total value of stock instead of an increase/decrease? that way only the last event needs to be processed

------

### Prompt 10

if i have a chain of retail stores, do they all share the same sku?

-----

### Prompt 11

I had thought that implementing a health check against the local store update systems in the service that consumes the main database would help detecting if the data is potentially out of sync (since the service is down, not pushing to the local db, and not reaching debezium, in turn not reaching kafka, and not reaching the consumer that pushes it to the central DB) but i realize my mistake lies in that there are many points of failure (the store update service, the db, debezium, kafka, the kafka consumer service, and the network between each instance) So what fault tolerance mechanism could i implement to ensure data is up to date or "potentially desincronized"? i want to inform in my api if the stocks i offer are unavailable or not.

------

### Prompt 12

the problem is what happens if a specific store stock is not bought or restocked? it will not have any event changes

-------

### Prompt 13

now this works as intended, serving online customers the up-to-date stock. now, i could make customers buy online and send that request to the chosen store, and if stock is not enough, to just fail. but i want a more recoverable situation. I was told I should prioritize consistency over availability (or reverse) justifying my decision. I believe every business will try to do whatever they can to sell and they will not be happy missing on sales, so i am trying to think of a way to let online customers buy from the stores and failing gracefully. how do real ims usually do this? would it be reasonable to keep stock and also a table of reservations for online purchases? could I maybe only display availability for online purchase only over a specific amount of stock?

-----

### Prompt 14

how should this integrate into my project should each store count with its own reservation table? syncing their reservations with the main central db that has all reservations? or should online reservations just be a concept handled by the central db

-----

### Prompt 15

what happens when i have reservations, i make a new reservation and live stock is smaller than sum(reservations). i take it that also reservations stop counting once they are fulfilled. when a reservation is attempted to be fulfilled, the live stock will have probably changed, so when the local store tries to doscount the live stock from the reservation once fulfilled it may fail. what about that?

-----

### Prompt 16

should central maybe communicate reservation and fulfillment to local stores over queues, or should it just communicate the action of fulfilling over queues if this communication is through queues how will the local store tell the central system that the fulfillment or reservation failed because of no stock

-----

### Prompt 17

question aside can i set up a kafka instance to forward topic messages to another kafka instance geographically far away? so my services can deliver the message to their close kafka and not worry about conectiviy

-----

