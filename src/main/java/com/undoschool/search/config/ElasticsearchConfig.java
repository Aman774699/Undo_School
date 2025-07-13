package com.undoschool.search.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.undoschool.search.repository")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${spring.elasticsearch.rest.uris}")
    private String elasticsearchUri;

    @Value("${spring.elasticsearch.rest.connection-timeout:1s}")
    private String connectionTimeout;

    @Value("${spring.elasticsearch.rest.read-timeout:30s}")
    private String readTimeout;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(elasticsearchUri.replace("http://", ""))
                .withConnectTimeout(Duration.parse("PT" + connectionTimeout.replace("s", "S")))
                .withSocketTimeout(Duration.parse("PT" + readTimeout.replace("s", "S")))
                .build();
        return RestClients.create(clientConfiguration).rest();
    }
} 