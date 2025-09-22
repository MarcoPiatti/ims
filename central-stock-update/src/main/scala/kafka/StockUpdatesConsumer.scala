package ims.central.update
package kafka

import config.KafkaConfig

import cats.effect.IO
import doobie.util.transactor.Transactor
import fs2.kafka.*
import io.circe.Decoder
import io.circe.derivation.Configuration
import io.circe.generic.auto.*
import io.circe.parser.decode

def circeDeserializer[A: Decoder]: Deserializer[IO, A] =
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  Deserializer.string[IO].map(decode[A]).flatMap {
    case Right(data) => Deserializer.const(data)
    case Left(err)   => Deserializer.fail(err)
  }

object StockUpdatesConsumer:
  def apply(kafkaConfig: KafkaConfig, 
            transactor: Transactor[IO],
            chunkProcessor: StockUpdateChunkProcessor
            ): IO[Nothing] =
    val consumerSettings: ConsumerSettings[IO, StockUpdateKey, StockUpdateData] = 
      ConsumerSettings(circeDeserializer[StockUpdateKey], circeDeserializer[StockUpdateData])
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withGroupId(kafkaConfig.groupId)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
    
    KafkaConsumer.stream(consumerSettings)
      .subscribeTo(kafkaConfig.topic)
      .consumeChunk(chunkProcessor.processChunk)