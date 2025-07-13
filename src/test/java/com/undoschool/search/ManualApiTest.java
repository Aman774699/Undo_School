package com.undoschool.search;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ManualApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testSearchEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/search", String.class);
        
        System.out.println("Response: " + response.getBody());
        assertNotNull(response.getBody());
    }

    @Test
    public void testSearchByCategory() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/search?category=Math", String.class);
        
        System.out.println("Math Category Response: " + response.getBody());
        assertNotNull(response.getBody());
    }

    @Test
    public void testFuzzySearch() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/search?q=dinosa", String.class);
        
        System.out.println("Fuzzy Search Response: " + response.getBody());
        assertNotNull(response.getBody());
    }
} 