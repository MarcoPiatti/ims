package ims.central.query
package kafka.reservations

import db.Queries

import cats.effect.IO
import doobie.implicits.*
import doobie.util.transactor.Transactor
import fs2.kafka.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*

trait ReservationResultProcessor:
  def process(event: ConsumerRecord[ReservationResultKey, ReservationResultData]): IO[Unit]

object ReservationResultProcessor:
  given Logger[IO] = Slf4jLogger.getLogger
  
  def apply(transactor: Transactor[IO]): ReservationResultProcessor = 
    (event: ConsumerRecord[ReservationResultKey, ReservationResultData]) =>
      val data = event.value
      for 
        _ <- Queries.updateReservationStatus(data.id, data.status).transact(transactor)
        _ <- info"Processed kafka event $event"
      yield ()
