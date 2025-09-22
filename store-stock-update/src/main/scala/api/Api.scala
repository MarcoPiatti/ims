package ims.store
package api

import domain.StockUpdate
import service.StockUpdater

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
  def endpoints(stockUpdateService: StockUpdater): List[ServerEndpoint[Any, IO]] =
    val postStock = endpoint.post.in("stock")
      .in(jsonBody[StockUpdate])
      .out(jsonBody[StockUpdate])
      .errorOut(jsonBody[ApiError])
      .serverLogic(stockUpdateService(_))
    
    List(postStock)