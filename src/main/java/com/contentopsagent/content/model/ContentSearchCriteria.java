package com.contentopsagent.content.model;

import java.time.LocalDate;
import java.time.YearMonth;

public record ContentSearchCriteria(
        String genre,
        String ageRating,
        String status,
        LocalDate releaseDateFrom,
        LocalDate releaseDateTo,
        String serviceRegion,
        String metadataStatus
) {
    public static ContentSearchCriteria of(
            String genre,
            String ageRating,
            String status,
            String releaseDateFrom,
            String releaseDateTo,
            String yearMonth,
            String serviceRegion,
            String metadataStatus
    ) {
        LocalDate from = parseDate(releaseDateFrom);
        LocalDate to = parseDate(releaseDateTo);
        YearMonth month = parseYearMonth(yearMonth);
        if (month != null) {
            from = month.atDay(1);
            to = month.atEndOfMonth();
        }
        return new ContentSearchCriteria(
                blankToNull(genre),
                blankToNull(ageRating),
                blankToNull(status),
                from,
                to,
                blankToNull(serviceRegion),
                blankToNull(metadataStatus)
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static LocalDate parseDate(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? null : LocalDate.parse(normalized);
    }

    private static YearMonth parseYearMonth(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? null : YearMonth.parse(normalized);
    }
}
