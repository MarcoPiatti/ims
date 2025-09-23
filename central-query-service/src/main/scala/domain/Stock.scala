package ims.central.query
package domain

import java.time.Instant

case class StockQuery(skuFilter: Seq[String], storeFilter: Seq[Int])

case class Stock(storeId: Int, sku: String, quantity: Int, lastUpdated: Instant)

case class StockResponse(storeId: Int, 
                         sku: String, 
                         quantity: Int, 
                         lastUpdated: Instant,
                         lastSynced: Instant,
                         synced: Boolean)

object StockResponse:
  def of(stock: Stock, lastSynced: Instant, synced: Boolean): StockResponse =
    StockResponse(stock.storeId, stock.sku, stock.quantity, stock.lastUpdated, lastSynced, synced)