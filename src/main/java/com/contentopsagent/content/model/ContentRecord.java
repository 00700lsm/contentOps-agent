package com.contentopsagent.content.model;

import java.time.LocalDate;

public record ContentRecord(
        long id,
        String title,
        String genre,
        String ageRating,
        String status,
        LocalDate releaseDate,
        String serviceRegion,
        String metadataStatus
) {
}
