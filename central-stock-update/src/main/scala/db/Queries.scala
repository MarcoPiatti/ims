package ims.central.update
package db

import cats.implicits.toFunctorOps
import cats.implicits.catsSyntaxApplicativeId
import doobie.*
import doobie.implicits.*
import doobie.implicits.legacy.instant.JavaTimeInstantMeta

import java.time.Instant

object Queries {
  def updateStock(storeId: Int, sku: String, id: Int, quantity: Int, createdAt: Instant): ConnectionIO[Unit] =
    val query = for
      _ <- sql"""
        insert into transactions (sku, store_id, id, quantity, created_at)
        values ($sku, $storeId, $id, $quantity, $createdAt)
      """.update.run

      _ <- sql"""
        insert into stock (sku, store_id, quantity, last_updated) values ($sku, $storeId, $quantity, $createdAt)
        on duplicate key update quantity = VALUES(quantity) and last_updated = VALUES(last_updated)
      """.update.run
    yield ()

    query.exceptSomeSqlState {
      case SqlState("23000") => ().pure
    }

  def updateHeartbeat(storeId: Int, timestamp: Instant): ConnectionIO[Unit] =
    val ts = java.sql.Timestamp.from(timestamp)
    sql"""
      insert into store_availability (store_id, last_check) values ($storeId, $ts)
      on duplicate key update last_check = $ts
    """.update.run.void
}
