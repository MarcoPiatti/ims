package ims.central.update
package db

import config.DbConfig

import cats.effect.{IO, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object Db:
  def transactor(config: DbConfig): Resource[IO, HikariTransactor[IO]] =
    for
      ce <- ExecutionContexts.fixedThreadPool[IO](8)
      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = "com.mysql.cj.jdbc.Driver",
        url = s"jdbc:mysql://${config.host}:${config.port}/${config.schema}",
        user            = config.user.value,
        pass            = config.password.value,
        connectEC       = ce
      )
    yield xa