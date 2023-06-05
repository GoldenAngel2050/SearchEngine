package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.bypass.BypassService;
import searchengine.services.search.SearchService;
import searchengine.services.statistics.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final BypassService bypassService;
    private final SearchService searchService;

    @Autowired
    public ApiController(StatisticsService statisticsService, BypassService bypassService,
                         SearchService searchService) {
        this.statisticsService = statisticsService;
        this.bypassService = bypassService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        return ResponseEntity.ok(bypassService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {

        return ResponseEntity.ok(bypassService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(String url){
        return ResponseEntity.ok(bypassService.indexPage(url));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(String query, int offset, int limit, String site) {
        return ResponseEntity.ok(searchService.search(query, offset, limit, site));
    }
}
