package searchengine.services.bypass;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.Repositiries;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.site.SiteMap;
import searchengine.model.*;
import searchengine.model.entity.EnumStatus;
import searchengine.services.indexing.FillingLemmaAndIndexs;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;

import static java.lang.Thread.sleep;

@Service
public class BypassServiceImpl implements BypassService {

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PagesRepository pagesRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    private final SitesList sites;
    private static boolean indexingNow = false;
    private final IndexingResponse indexingResponse = new IndexingResponse();
    private Repositiries repositiries = new Repositiries();
    private ForkJoinPool pool;
    private ExecutorService executorService;

    @Autowired
    public BypassServiceImpl(SitesList sites) {
        this.sites = sites;
    }

    @Override
    public IndexingResponse startIndexing() {
        try {
            if(indexingNow){
                indexingResponse.setResult(false);
                indexingResponse.setError("Индексация уже запущена");
            } else {
                executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                indexingNow = true;
                pool = new ForkJoinPool();
                for (Site site : sites.getSites()) {
                    int siteId = deleteSiteDataAndCreateNew(site);
                    executorService.submit(() -> sitePageTraversal(site.getUrl(), siteId));
            }
                indexingResponse.setResult(true);
                indexingResponse.setError("");
            }
        } catch (RejectedExecutionException ex) {
            System.out.println(ex.getMessage());
        }
        return indexingResponse;
    }

    private int deleteSiteDataAndCreateNew(Site site){
        int siteId = siteRepository.getSiteIdByUrl(site.getUrl());
        if (siteId != 0) {
            indexRepository.deleteFromIndexsPagesOfSite(siteId);
            lemmaRepository.deleteAllLemmaOfSite(siteId);
            pagesRepository.deleteAllPagesOfSite(siteId);
            siteRepository.deleteSite(siteId);
        }
        Date currentDate = new Date();
        siteRepository.createSite(EnumStatus.INDEXING, currentDate, site.getUrl(), site.getName());
        return siteRepository.getSiteIdByUrl(site.getUrl());
    }

    private void sitePageTraversal(String url, int siteId) {
        SiteMap siteMap = new SiteMap(url, siteId, url);
        repositiries.setSiteRepository(siteRepository);
        repositiries.setPagesRepository(pagesRepository);
        repositiries.setLemmaRepository(lemmaRepository);
        repositiries.setIndexRepository(indexRepository);

        SiteMapRecursiveTask task = new SiteMapRecursiveTask(siteMap, repositiries);
        Boolean error = pool.invoke(task);

        Date currentDate = new Date();
        siteRepository.updateStatusTimeSite(currentDate, url);
        if (!error) {
            siteRepository.updateStatusSite(EnumStatus.INDEXED, url);
        } else {
            siteRepository.updateStatusSite(EnumStatus.FAILED, url);
            siteRepository.updateLastError("Ошибка выполнения индексации", url);
        }
    }

    @Override
    public IndexingResponse stopIndexing() {
        try {
            if (indexingNow) {
                indexingNow = false;
                pool.shutdownNow();
                executorService.shutdownNow();
                sleep(2000);
                siteRepository.updateStopIndexing();
                while(!pool.isShutdown()){
                    pool.shutdown();
                }
                indexingResponse.setResult(true);
                indexingResponse.setError("");
            } else {
                indexingResponse.setResult(false);
                indexingResponse.setError("Индексация не запущена");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return indexingResponse;
    }

    @Override
    public IndexingResponse indexPage(String url) {
        List<Site> siteList = sites.getSites();
        for (Site site : siteList) {
            String siteUrl = site.getUrl();
            StringBuilder sb = new StringBuilder(siteUrl);
            sb.delete(0, siteUrl.indexOf(':'));
            String siteUrlWithOutProxy = sb.toString();
            String regex = "http[s]?"+siteUrlWithOutProxy+"[^#,]*";

            if(siteList.indexOf(site) + 1 == siteList.size() && !url.matches(regex)){
                indexingResponse.setResult(false);
                indexingResponse.setError("Данная страница находится за пределами сайтов, " +
                        "указанных в конфигурационном файле");
                break;
            }

            if(url.matches(regex)){
                indexingPage(url, siteUrl, site.getName());
                indexingResponse.setResult(true);
                break;
            }
        }
        return indexingResponse;
    }

    private void indexingPage(String url, String siteUrl, String siteName){
        Document document = ParseHTML.getContent(url);
        String linkToDataBase = url.replace(siteUrl, "");
        int responseCode = document.connection().response().statusCode();
        if (!String.valueOf(responseCode).matches( "4[0-9]{2}+") &&
                !String.valueOf(responseCode).matches("5[0-9]{2}+")) {
            int siteId = siteRepository.getSiteIdByUrl(siteUrl);
            if (siteId == 0) {
                Date currentDate = new Date();
                siteRepository.createSite(EnumStatus.INDEXED, currentDate, siteUrl, siteName);
            }
            siteId = siteRepository.getSiteIdByUrl(siteUrl);
            int pageId = pagesRepository.getPageIdByPath(linkToDataBase, siteId);
            if (pageId != 0) {
                List<String> lemmaList = lemmaRepository.getAllLemma(pageId);
                indexRepository.deleteAllDataOfPage(pageId);
                pagesRepository.deletePage(linkToDataBase);
                StringBuilder insertQuery = new StringBuilder();
                for (String lemma : lemmaList) {
                    insertQuery.append((insertQuery.length() == 0 ? "" : ",") +
                            " ('" + lemma + "' , '" + siteId + "', 0)");
                }
                lemmaRepository.updateCountdownLemmaOfTheSite(insertQuery);
            }
            pagesRepository.createPage(siteId, linkToDataBase, document.connection().response().statusCode(),
                    String.valueOf(document));

            FillingLemmaAndIndexs fillingLemmaAndIndexs =
                    new FillingLemmaAndIndexs();
            repositiries.setSiteRepository(siteRepository);
            repositiries.setPagesRepository(pagesRepository);
            repositiries.setLemmaRepository(lemmaRepository);
            repositiries.setIndexRepository(indexRepository);
            fillingLemmaAndIndexs.processing(repositiries, document, linkToDataBase, siteUrl);
        }
    }

    public static boolean isIndexingNow() {
        return indexingNow;
    }

}
