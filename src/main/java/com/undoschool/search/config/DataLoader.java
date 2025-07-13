package com.undoschool.search.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.undoschool.search.document.CourseDocument;
import com.undoschool.search.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final CourseService courseService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DataLoader(CourseService courseService) {
        this.courseService = courseService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void run(String... args) {
        try {
            logger.info("Loading sample courses data...");
            
            ClassPathResource resource = new ClassPathResource("sample-courses.json");
            if (!resource.exists()) {
                logger.error("sample-courses.json file not found in classpath");
                return;
            }
            
            InputStream inputStream = resource.getInputStream();
            
            List<CourseDocument> courses = objectMapper.readValue(inputStream, 
                    new TypeReference<List<CourseDocument>>() {});
            
            if (courses.isEmpty()) {
                logger.warn("No courses found in sample data");
                return;
            }
            
            logger.info("Loaded {} courses from sample data", courses.size());
            courseService.indexCourses(courses);
            logger.info("Successfully indexed all courses");
            
        } catch (IOException e) {
            logger.error("Failed to load sample courses data: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during data loading: {}", e.getMessage(), e);
        }
    }
} 