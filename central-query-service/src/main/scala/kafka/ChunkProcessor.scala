package ims.central.query
package kafka

import cats.effect.IO
import cats.implicits.toTraverseOps
import fs2.Chunk
import fs2.kafka.*
import fs2.kafka.consumer.KafkaConsumeChunk.CommitNow

type ChunkProcessor[K,V] = Chunk[ConsumerRecord[K,V]] => IO[CommitNow]
object ChunkProcessor:
  def last[K,V](processor: ConsumerRecord[K,V] => IO[Unit]): ChunkProcessor[K,V] = 
    _.last.traverse(processor).as(CommitNow)
    
  def all[K,V](processor: ConsumerRecord[K,V] => IO[Unit]): ChunkProcessor[K,V] = 
    _.traverse(processor).as(CommitNow)