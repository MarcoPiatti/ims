package ims.central.update
package kafka.heartbeat

case class HeartbeatKey(store_id: Int)
case class HeartbeatData(store_id: Int, last_check: String)