package ims.store
package api

import domain.Stock
import service.StockUpdater

import cats.effect.IO
import io.circe.generic.auto.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

object Api:
  def endpoints(stockUpdateService: StockUpdater): List[ServerEndpoint[Any, IO]] =
    val postStock = endpoint.post.in("stock")
      .in(jsonBody[Stock])
      .out(jsonBody[Stock])
      .serverLogic(stockUpdateService(_))
    
    List(postStock)