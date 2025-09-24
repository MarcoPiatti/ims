package ims.central.query
package kafka.reservations

import ims.central.query.domain.ReservationStatus

import java.time.Instant

case class ReservationKey(store_id: Int)

case class ReservationData(id: Int,
                           store_id: Int,
                           sku: String,
                           quantity: Int)
