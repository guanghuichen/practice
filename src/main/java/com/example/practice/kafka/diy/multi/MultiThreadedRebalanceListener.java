package com.example.practice.kafka.diy.multi;


import com.example.practice.kafka.diy.worker.ConsumerWorker;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 任何Rebalance监听器都要实现ConsumerRebalanceListener接口。
 * 该类定义了3个字段，分别保存Consumer实例、要停掉的Worker线程实例以及要提交的位移数据。
 * 主要的逻辑在onPartitionsRevoked方法中实现。第一步是停掉Worker线程；第二步是手动提交位移。
 */
public class MultiThreadedRebalanceListener implements ConsumerRebalanceListener {

  private final Consumer<String, String> consumer;
  private final Map<TopicPartition, ConsumerWorker<String, String>> outstandingWorkers;
  private final Map<TopicPartition, OffsetAndMetadata> offsets;

  public MultiThreadedRebalanceListener(Consumer<String, String> consumer, Map<TopicPartition, ConsumerWorker<String, String>> outstandingWorkers, Map<TopicPartition, OffsetAndMetadata> offsets) {
    this.consumer = consumer;
    this.outstandingWorkers = outstandingWorkers;
    this.offsets = offsets;
  }

  @Override
  public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
    Map<TopicPartition, ConsumerWorker<String, String>> stoppedWorkers = new HashMap<>();
    for (TopicPartition tp : partitions) {
      ConsumerWorker<String, String> worker = outstandingWorkers.remove(tp);
      if (worker != null) {
        worker.close();
        stoppedWorkers.put(tp, worker);
      }
    }

    stoppedWorkers.forEach((tp, worker) -> {
      long offset = worker.waitForCompletion(1, TimeUnit.SECONDS);
      if (offset > 0L) {
        offsets.put(tp, new OffsetAndMetadata(offset));
      }
    });

    Map<TopicPartition, OffsetAndMetadata> revokedOffsets = new HashMap<>();
    partitions.forEach(tp -> {
      OffsetAndMetadata offset = offsets.remove(tp);
      if (offset != null) {
        revokedOffsets.put(tp, offset);
      }
    });

    try {
      consumer.commitSync(revokedOffsets);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
    consumer.resume(partitions);
  }
}