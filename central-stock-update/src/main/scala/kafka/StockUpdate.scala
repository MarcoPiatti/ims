package ims.central.update
package kafka

case class StockUpdateKey(store_id: Int, sku: String)
case class StockUpdateData(id: Int, store_id: Int, sku: String, quantity: Int, created_at: String)