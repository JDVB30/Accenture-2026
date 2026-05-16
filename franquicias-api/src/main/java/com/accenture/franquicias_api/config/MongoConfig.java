package com.accenture.franquicias_api.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MongoConfig {

    @Bean
    @Primary
    public MongoClient reactiveMongoClient() {
        String uri = "mongodb://admin:admin123@franquicias-mongodb:27017/franquicias_db?authSource=admin";
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build();
        return MongoClients.create(settings);
    }
}
