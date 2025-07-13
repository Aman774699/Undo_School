package com.undoschool.search.service;

import com.undoschool.search.document.CourseDocument;
import com.undoschool.search.repository.CourseRepository;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final String indexName = "courses";

    @Autowired
    public CourseServiceImpl(CourseRepository courseRepository, ElasticsearchOperations elasticsearchOperations) {
        this.courseRepository = courseRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public void indexCourses(List<CourseDocument> courses) {
        // Set suggest field for each course
        courses.forEach(CourseDocument::setSuggest);
        courseRepository.saveAll(courses);
    }

    @Override
    public Page<CourseDocument> searchCourses(
            String query,
            Integer minAge,
            Integer maxAge,
            String category,
            String type,
            Double minPrice,
            Double maxPrice,
            Instant startDate,
            String sort,
            Pageable pageable) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // Full-text search on title and description with fuzzy matching
        if (query != null && !query.isEmpty()) {
            boolQueryBuilder.must(
                    QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchQuery("title", query).fuzziness(Fuzziness.AUTO).boost(2.0f))
                        .should(QueryBuilders.matchQuery("description", query).fuzziness(Fuzziness.AUTO))
                        .minimumShouldMatch(1)
            );
        }

        // Age range filter
        if (minAge != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("maxAge").gte(minAge));
        }
        if (maxAge != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("minAge").lte(maxAge));
        }

        // Category filter
        if (category != null && !category.isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category", category));
        }

        // Type filter
        if (type != null && !type.isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("type", type));
        }

        // Price range filter
        if (minPrice != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(minPrice));
        }
        if (maxPrice != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(maxPrice));
        }

        // Date filter
        if (startDate != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("nextSessionDate").gte(startDate));
        }

        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(pageable);

        // Sorting
        if (sort != null) {
            switch (sort) {
                case "priceAsc":
                    searchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
                    break;
                case "priceDesc":
                    searchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
                    break;
                case "upcoming":
                default:
                    searchQueryBuilder.withSort(SortBuilders.fieldSort("nextSessionDate").order(SortOrder.ASC));
                    break;
            }
        } else {
            // Default sort by upcoming sessions
            searchQueryBuilder.withSort(SortBuilders.fieldSort("nextSessionDate").order(SortOrder.ASC));
        }

        NativeSearchQuery searchQuery = searchQueryBuilder.build();
        SearchHits<CourseDocument> searchHits = elasticsearchOperations.search(
                searchQuery, CourseDocument.class, IndexCoordinates.of(indexName));

        List<CourseDocument> courses = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        long total = searchHits.getTotalHits();

        return new PageImpl<>(courses, pageable, total);
    }

    @Override
    public List<String> suggestCourseTitles(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            // For simplicity, we'll use a prefix query on the title field instead of completion suggester
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.prefixQuery("title", query.toLowerCase()));
    
            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(boolQueryBuilder)
                    .withMaxResults(10)
                    .build();
    
            SearchHits<CourseDocument> searchHits = elasticsearchOperations.search(
                    searchQuery, CourseDocument.class, IndexCoordinates.of(indexName));
    
            return searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent().getTitle())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Log the error and return empty list
            return Collections.emptyList();
        }
    }
} 