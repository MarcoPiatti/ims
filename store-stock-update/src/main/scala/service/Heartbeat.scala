package ims.store
package service

import db.Queries

import cats.effect.{IO, OutcomeIO, ResourceIO}
import doobie.*
import doobie.implicits.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*

import scala.concurrent.duration.FiniteDuration

object Heartbeat:
  given Logger[IO] = Slf4jLogger.getLogger
  def start(interval: FiniteDuration, storeId: Int, transactor: Transactor[IO]): ResourceIO[IO[OutcomeIO[Nothing]]] =
    val action: IO[Unit] = 
      for
        _ <- Queries.sendHeartbeat(storeId)
          .transact(transactor)
          .handleErrorWith(e => error"DB Heartbeat error: $e")
        _ <- IO.sleep(interval)
      yield ()
    action.foreverM.background