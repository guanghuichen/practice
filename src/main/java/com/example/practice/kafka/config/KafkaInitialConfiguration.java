package com.example.practice.kafka.config;


import com.example.practice.kafka.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaInitialConfiguration {

  // 创建一个名为testt opic的Topic并设置分区数为3，分区副本数为2
  @Bean
  public NewTopic initialTopic() {

    return new NewTopic(Topics.TEST_TOPIC, 3, (short) 2);
  }

  // 如果要修改分区数，只需修改配置值重启项目即可
  // 修改分区数并不会导致数据的丢失，但是分区数只能增大不能减小
  @Bean
  public NewTopic updateTopic() {
    return new NewTopic("testtopic", 10, (short) 2);
  }

}
