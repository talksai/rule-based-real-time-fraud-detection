package com.codewithsai.alert.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories("com.codewithsai.alert.notification.repository")
public class MongoConfig { }

