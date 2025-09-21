package ims.central.update
package db

import cats.free.Free
import doobie.*
import doobie.implicits.*
import doobie.mysql._

object Queries {
  def addStock(storeId: Int, sku: String, id: Int, addedQuantity: Int, createdAt: String): ConnectionIO[Unit] =
    val query = for
      _ <- sql"""
        INSERT INTO transactions (sku, store_id, id, quantity, created_at)
        VALUES ($sku, $storeId, $id, $addedQuantity, $createdAt)
      """.update.run
      
      _ <- sql"""
        INSERT INTO stock (sku, store_id, quantity) VALUES ($sku, $storeId, $addedQuantity)
        ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)
      """.update.run
    yield ()
    
    query.exceptSomeSqlState {
      case "23000" => Free.pure(())
    }
}
