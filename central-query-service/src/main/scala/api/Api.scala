package ims.central.query
package api

import domain.{ReservationRequest, ReservationResponse, Stock, StockQuery, StockResponse}
import service.{ReservationPersistor, StockQueryService}

import cats.effect.IO
import io.circe.generic.auto.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

case class ApiError(message: String)
object ApiError:
  def of[A](message: String): Either[ApiError, A] = Left(ApiError(message))

object Api:
  def endpoints(stockQueryService: StockQueryService, reservationPersistor: ReservationPersistor): List[ServerEndpoint[Any, IO]] =
    val getStock = endpoint.get.in("stock")
      .in(queryParams)
      .out(jsonBody[Seq[StockResponse]])
      .errorOut(jsonBody[ApiError])
      .serverLogicSuccess(params =>
        val query = StockQuery(
          params.getMulti("sku").getOrElse(Seq.empty), 
          params.getMulti("storeId").getOrElse(Seq.empty).map(_.toInt)
        )
        stockQueryService(query)
      )

    val reserveStock = endpoint.post.in("reservation")
      .in(jsonBody[ReservationRequest])
      .out(jsonBody[ReservationResponse])
      .errorOut(jsonBody[ApiError])
      .serverLogic(reservationPersistor(_))
    
    List(getStock)