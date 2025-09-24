package ims.store

import api.Api
import config.Config
import db.Db
import server.{Routes, Server}
import service.{Heartbeat, StockUpdater}
import kafka.reservations.ReservationStreaming

import cats.effect.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


object Main extends IOApp.Simple:
  given Logger[IO] = Slf4jLogger.getLogger
  
  def run: IO[Unit] =
    val config = Config.load()
    val app = 
      for transactor <- Db.transactor(config.db)
          _ <- Heartbeat.start(config.heartbeat.interval, config.store.id, transactor)
          stockUpdater = StockUpdater(config.store.id, transactor)
          _ <- ReservationStreaming.start(config.store.id, config.kafka.reservations, config.kafka.reservationResults, stockUpdater)
          apiEndpoints = Api.endpoints(stockUpdater)
          routes = Routes.all(apiEndpoints)
          server <- Server(config.server, routes)
      yield server
      
    app.useForever.as(ExitCode.Success)