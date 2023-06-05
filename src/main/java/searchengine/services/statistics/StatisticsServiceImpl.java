package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.exceptions.SiteNotFoundException;
import searchengine.model.LemmaRepository;
import searchengine.model.PagesRepository;
import searchengine.model.SiteRepository;
import searchengine.services.bypass.BypassServiceImpl;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PagesRepository pagesRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    private final SitesList sites;

    @Override
    public StatisticsResponse getStatistics() {
        StatisticsResponse response = new StatisticsResponse();
        TotalStatistics total = new TotalStatistics();
        total.setSites(siteRepository.getCountSite());
        total.setIndexing(BypassServiceImpl.isIndexingNow());

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (Site site : sitesList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            String siteUrl = site.getUrl();
            int siteId = siteRepository.getSiteIdByUrl(siteUrl);
            if (siteId != 0) {
                int pages = pagesRepository.getCountPagesOfSite(siteUrl);
                int lemmas = lemmaRepository.getLemmaCountOfSite(siteUrl);
                searchengine.model.entity.Site siteStatusError =
                        siteRepository.getSiteStatusError(siteId).orElseThrow(() -> new SiteNotFoundException(siteId));
                item.setPages(pages);
                item.setLemmas(lemmas);
                String status = String.valueOf(siteStatusError.getStatus());
                item.setStatus(status);
                if (status.equals("FAILED")) {
                    item.setError(siteStatusError.getLastError());
                }
                item.setStatusTime(siteRepository.getStatusTime(siteId).getTime());
                total.setPages(total.getPages() + pages);
                total.setLemmas(total.getLemmas() + lemmas);
                detailed.add(item);
            }
        }
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
