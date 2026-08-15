package com.contentopsagent.document.loader;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentopsagent.document.model.ParsedDocument;
import com.contentopsagent.document.model.SourceDocument;
import org.junit.jupiter.api.Test;

class MarkdownDocumentParserTest {

    private final MarkdownDocumentParser parser = new MarkdownDocumentParser();

    @Test
    void extractsHeadingSections() {
        SourceDocument document = new SourceDocument(
                "age-rating-policy",
                "age-rating-policy.md",
                """
                        # 연령 등급 정책

                        소개 문장

                        ## 3. 15세 콘텐츠 공개 조건

                        검수가 필요하다.

                        ### 3.2 공개 조건

                        검수 완료 이후 공개한다.
                        """
        );

        ParsedDocument parsed = parser.parse(document);

        assertThat(parsed.sections()).extracting(section -> section.title())
                .contains("연령 등급 정책", "3. 15세 콘텐츠 공개 조건", "3.2 공개 조건");
        assertThat(parsed.sections().getLast().content()).contains("검수 완료 이후 공개한다.");
    }
}
