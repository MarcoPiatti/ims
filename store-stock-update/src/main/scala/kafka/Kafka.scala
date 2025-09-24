package ims.store
package kafka

import config.KafkaTopicConfig

import cats.effect.IO
import cats.effect.Resource
import fs2.kafka.*
import io.circe.{Decoder, Encoder}
import io.circe.derivation.Configuration
import io.circe.parser.decode
import io.circe.syntax.*

def circeSerializer[A: Encoder]: Serializer[IO, A] =
  Serializer.string[IO].contramap(a => a.asJson.noSpaces)

def circeDeserializer[A: Decoder]: Deserializer[IO, A] =
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  Deserializer.string[IO].map(decode[A]).flatMap {
    case Right(data) => Deserializer.const(data)
    case Left(err)   => Deserializer.fail(err)
  }

trait KafkaSender[K: Encoder, V: Encoder]:
  def send(key: K, value: V): IO[Unit]

object KafkaSender:
  def apply[K: Encoder, V: Encoder](producer: KafkaProducer[IO, K, V], topic: String): KafkaSender[K, V] =
    new KafkaSender[K, V]:
      def send(key: K, value: V): IO[Unit] =
        val record = ProducerRecord(topic, key, value)
        producer.produce(ProducerRecords.one(record)).flatten.void


object Kafka:
  def chunkConsumer[K: Decoder, V: Decoder](config: KafkaTopicConfig,
                                            processor: ChunkProcessor[K,V]): IO[Nothing] =
    val consumerSettings: ConsumerSettings[IO, K, V] = 
      ConsumerSettings(circeDeserializer[K], circeDeserializer[V])
      .withBootstrapServers(config.bootstrapServers)
      .withGroupId(config.groupId)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
    
    KafkaConsumer.stream(consumerSettings)
      .subscribeTo(config.topic)
      .consumeChunk(processor)
  
  def sender[K: Encoder, V: Encoder](config: KafkaTopicConfig): Resource[IO, KafkaSender[K, V]] =
    val settings: ProducerSettings[IO, K, V] =
      ProducerSettings(circeSerializer[K], circeSerializer[V])
        .withBootstrapServers(config.bootstrapServers)
    KafkaProducer.resource(settings).map(KafkaSender(_, config.topic))

