package com.example.practice.controller.kafka;

import com.example.practice.kafka.Topics;
import com.example.practice.kafka.producer.KafkaProducer;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class KafkaController {

  @Autowired
  private KafkaProducer kafkaProducer;

  @GetMapping("/send/{message}")
//  @GetMapping("/send/{message}/partition/{index}")
  public boolean hello(@PathVariable("message") String message
//      , @PathVariable("index") int index
  ) {
    ExecutorService executorService = Executors.newFixedThreadPool(50);

    for (int i = 0; i < 300; i++) {
      int threadId = i;
      executorService.execute(() -> {
        for (int j = 0; j < 10; j++) {
          String data = message + j + threadId;
          String key = "1";
          if (j % 2 == 0) {
            key = "2";
          }
          kafkaProducer.send(Topics.TEST_TOPIC, key, data);
        }
      });
    }

    executorService.shutdown();
    return true;
  }

}