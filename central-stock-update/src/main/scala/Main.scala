package ims.central.update

import config.Config
import db.Db
import kafka.heartbeat.HeartbeatProcessor
import kafka.stockUpdate.{StockUpdateData, StockUpdateKey, StockUpdateProcessor}
import kafka.{ChunkProcessor, Kafka}

import cats.effect.{ExitCode, IO, IOApp}
import io.circe.generic.auto.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple:
  given Logger[IO] = Slf4jLogger.getLogger

  def run: IO[Unit] =
    val config = Config.load()
    val app =
      for 
        transactor <- Db.transactor(config.db)
        
        stockUpdateChunkProcessor = ChunkProcessor.last(StockUpdateProcessor(transactor).process)
        stockUpdateConsumer = Kafka.chunkConsumer(config.kafka.stockUpdates, stockUpdateChunkProcessor)
      
        heartbeatChunkProcessor = ChunkProcessor.last(HeartbeatProcessor(transactor).process)
        heartbeatConsumer = Kafka.chunkConsumer(config.kafka.heartbeat, heartbeatChunkProcessor)
      
        _ <- (stockUpdateConsumer, heartbeatConsumer).parTupled.background
      yield ()
    app.useForever.as(ExitCode.Success)  
