This project is a simple Scala 3 HTTP server using cats-effect, http4s, tapir, and doobie.

Features:
- POST /stock endpoint to insert SKU and quantity into store_db stock and outbox tables in a transaction.

## Usage

1. Edit src/main/resources/application.conf to set your MySQL connection details.
2. Run with `sbt run`.

## Dependencies
- Scala 3
- cats-effect
- http4s
- tapir
- doobie (with MySQL driver)

## Development
- Main entry: src/main/scala/Main.scala
- Endpoint: POST /stock

