package ims.central.query
package config

import com.comcast.ip4s.{Host, Port}
import pureconfig.*

import scala.concurrent.duration.FiniteDuration
export pureconfig.module.ip4s.portReader
export pureconfig.module.ip4s.hostReader
import scala.deriving.Mirror

given ConfigReader[Sensitive] = ConfigReader[String].map(Sensitive(_))

case class Sensitive(value: String) extends AnyVal {
  override def toString: String = "*****"
}

case class Config(db: DbConfig,
                  server: ServerConfig,
                  store: StoreConfig) derives ConfigReader

case class DbConfig(host: Host,
                    port: Port,
                    schema: String,
                    user: Sensitive,
                    password: Sensitive)

case class ServerConfig(port: Port, host: Host)

case class StoreConfig(syncLimit: FiniteDuration)

object Config:
  def load(): Config = ConfigSource.default.loadOrThrow[Config]