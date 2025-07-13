# Course Search Application

This project implements a course search application using Spring Boot and Elasticsearch.

## Prerequisites

- Docker and Docker Compose
- Java 17 or higher
- Maven

## Elasticsearch Setup

To start the Elasticsearch server:

```bash
docker-compose up -d
```

Verify that Elasticsearch is running:

```bash
curl http://localhost:9200
```

You should see a JSON response with Elasticsearch version information.

## Building and Running the Application

1. Build the application:

```bash
./mvnw clean install -DskipTests
```

2. Run the application:

```bash
./mvnw spring-boot:run
```

The application will:
- Connect to Elasticsearch at localhost:9200
- Create the courses index if it doesn't exist
- Load sample course data from `src/main/resources/sample-courses.json`
- Expose REST endpoints for searching courses

## API Endpoints

### Search Courses

```
GET /api/search
```

Query parameters:
- `q`: Search keyword for title and description
- `minAge`, `maxAge`: Age range filters
- `category`: Filter by category
- `type`: Filter by type (ONE_TIME, COURSE, CLUB)
- `minPrice`, `maxPrice`: Price range filters
- `startDate`: Filter courses on or after this date (ISO-8601 format)
- `sort`: Sorting option (upcoming, priceAsc, priceDesc)
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)

Example requests:

1. Basic search for "math" courses:
```bash
curl "http://localhost:8081/api/search?q=math"
```

2. Search for science courses for ages 8-12:
```bash
curl "http://localhost:8081/api/search?category=Science&minAge=8&maxAge=12"
```

3. Search for upcoming courses sorted by price (low to high):
```bash
curl "http://localhost:8081/api/search?startDate=2025-01-01T00:00:00Z&sort=priceAsc"
```

4. Search for art clubs with price under $50:
```bash
curl "http://localhost:8081/api/search?category=Art&type=CLUB&maxPrice=50"
```

### Autocomplete Suggestions

```
GET /api/search/suggest
```

Query parameters:
- `q`: Partial title to get suggestions for

Example:
```bash
curl "http://localhost:8081/api/search/suggest?q=math"
```

This will return course titles that start with "math".

### Fuzzy Search

The main search endpoint supports fuzzy matching on the title field. For example:
```bash
curl "http://localhost:8081/api/search?q=dinors"
```

This will match courses with "Dinosaurs" in the title, despite the typo.

## Data Ingestion

The application automatically loads sample data from `src/main/resources/sample-courses.json` on startup.

To verify data ingestion:
```bash
curl "http://localhost:9200/courses/_count"
```

You should see a count of at least 50 documents.

## Running Tests

To run the integration tests:

```bash
./mvnw test
```

The tests use Testcontainers to spin up an Elasticsearch instance automatically.

## Troubleshooting

If you encounter any issues:

1. Make sure Elasticsearch is running:
```bash
docker ps
```

2. Check the application logs for any errors:
```bash
./mvnw spring-boot:run
```

3. Verify the index exists:
```bash
curl "http://localhost:9200/_cat/indices"
```

4. If the index needs to be recreated:
```bash
curl -X DELETE "http://localhost:9200/courses"
```
Then restart the application.

## Submission Instructions

To prepare your project for submission:

1. Make sure all features are working:
   - Basic search functionality
   - Filtering by all criteria
   - Sorting options
   - Autocomplete suggestions
   - Fuzzy search

2. Verify your code meets the requirements:
   - Clean code organization (controllers, services, repositories)
   - Proper error handling
   - Efficient queries using filters
   - Clear documentation

3. Create a ZIP file of your project:
   ```bash
   git archive --format=zip HEAD -o course-search-submission.zip
   ```
   Or manually zip the project folder (excluding target/ and other build directories).

4. Submit the ZIP file according to the submission guidelines. 