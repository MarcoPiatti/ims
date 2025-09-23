package ims.central.query
package server

import config.ServerConfig

import cats.effect.{IO, Resource}
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

object Server:
  def apply(config: ServerConfig, httpApp: HttpApp[IO]): Resource[IO, Server] =
    EmberServerBuilder.default[IO]
      .withPort(config.port)
      .withHost(config.host)
      .withHttpApp(httpApp)
      .build

