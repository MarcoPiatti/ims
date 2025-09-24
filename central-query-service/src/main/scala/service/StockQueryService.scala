package ims.central.query
package service

import db.Queries
import domain.*

import cats.effect.IO
import doobie.*
import doobie.implicits.*

import java.time.Instant
import scala.concurrent.duration.FiniteDuration
import scala.jdk.DurationConverters.JavaDurationOps

trait StockQueryService:
  def apply(stock: StockQuery): IO[Seq[StockResponse]]

object StockQueryService:
  private def isSynced(lastHeartbeat: Instant, now: Instant, syncLimit: FiniteDuration): Boolean =
    lastHeartbeat.until(now).toScala <= syncLimit
  
  def apply(minimumStock: Int, syncLimit: FiniteDuration, transactor: Transactor[IO]): StockQueryService = (stockQuery: StockQuery) =>
    val transaction = 
      for 
        stock <- Queries.stock(stockQuery)
        storeLastHeartbeats <- Queries.storesLastHeartbeat
      yield (stock, storeLastHeartbeats)
      
    for
      (stock, storeLastHeartbeats) <- transaction.transact(transactor) 
      now <- IO.realTimeInstant
      storeStatuses = storeLastHeartbeats
        .view
        .mapValues(it => it -> isSynced(it, now, syncLimit))
        .toMap
    yield stock
      .map(stock =>
        val safeQuantity = (stock.quantity - minimumStock).max(0)
        val (lastHeartbeat, isAvailable) = storeStatuses.getOrElse(stock.storeId, (Instant.EPOCH, false))
        StockResponse.of(stock.copy(quantity = safeQuantity), lastHeartbeat, isAvailable)
      )
      .filterNot(_.quantity == 0)