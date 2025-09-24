package ims.store
package kafka.reservations

import java.time.Instant

case class ReservationKey(store_id: Int)

case class ReservationData(id: Int,
                           store_id: Int,
                           sku: String,
                           quantity: Int)

