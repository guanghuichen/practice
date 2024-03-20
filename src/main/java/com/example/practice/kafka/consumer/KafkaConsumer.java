package com.example.practice.kafka.consumer;

import com.example.practice.kafka.Topics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaConsumer {

  @KafkaListener(groupId = "test-001",topics = {Topics.TEST_TOPIC})
  public void onMessage1(ConsumerRecord<?, ?> record) {
    System.out.println("简单消费Topic："+record.topic()+"**分区"+record.partition()+"**值内容"+record.value());

//    log.info("简单消费Topic：{} **分区 {} **值内容 {}", record.topic(), record.partition(), record.value());
  }

}
