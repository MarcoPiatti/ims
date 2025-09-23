package ims.central.query
package db

import domain.{Stock, StockQuery, StockResponse}

import doobie.*
import doobie.implicits.*
import doobie.implicits.legacy.instant.JavaTimeInstantMeta

import java.time.Instant

object Queries {
  def stock(stockQuery: StockQuery): ConnectionIO[Seq[Stock]] =
    val base = fr"""select store_id, sku, quantity, last_updated from stock"""
    val skuFilter = Fragments.inOpt(fr"sku", stockQuery.skuFilter)
    val storeFilter = Fragments.inOpt(fr"store_id", stockQuery.storeFilter)
    val finalQuery = base ++ Fragments.whereAndOpt(skuFilter, storeFilter)
    
    finalQuery.query[Stock].to[Seq]
    
  val storesLastHeartbeat: ConnectionIO[Map[Int, Instant]] =
    sql"select store_id, last_check from store_availability".query[(Int, Instant)].to[List].map(_.toMap)
}
