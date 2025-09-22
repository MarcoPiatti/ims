package ims.central.update

import config.Config
import db.Db
import kafka.{StockUpdateChunkProcessor, StockUpdateProcessor, StockUpdatesConsumer}

import cats.effect.{IO, IOApp}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


object Main extends IOApp.Simple:
  given Logger[IO] = Slf4jLogger.getLogger

  def run: IO[Unit] =
    val config = Config.load()
    Db.transactor(config.db).use { transactor =>
      val stockUpdateProcessor = StockUpdateProcessor(transactor)
      val stockUpdateChunkProcessor = StockUpdateChunkProcessor(stockUpdateProcessor)
      StockUpdatesConsumer(config.kafka, transactor, stockUpdateChunkProcessor)
    }
