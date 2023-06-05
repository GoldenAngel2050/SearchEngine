package searchengine.services.bypass;

import searchengine.dto.indexing.IndexingResponse;

public interface BypassService {
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
    IndexingResponse indexPage(String url);
}
