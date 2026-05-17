package com.volleyball.volleyballcommunitybackend.config;

import lombok.Data;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Data
@Configuration
@ConfigurationProperties(prefix = "neo4j")
public class Neo4jConfig {
    private String uri;
    private String username;
    private String password;

    @Bean
    public Driver neo4jDriver() {
        Config config = Config.builder()
                .withConnectionTimeout(60, TimeUnit.SECONDS)
                .withMaxConnectionLifetime(2, TimeUnit.HOURS)
                .withMaxConnectionPoolSize(50)
                .withConnectionAcquisitionTimeout(2, TimeUnit.MINUTES)
                .build();
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password), config);
    }
}
