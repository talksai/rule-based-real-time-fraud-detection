package com.codewithsai.alert.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class AlertNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlertNotificationApplication.class, args);
	}

}
