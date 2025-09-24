package ims.central.query
package kafka.reservations

import ims.central.query.domain.ReservationStatus

import java.time.Instant

case class ReservationResultKey(id: Int)
case class ReservationResultData(id: Int, status: ReservationStatus)