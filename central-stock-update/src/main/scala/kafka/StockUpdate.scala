package ims.central.update
package kafka

case class StockUpdateKey(storeId: Int, sku: String)
case class StockUpdateData(id: Int, storeId: Int, sku: String, quantity: Int, createdAt: String)