package ims.central.update
package kafka.heartbeat

import db.Queries

import cats.effect.IO
import doobie.implicits.*
import doobie.util.transactor.Transactor
import fs2.kafka.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*

trait HeartbeatProcessor:
  def process(event: ConsumerRecord[HeartbeatKey, HeartbeatData]): IO[Unit]

object HeartbeatProcessor:
  given Logger[IO] = Slf4jLogger.getLogger
  
  def apply(transactor: Transactor[IO]): HeartbeatProcessor = (event: ConsumerRecord[HeartbeatKey, HeartbeatData]) =>
    val data = event.value
    for 
      result <- Queries
        .updateHeartbeat(data.store_id, data.last_check)
        .transact(transactor)
      _ <- info"Processed kafka event $event"
    yield result
