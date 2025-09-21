package ims.central.update
package kafka

import config.KafkaConfig

import cats.effect.{IO, Resource}
import doobie.implicits.*
import doobie.util.transactor.Transactor
import fs2.kafka.*
import fs2.kafka.consumer.KafkaConsumeChunk.CommitNow
import fs2.{Chunk, Stream}

trait StockUpdateChunkProcessor:
  def processChunk(chunk: Chunk[ConsumerRecord[StockUpdateKey, StockUpdateData]]): IO[CommitNow]

object StockUpdateChunkProcessor:
  def apply(processor: StockUpdateProcessor): StockUpdateChunkProcessor = 
    (chunk: Chunk[ConsumerRecord[StockUpdateKey, StockUpdateData]]) => 
      chunk.traverse(processor.process).as(CommitNow)