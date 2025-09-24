package ims.central.query
package service

import api.ApiError
import db.Queries
import domain.*
import domain.ReservationStatus.PENDING
import kafka.KafkaSender
import kafka.reservations.{ReservationData, ReservationKey}

import cats.data.EitherT
import cats.effect.IO
import doobie.*
import doobie.implicits.*

trait ReservationPersistor:
  def apply(stock: ReservationRequest): IO[Either[ApiError, ReservationResponse]]

object ReservationPersistor:
  def apply(minimumStock: Int,
            transactor: Transactor[IO],
            kafkaSender: KafkaSender[ReservationKey, ReservationData]
           ): ReservationPersistor = (request: ReservationRequest) =>
    val transaction = 
      for 
        stock <- EitherT.fromOptionF(
          Queries.singleStock(request.storeId, request.sku), 
          ApiError(s"Stock not found for storeId: ${request.storeId}, sku: ${request.sku}")
        )
        _ <- EitherT.cond[ConnectionIO](stock >= request.quantity + minimumStock, (),
          ApiError(s"Not enough stock. reservation quantity: ${request.quantity}, available stock: ${stock - minimumStock}"),
        )
        id <- EitherT.right(Queries.createReservation(request.storeId, request.sku, request.quantity, PENDING))
      yield ReservationResponse(id, PENDING)

    val flow = for
      _ <- EitherT.cond[IO](request.quantity > 0, (), ApiError("Quantity must be greater than zero"))
      reservationResponse <- transaction.transact(transactor)
      _ <- EitherT.right(kafkaSender.send(
        ReservationKey(request.storeId),
        ReservationData(reservationResponse.id, request.storeId, request.sku, request.quantity)
      ))
    yield reservationResponse
    flow.value