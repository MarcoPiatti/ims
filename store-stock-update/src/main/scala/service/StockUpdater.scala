package ims.store
package service

import api.ApiError
import db.Queries
import domain.StockUpdate

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import doobie.*
import doobie.implicits.*

trait StockUpdater:
  def apply(stock: StockUpdate): IO[Either[ApiError, StockUpdate]]

object StockUpdater:
  def apply(storeId: Int, transactor: Transactor[IO]): StockUpdater = (stock: StockUpdate) =>
    val flow =
      for
        quantity <- Queries.stock(stock.sku)
        newQuantity = quantity + stock.quantity
        result <- if newQuantity >= 0
                    then Queries.updateStock(storeId, stock.sku, newQuantity).map(Right(_))
                    else ApiError.of(s"Not enough stock. Quantity: $quantity, attempted removal: ${stock.quantity}").pure[ConnectionIO]
      yield result
    flow.transact(transactor)
