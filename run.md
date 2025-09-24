## Running the project

------

Docker Engine must be installed on your machine.
Then you can run the following command under this working directory:

```bash
docker compose up -d
```

This will start all the project containers in detached mode.

To stop all containers:

```bash
docker compose down
```

To check the health and logs of your containers:

```bash
docker compose ps
docker compose logs <service-name>
```

----
### Services and endpoints

If a service is not healthy, check its logs and ensure the healthcheck endpoint is returning HTTP 200 OK.

You can check the store service API docs at:

    http://localhost:9300/docs

And the central service API docs at:

    http://localhost:9301/docs

-----
### Kafka
You can view information on kafka-ui at:

    http://localhost:8080

-----
### Databases

Having the mysql client installed.
You can access the local store database remotely with the following command:

```bash
mysql -h 127.0.0.1 -P 3306 -u root -pdebezium
```

To access the central database:

```bash
mysql -h 127.0.0.1 -P 3307 -u root -pcentralpw
```

Or else you can access the databases through the docker containers:

```bash
docker exec -it <store-mysql-container-id> mysql -u root -pdebezium
```
```bash
docker exec -it <central-mysql-container-id> mysql -u root -pcentralpw
```
