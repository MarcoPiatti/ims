package ims.store
package kafka.reservations

import db.Queries

import cats.effect.IO
import doobie.implicits.*
import doobie.util.transactor.Transactor
import fs2.kafka.*
import ims.store.domain.StockUpdate
import ims.store.service.StockUpdater
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*

trait ReservationResultProcessor:
  def process(event: ConsumerRecord[ReservationKey, ReservationData]): IO[Unit]

object ReservationResultProcessor:
  given Logger[IO] = Slf4jLogger.getLogger
  
  def apply(stockUpdater: StockUpdater): ReservationResultProcessor = 
    (event: ConsumerRecord[ReservationKey, ReservationData]) =>
      val data = event.value
      for 
        _ <- stockUpdater(StockUpdate(data.sku, data.quantity))
        _ <- info"Processed kafka event $event"
      yield ()
