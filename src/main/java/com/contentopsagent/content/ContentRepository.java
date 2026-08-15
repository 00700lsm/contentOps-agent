package com.contentopsagent.content;

import com.contentopsagent.content.model.ContentRecord;
import com.contentopsagent.content.model.ContentSearchCriteria;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ContentRepository {

    private static final RowMapper<ContentRecord> ROW_MAPPER = ContentRepository::mapRow;

    private final JdbcTemplate jdbcTemplate;

    public ContentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void ensureSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS contents (
                    id BIGINT PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    genre VARCHAR(100) NOT NULL,
                    age_rating VARCHAR(100) NOT NULL,
                    status VARCHAR(100) NOT NULL,
                    release_date DATE NOT NULL,
                    service_region VARCHAR(50) NOT NULL,
                    metadata_status VARCHAR(100) NOT NULL
                )
                """);
    }

    public void replaceAll(List<ContentRecord> records) {
        jdbcTemplate.update("DELETE FROM contents");
        for (ContentRecord record : records) {
            jdbcTemplate.update(
                    """
                    INSERT INTO contents
                    (id, title, genre, age_rating, status, release_date, service_region, metadata_status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    record.id(),
                    record.title(),
                    record.genre(),
                    record.ageRating(),
                    record.status(),
                    Date.valueOf(record.releaseDate()),
                    record.serviceRegion(),
                    record.metadataStatus()
            );
        }
    }

    public List<ContentRecord> search(ContentSearchCriteria criteria) {
        return jdbcTemplate.query(
                """
                SELECT id, title, genre, age_rating, status, release_date, service_region, metadata_status
                FROM contents
                WHERE (?::varchar IS NULL OR genre = ?)
                  AND (?::varchar IS NULL OR age_rating = ?)
                  AND (?::varchar IS NULL OR status = ?)
                  AND (?::date IS NULL OR release_date >= ?)
                  AND (?::date IS NULL OR release_date <= ?)
                  AND (?::varchar IS NULL OR service_region = ?)
                  AND (?::varchar IS NULL OR metadata_status = ?)
                ORDER BY id
                """,
                ROW_MAPPER,
                criteria.genre(), criteria.genre(),
                criteria.ageRating(), criteria.ageRating(),
                criteria.status(), criteria.status(),
                criteria.releaseDateFrom() == null ? null : Date.valueOf(criteria.releaseDateFrom()),
                criteria.releaseDateFrom() == null ? null : Date.valueOf(criteria.releaseDateFrom()),
                criteria.releaseDateTo() == null ? null : Date.valueOf(criteria.releaseDateTo()),
                criteria.releaseDateTo() == null ? null : Date.valueOf(criteria.releaseDateTo()),
                criteria.serviceRegion(), criteria.serviceRegion(),
                criteria.metadataStatus(), criteria.metadataStatus()
        );
    }

    public Optional<ContentRecord> findById(long id) {
        List<ContentRecord> found = jdbcTemplate.query(
                """
                SELECT id, title, genre, age_rating, status, release_date, service_region, metadata_status
                FROM contents
                WHERE id = ?
                """,
                ROW_MAPPER,
                id
        );
        return found.stream().findFirst();
    }

    private static ContentRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        Date releaseDate = rs.getDate("release_date");
        return new ContentRecord(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("genre"),
                rs.getString("age_rating"),
                rs.getString("status"),
                releaseDate.toLocalDate(),
                rs.getString("service_region"),
                rs.getString("metadata_status")
        );
    }
}
