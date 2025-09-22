package ims.central.update
package kafka

import db.Queries

import cats.effect.IO
import doobie.implicits.*
import doobie.util.transactor.Transactor
import fs2.kafka.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*

trait StockUpdateProcessor:
  def process(event: ConsumerRecord[StockUpdateKey, StockUpdateData]): IO[Unit]

object StockUpdateProcessor:
  given Logger[IO] = Slf4jLogger.getLogger
  
  def apply(transactor: Transactor[IO]): StockUpdateProcessor = (event: ConsumerRecord[StockUpdateKey, StockUpdateData]) =>
    val data = event.value
    for 
      result <- Queries
      .updateStock(data.store_id, data.sku, data.id, data.quantity, data.created_at)
      .transact(transactor)
      _ <- info"Processed stock update event $event"
    yield result
