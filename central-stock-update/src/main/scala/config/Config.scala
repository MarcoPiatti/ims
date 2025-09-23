package ims.central.update
package config

import com.comcast.ip4s.{Host, Port}
import pureconfig.*
export pureconfig.module.ip4s.portReader
export pureconfig.module.ip4s.hostReader
import scala.deriving.Mirror

given ConfigReader[Sensitive] = ConfigReader[String].map(Sensitive(_))

case class Sensitive(value: String) extends AnyVal {
  override def toString: String = "*****"
}

case class Config(storeId: Int,
                  db: DbConfig,
                  kafka: KafkaConfig) derives ConfigReader

case class DbConfig(host: Host,
                    port: Port,
                    schema: String,
                    user: Sensitive,
                    password: Sensitive)

case class KafkaConfig(stockUpdates: KafkaTopicConfig, heartbeat: KafkaTopicConfig)

case class KafkaTopicConfig(bootstrapServers: String,
                            topic: String,
                            groupId: String,
                            autoOffsetReset: String)

object Config:
  def load(): Config = ConfigSource.default.loadOrThrow[Config]