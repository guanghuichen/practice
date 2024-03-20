package com.example.practice.kafka.diy;

import com.example.practice.kafka.diy.multi.MultiThreadedConsumer;
import com.example.practice.kafka.diy.single.OrdinaryConsumer;

public class Test {
  public static void main(String[] args) throws InterruptedException {
    int expectedCount = 50 * 900;
    String brokerId = "localhost:9092";
    String groupId = "test-group";
    String topic = "test";

    OrdinaryConsumer consumer = new OrdinaryConsumer(brokerId, topic, groupId + "-single", expectedCount);
    long start = System.currentTimeMillis();
    consumer.run();
    System.out.println("Single-threaded consumer costs " + (System.currentTimeMillis() - start));

    Thread.sleep(1L);

    MultiThreadedConsumer multiThreadedConsumer = new MultiThreadedConsumer(brokerId, topic, groupId + "-multi", expectedCount);
    start = System.currentTimeMillis();
    multiThreadedConsumer.run();
    System.out.println("Multi-threaded consumer costs " + (System.currentTimeMillis() - start));
  }
}