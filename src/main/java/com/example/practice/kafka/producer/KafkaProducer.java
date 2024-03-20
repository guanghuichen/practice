package com.example.practice.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;

  public boolean send(String topic, String key, String message) {
    try {
//      kafkaTemplate.send(topic, index, "key-", message);
      kafkaTemplate.send(topic, key, message);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

}
