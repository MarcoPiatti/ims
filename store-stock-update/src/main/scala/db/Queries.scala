package ims.store
package db

import domain.Stock

import doobie.*
import doobie.implicits.*

object Queries {
  def addStock(storeId: Int, sku: String, addedQuantity: Int): ConnectionIO[Stock] =
    for {
      _ <- sql"""
           insert into stock (sku, quantity) values ($sku, $addedQuantity)
           on duplicate key update quantity = quantity + VALUES(quantity)
           """.update.run
      newQuantity <- sql"select quantity from stock where sku = $sku".query[Int].unique
      _ <- sql"insert into outbox_event (store_id, sku, quantity) values ($storeId, $sku, $addedQuantity)".update.run
    } yield Stock(sku, newQuantity)
}
