package com.contentopsagent.document.loader;

import com.contentopsagent.document.model.ParsedDocument;
import com.contentopsagent.document.model.ParsedSection;
import com.contentopsagent.document.model.SourceDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class MarkdownDocumentParser {

    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);

    public ParsedDocument parse(SourceDocument document) {
        String content = document.content() == null ? "" : document.content();
        Matcher matcher = HEADING.matcher(content);
        List<Heading> headings = new ArrayList<>();
        while (matcher.find()) {
            headings.add(new Heading(matcher.start(), matcher.end(), matcher.group(2).trim()));
        }
        if (headings.isEmpty()) {
            return new ParsedDocument(
                    document.documentId(),
                    document.documentName(),
                    List.of(new ParsedSection("본문", content.trim()))
            );
        }

        List<ParsedSection> sections = new ArrayList<>();
        if (headings.getFirst().start() > 0) {
            String preamble = content.substring(0, headings.getFirst().start()).trim();
            if (!preamble.isBlank()) {
                sections.add(new ParsedSection("서문", preamble));
            }
        }
        for (int i = 0; i < headings.size(); i++) {
            Heading heading = headings.get(i);
            int bodyStart = heading.end();
            int bodyEnd = i + 1 < headings.size() ? headings.get(i + 1).start() : content.length();
            String body = content.substring(bodyStart, bodyEnd).trim();
            String sectionContent = heading.title() + "\n" + body;
            sections.add(new ParsedSection(heading.title(), sectionContent.trim()));
        }
        return new ParsedDocument(document.documentId(), document.documentName(), sections);
    }

    private record Heading(int start, int end, String title) {
    }
}
