This project is a simple Scala 3 HTTP server using cats-effect, http4s, tapir, and doobie.

Features:
- POST /stock endpoint to insert SKU and quantity into store_db stock and outbox tables in a transaction.
