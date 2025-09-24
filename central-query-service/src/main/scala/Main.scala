package ims.central.query

import api.Api
import config.Config
import db.Db
import kafka.Kafka
import kafka.reservations.{ReservationData, ReservationKey}
import server.{Routes, Server}
import service.{ReservationPersistor, StockQueryService}

import cats.effect.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.circe.generic.auto.*


object Main extends IOApp.Simple:
  given Logger[IO] = Slf4jLogger.getLogger
  
  def run: IO[Unit] =
    val config = Config.load()
    val app = 
      for
        transactor <- Db.transactor(config.db)
        kafkaSender <- Kafka.sender[ReservationKey, ReservationData](config.kafka.reservations)
        stockQueryService = StockQueryService(config.reservation.minimumStock, config.store.syncLimit, transactor)
        reservationPersistor = ReservationPersistor(config.reservation.minimumStock, transactor, kafkaSender)
        apiEndpoints = Api.endpoints(stockQueryService, reservationPersistor)
        routes = Routes.all(apiEndpoints)
        server <- Server(config.server, routes)
      yield server
      
    app.useForever.as(ExitCode.Success)