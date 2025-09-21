package ims.store
package server

import cats.effect.IO
import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*
import org.http4s.{HttpApp, HttpRoutes}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Routes:
  private case class HealthStatus(status: String)

  def all(endpoints: List[ServerEndpoint[Any,IO]]): HttpApp[IO] =
    val swagger = SwaggerInterpreter().fromServerEndpoints(endpoints, "Store Stock Update Service", "1.0")
    val swaggerRoute = Http4sServerInterpreter[IO]().toRoutes(swagger)
    val apiRoutes = Http4sServerInterpreter[IO]().toRoutes(endpoints)

    val healthCheck = HttpRoutes.of[IO]:
      case GET -> Root / "health-check" => Ok(HealthStatus("Active").asJson)

    (healthCheck <+> apiRoutes <+> swaggerRoute).orNotFound