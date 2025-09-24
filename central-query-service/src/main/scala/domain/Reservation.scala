package ims.central.query
package domain

import java.time.Instant

case class Reservation(id: Int,
                       storeId: Int,
                       sku: String,
                       quantity: Int,
                       status: ReservationStatus,
                       createdAt: Instant)

enum ReservationStatus:
  case PENDING, CONFIRMED, CANCELLED

case class ReservationRequest(sku: String, storeId: Int, quantity: Int)
case class ReservationResponse(id: Int, status: ReservationStatus)