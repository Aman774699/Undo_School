package com.undoschool.search.controller;

import com.undoschool.search.controller.dto.SearchResponse;
import com.undoschool.search.document.CourseDocument;
import com.undoschool.search.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchController {

    private final CourseService courseService;

    @Autowired
    public SearchController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false, defaultValue = "upcoming") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        Page<CourseDocument> results = courseService.searchCourses(
                q, minAge, maxAge, category, type, minPrice, maxPrice, startDate, sort,
                PageRequest.of(page, size));

        return ResponseEntity.ok(SearchResponse.fromPage(results));
    }

    @GetMapping("/search/suggest")
    public ResponseEntity<List<String>> suggestCourseTitles(
            @RequestParam String q) {
        List<String> suggestions = courseService.suggestCourseTitles(q);
        return ResponseEntity.ok(suggestions);
    }
} 