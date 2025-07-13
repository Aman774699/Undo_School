package com.undoschool.search.controller.dto;

import com.undoschool.search.document.CourseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private long total;
    private List<CourseDto> courses;
    
    public static SearchResponse fromPage(Page<CourseDocument> page) {
        List<CourseDto> courses = page.getContent().stream()
                .map(CourseDto::fromDocument)
                .collect(Collectors.toList());
        
        return SearchResponse.builder()
                .total(page.getTotalElements())
                .courses(courses)
                .build();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseDto {
        private String id;
        private String title;
        private String category;
        private String type;
        private Double price;
        private Instant nextSessionDate;
        
        public static CourseDto fromDocument(CourseDocument document) {
            return CourseDto.builder()
                    .id(document.getId())
                    .title(document.getTitle())
                    .category(document.getCategory())
                    .type(document.getType())
                    .price(document.getPrice())
                    .nextSessionDate(document.getNextSessionDate())
                    .build();
        }
    }
} 