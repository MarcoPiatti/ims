package ims.store
package service

import db.Queries
import domain.Stock

import cats.effect.IO
import doobie.*
import doobie.implicits.*

trait StockUpdater:
  def apply(stock: Stock): IO[Either[Unit, Stock]]

object StockUpdater:
  def apply(storeId: Int, transactor: Transactor[IO]): StockUpdater = (stock: Stock) => Queries
    .addStock(storeId, stock.sku, stock.quantity)
    .transact(transactor)
    .map(Right(_))
