package ims.central.update
package kafka

import config.KafkaConfig

import cats.effect.{IO, Resource}
import doobie.implicits.*
import doobie.util.transactor.Transactor
import fs2.kafka.*
import fs2.kafka.consumer.KafkaConsumeChunk.CommitNow
import fs2.{Chunk, Stream}
import io.circe.Decoder
import io.circe.parser.decode

def circeDeserializer[A: Decoder]: Deserializer[IO, A] =
  Deserializer.string[IO].map(decode[A]).flatMap {
    case Right(data) => Deserializer.const(data)
    case Left(err)   => Deserializer.fail(s"Failed to decode event: ${err.getMessage}")
  }

object StockUpdatesConsumer:
  def apply(kafkaConfig: KafkaConfig, 
            transactor: Transactor[IO],
            chunkProcessor: StockUpdateChunkProcessor
            ): IO[Nothing] =
    val consumerSettings = ConsumerSettings(circeDeserializer[StockUpdateKey], circeDeserializer[StockUpdateData])
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withGroupId(kafkaConfig.groupId)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
    KafkaConsumer.stream(consumerSettings)
      .subscribeTo(kafkaConfig.topic)
      .consumeChunk(chunkProcessor)