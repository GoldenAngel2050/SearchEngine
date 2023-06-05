package searchengine.services.search;

import searchengine.dto.search.SearchResponse;

public interface SearchService {
    SearchResponse search(String query, int offset, int limit, String site);
}
