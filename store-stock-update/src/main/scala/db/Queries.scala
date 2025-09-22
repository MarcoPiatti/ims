package ims.store
package db

import domain.StockUpdate

import doobie.*
import doobie.implicits.*

object Queries {
  def stock(sku: String): ConnectionIO[Int] =
    sql"select quantity from stock where sku = $sku".query[Int].option.map(_.getOrElse(0))

  def updateStock(storeId: Int, sku: String, quantity: Int): ConnectionIO[StockUpdate] =
    for {
      _ <- sql"""
        insert into stock (sku, quantity) values ($sku, $quantity)
        on duplicate key update quantity = VALUES(quantity)
      """.update.run
      _ <- sql"insert into outbox_event (store_id, sku, quantity) values ($storeId, $sku, $quantity)".update.run
    } yield StockUpdate(sku, quantity)
}
