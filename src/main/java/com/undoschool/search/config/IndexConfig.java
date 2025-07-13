package com.undoschool.search.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static org.elasticsearch.client.RequestOptions.DEFAULT;

@Configuration
public class IndexConfig {

    private static final Logger logger = LoggerFactory.getLogger(IndexConfig.class);

    @Value("${app.elasticsearch.index-name}")
    private String indexName;

    private final RestHighLevelClient client;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    public IndexConfig(RestHighLevelClient client, ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.client = client;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    @PostConstruct
    public void createIndexIfNotExists() {
        try {
            boolean indexExists = client.indices().exists(new GetIndexRequest(indexName), DEFAULT);
            if (!indexExists) {
                logger.info("Creating index: {}", indexName);
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
                createIndexRequest.settings(Settings.builder()
                        .put("index.number_of_shards", 1)
                        .put("index.number_of_replicas", 0)
                        .build());

                String mapping = "{\n" +
                        "  \"properties\": {\n" +
                        "    \"id\": { \"type\": \"keyword\" },\n" +
                        "    \"title\": { \"type\": \"text\", \"analyzer\": \"standard\" },\n" +
                        "    \"description\": { \"type\": \"text\", \"analyzer\": \"standard\" },\n" +
                        "    \"category\": { \"type\": \"keyword\" },\n" +
                        "    \"type\": { \"type\": \"keyword\" },\n" +
                        "    \"gradeRange\": { \"type\": \"keyword\" },\n" +
                        "    \"minAge\": { \"type\": \"integer\" },\n" +
                        "    \"maxAge\": { \"type\": \"integer\" },\n" +
                        "    \"price\": { \"type\": \"double\" },\n" +
                        "    \"nextSessionDate\": { \"type\": \"date\" },\n" +
                        "    \"suggest\": { \"type\": \"completion\" }\n" +
                        "  }\n" +
                        "}";

                createIndexRequest.mapping(mapping, XContentType.JSON);
                client.indices().create(createIndexRequest, DEFAULT);
                logger.info("Index created successfully: {}", indexName);
            } else {
                logger.info("Index already exists: {}", indexName);
            }
        } catch (IOException e) {
            logger.error("Error creating index: {}", e.getMessage(), e);
        }
    }
} 