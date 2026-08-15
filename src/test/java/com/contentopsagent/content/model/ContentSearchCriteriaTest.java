package com.contentopsagent.content.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ContentSearchCriteriaTest {

    @Test
    void yearMonthOverridesDateRange() {
        ContentSearchCriteria criteria = ContentSearchCriteria.of(
                "액션",
                null,
                "SCHEDULED",
                "2026-01-01",
                "2026-12-31",
                "2026-08",
                "KR",
                null
        );

        assertThat(criteria.genre()).isEqualTo("액션");
        assertThat(criteria.status()).isEqualTo("SCHEDULED");
        assertThat(criteria.releaseDateFrom()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(criteria.releaseDateTo()).isEqualTo(LocalDate.of(2026, 8, 31));
        assertThat(criteria.serviceRegion()).isEqualTo("KR");
        assertThat(criteria.ageRating()).isNull();
        assertThat(criteria.metadataStatus()).isNull();
    }

    @Test
    void blankValuesBecomeNull() {
        ContentSearchCriteria criteria = ContentSearchCriteria.of("  ", "", "READY", null, null, null, " ", "");

        assertThat(criteria.genre()).isNull();
        assertThat(criteria.ageRating()).isNull();
        assertThat(criteria.status()).isEqualTo("READY");
        assertThat(criteria.releaseDateFrom()).isNull();
        assertThat(criteria.releaseDateTo()).isNull();
        assertThat(criteria.serviceRegion()).isNull();
        assertThat(criteria.metadataStatus()).isNull();
    }
}
