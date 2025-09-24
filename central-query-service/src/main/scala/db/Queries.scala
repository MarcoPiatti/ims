package ims.central.query
package db

import domain.{ReservationStatus, Stock, StockQuery, StockResponse}

import cats.syntax.functor.toFunctorOps
import doobie.*
import doobie.implicits.*
import doobie.implicits.legacy.instant.JavaTimeInstantMeta

import java.time.Instant

object Queries {
  def stock(stockQuery: StockQuery): ConnectionIO[Seq[Stock]] =
    val base = fr"""
      select s.store_id,
             s.sku,
             (s.quantity - coalesce(r.quantity, 0)) as quantity,
             s.last_updated
      from stock s
      left join (
        select store_id, sku, sum(quantity) as quantity
        from reservations
        where status = 'PENDING'
        group by store_id, sku
      ) r on r.store_id = s.store_id and r.sku = s.sku
    """
    val skuFilter = Fragments.inOpt(fr"s.sku", stockQuery.skuFilter)
    val storeFilter = Fragments.inOpt(fr"s.store_id", stockQuery.storeFilter)
    val finalQuery = base ++ Fragments.whereAndOpt(skuFilter, storeFilter)
    
    finalQuery.query[Stock].to[Seq]
    
  val storesLastHeartbeat: ConnectionIO[Map[Int, Instant]] =
    sql"select store_id, last_check from store_availability".query[(Int, Instant)].to[List].map(_.toMap)

  def updateReservationStatus(reservationId: Int, status: ReservationStatus): ConnectionIO[Unit] =
    sql"""
      update reservations set status = ${status.toString} where id = $reservationId
    """.update.run.void

  def singleStock(storeId: Int, sku: String): ConnectionIO[Option[Int]] =
    sql"select quantity from stock where store_id = $storeId and sku = $sku".query[Int].option

  def createReservation(storeId: Int, sku: String, quantity: Int, status: ReservationStatus): ConnectionIO[Int] =
    sql"""
      insert into reservations (store_id, sku, quantity, status, created_at)
      values ($storeId, $sku, $quantity, ${status.toString}, ${Instant.now()})
    """.update.withUniqueGeneratedKeys[Int]("id")
}
