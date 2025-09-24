package ims.store
package kafka.reservations

import config.KafkaTopicConfig
import domain.StockUpdate
import kafka.{circeDeserializer, circeSerializer}
import service.StockUpdater

import cats.data.NonEmptySetImpl
import cats.effect.{IO, Resource}
import cats.syntax.all.*
import fs2.Stream
import fs2.kafka.*
import fs2.kafka.instances.fs2KafkaTopicPartitionOrder
import io.circe.generic.auto.*
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.utils.Utils
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*

import java.nio.charset.StandardCharsets

object ReservationStreaming:
  given Logger[IO] = Slf4jLogger.getLogger

  private def consumerSettings(cfg: KafkaTopicConfig): ConsumerSettings[IO, ReservationKey, ReservationData] =
    ConsumerSettings(circeDeserializer[ReservationKey], circeDeserializer[ReservationData])
      .withBootstrapServers(cfg.bootstrapServers)
      .withGroupId(cfg.groupId)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)

  private def producerSettings(cfg: KafkaTopicConfig): ProducerSettings[IO, ReservationResultKey, ReservationResultData] =
    ProducerSettings(circeSerializer[ReservationResultKey], circeSerializer[ReservationResultData])
      .withBootstrapServers(cfg.bootstrapServers)

  private def partitionForStore(storeId: Int, partitions: Int): Int =
    // We mirror Kafka's default partitioner: murmur2(serializedKey) & 0x7fffffff % partitions
    val keyJson = s"{\"store_id\":$storeId}" // matches simple circe encoding for ReservationKey
    val hash = Utils.murmur2(keyJson.getBytes(StandardCharsets.UTF_8)) & 0x7fffffff
    hash % partitions

  private def fetchPartitionCount(cfg: KafkaTopicConfig): IO[Int] =
    val adminSettings = AdminClientSettings(cfg.bootstrapServers)
    KafkaAdminClient.resource(adminSettings).use { admin =>
      admin.describeTopics(List(cfg.topic)).flatMap { metaMap =>
        metaMap.get(cfg.topic) match
          case Some(desc) => IO.pure(desc.partitions.size)
          case None        => IO.raiseError(new RuntimeException(s"Topic ${cfg.topic} metadata not found"))
      }
    }

  // Pipe logic implemented inline inside stream composition
  def stream(storeId: Int,
             consumerCfg: KafkaTopicConfig,
             producerCfg: KafkaTopicConfig,
             stockUpdater: StockUpdater): Stream[IO, Unit] =
    val inTopic  = consumerCfg.topic
    val outTopic = producerCfg.topic

    val consSettings = consumerSettings(consumerCfg)
    val prodSettings = producerSettings(producerCfg)

    for
      partitionCount <- Stream.eval(fetchPartitionCount(consumerCfg))
      targetPartition = partitionForStore(storeId, partitionCount)
      _ <- Stream.eval(info"Store $storeId will consume partition $targetPartition of $inTopic (partitions=$partitionCount)")
      tp = TopicPartition(inTopic, targetPartition)
      _ <- KafkaConsumer
        .stream(consSettings)
        .evalTap(_.assign(NonEmptySetImpl.one(tp)))
        .records
        .evalMapChunk { committable =>
          val value = committable.record.value
          val update = StockUpdate(value.sku, value.quantity)
          stockUpdater(update).map { result =>
            val data = ReservationResultData(value.id, result.fold(_ => ReservationStatus.CANCELLED, _ => ReservationStatus.CONFIRMED))
            val record = ProducerRecord(outTopic, ReservationResultKey(data.id), data)
            ProducerRecords.one(record)
          }
        }
        .through(KafkaProducer.pipe(prodSettings))
    yield ()

  def start(storeId: Int,
            consumerCfg: KafkaTopicConfig,
            producerCfg: KafkaTopicConfig,
            stockUpdater: StockUpdater): Resource[IO, Unit] =
    stream(storeId, consumerCfg, producerCfg, stockUpdater).compile.drain.background.void
