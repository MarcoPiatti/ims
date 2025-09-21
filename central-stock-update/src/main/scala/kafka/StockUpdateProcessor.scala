package ims.central.update
package kafka

import config.KafkaConfig
import db.Queries

import cats.effect.{IO, Resource}
import doobie.enumerated.SqlState
import doobie.implicits.*
import doobie.util.transactor.Transactor
import fs2.kafka.*
import fs2.kafka.consumer.KafkaConsumeChunk.CommitNow
import fs2.{Chunk, Stream}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.slf4j.internal.Slf4jLoggerInternal.Slf4jLogger

trait StockUpdateProcessor:
  def process(event: ConsumerRecord[StockUpdateKey, StockUpdateData]): IO[Unit]

object StockUpdateProcessor:
  given Logger[IO] = Slf4jLogger.getLogger
  
  def apply(transactor: Transactor[IO]): StockUpdateProcessor = (event: ConsumerRecord[StockUpdateKey, StockUpdateData]) =>
    val data = event.value
    for 
      result <- Queries
      .addStock(data.storeId, data.sku, data.id, data.quantity, data.createdAt)
      .transact(transactor)
      _ <- info"Processed stock update event $event"
    yield result
