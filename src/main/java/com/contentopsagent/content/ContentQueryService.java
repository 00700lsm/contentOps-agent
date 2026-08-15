package com.contentopsagent.content;

import com.contentopsagent.content.model.ContentRecord;
import com.contentopsagent.content.model.ContentSearchCriteria;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ContentQueryService {

    private final ContentRepository contentRepository;

    public ContentQueryService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public List<ContentRecord> search(ContentSearchCriteria criteria) {
        return contentRepository.search(criteria);
    }

    public Optional<ContentRecord> findById(long id) {
        return contentRepository.findById(id);
    }
}
