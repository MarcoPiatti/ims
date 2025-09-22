package ims.central.update
package kafka

import cats.effect.IO
import cats.implicits.toTraverseOps
import fs2.Chunk
import fs2.kafka.*
import fs2.kafka.consumer.KafkaConsumeChunk.CommitNow

trait StockUpdateChunkProcessor:
  def processChunk(chunk: Chunk[ConsumerRecord[StockUpdateKey, StockUpdateData]]): IO[CommitNow]

object StockUpdateChunkProcessor:
  def apply(processor: StockUpdateProcessor): StockUpdateChunkProcessor = 
    (chunk: Chunk[ConsumerRecord[StockUpdateKey, StockUpdateData]]) => 
      chunk.last.traverse(processor.process).as(CommitNow)