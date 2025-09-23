package ims.central.update
package kafka

import config.{KafkaConfig, KafkaTopicConfig}

import cats.effect.IO
import doobie.util.transactor.Transactor
import fs2.Chunk
import fs2.kafka.*
import fs2.kafka.consumer.KafkaConsumeChunk.CommitNow
import io.circe.Decoder
import io.circe.derivation.Configuration
import io.circe.parser.decode
import org.apache.kafka.common.config.TopicConfig

def circeDeserializer[A: Decoder]: Deserializer[IO, A] =
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  Deserializer.string[IO].map(decode[A]).flatMap {
    case Right(data) => Deserializer.const(data)
    case Left(err)   => Deserializer.fail(err)
  }

object Kafka:
  def chunkConsumer[K: Decoder, V: Decoder](config: KafkaTopicConfig,
                                            processor: ChunkProcessor[K,V]): IO[Nothing] =
    val consumerSettings: ConsumerSettings[IO, K, V] = 
      ConsumerSettings(circeDeserializer[K], circeDeserializer[V])
      .withBootstrapServers(config.bootstrapServers)
      .withGroupId(config.groupId)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
    
    KafkaConsumer.stream(consumerSettings)
      .subscribeTo(config.stockUpdateTopic, config.heartbeatTopic)
      .consumeChunk(processor)