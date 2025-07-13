package com.undoschool.search.service;

import com.undoschool.search.document.CourseDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface CourseService {
    
    void indexCourses(List<CourseDocument> courses);
    
    Page<CourseDocument> searchCourses(
            String query,
            Integer minAge,
            Integer maxAge,
            String category,
            String type,
            Double minPrice,
            Double maxPrice,
            Instant startDate,
            String sort,
            Pageable pageable
    );
    
    List<String> suggestCourseTitles(String query);
} 