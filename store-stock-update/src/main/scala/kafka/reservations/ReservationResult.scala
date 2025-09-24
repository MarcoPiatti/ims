package ims.store
package kafka.reservations

import java.time.Instant

case class ReservationResultKey(id: Int)
case class ReservationResultData(id: Int, status: ReservationStatus)

enum ReservationStatus:
  case PENDING, CONFIRMED, CANCELLED
