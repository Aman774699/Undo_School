package com.undoschool.search;

import com.undoschool.search.document.CourseDocument;
import com.undoschool.search.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class CourseSearchApplicationTests {

    @Container
    private static final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(
            "docker.elastic.co/elasticsearch/elasticsearch:7.17.10"
    ).withExposedPorts(9200);

    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.rest.uris", 
                () -> "http://" + elasticsearchContainer.getHost() + ":" + elasticsearchContainer.getMappedPort(9200));
    }

    @Autowired
    private CourseService courseService;
    
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    
    @BeforeEach
    void setUp() {
        // Clear the index before each test
        if (elasticsearchOperations.indexOps(CourseDocument.class).exists()) {
            elasticsearchOperations.indexOps(CourseDocument.class).delete();
            elasticsearchOperations.indexOps(CourseDocument.class).create();
        }
        
        // Create test data
        CourseDocument course1 = CourseDocument.builder()
                .id("TEST001")
                .title("Test Math Course")
                .description("A test math course for integration testing")
                .category("Math")
                .type("COURSE")
                .gradeRange("1st-3rd")
                .minAge(6)
                .maxAge(9)
                .price(149.99)
                .nextSessionDate(Instant.parse("2025-01-15T14:00:00Z"))
                .build();

        CourseDocument course2 = CourseDocument.builder()
                .id("TEST002")
                .title("Test Science Course")
                .description("A test science course for integration testing")
                .category("Science")
                .type("COURSE")
                .gradeRange("2nd-4th")
                .minAge(7)
                .maxAge(10)
                .price(179.99)
                .nextSessionDate(Instant.parse("2025-01-20T15:30:00Z"))
                .build();

        courseService.indexCourses(Arrays.asList(course1, course2));
        
        // Wait for indexing to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testSearchCourses() {
        // When - search by keyword
        Page<CourseDocument> mathResults = courseService.searchCourses(
                "math", null, null, null, null, null, null, null, null,
                PageRequest.of(0, 10));

        // Then
        assertEquals(1, mathResults.getTotalElements());
        assertEquals("Test Math Course", mathResults.getContent().get(0).getTitle());

        // When - search by category
        Page<CourseDocument> scienceResults = courseService.searchCourses(
                null, null, null, "Science", null, null, null, null, null,
                PageRequest.of(0, 10));

        // Then
        assertEquals(1, scienceResults.getTotalElements());
        assertEquals("Test Science Course", scienceResults.getContent().get(0).getTitle());

        // When - search by age range
        Page<CourseDocument> ageResults = courseService.searchCourses(
                null, 7, null, null, null, null, null, null, null,
                PageRequest.of(0, 10));

        // Then
        assertEquals(2, ageResults.getTotalElements());
    }
    
    @Test
    void testSuggestCourseTitles() {
        // When
        var suggestions = courseService.suggestCourseTitles("mat");
        
        // Then
        assertTrue(suggestions.contains("Test Math Course"));
    }
} 