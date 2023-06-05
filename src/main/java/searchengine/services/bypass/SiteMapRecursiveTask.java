package searchengine.services.bypass;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.dto.Repositiries;
import searchengine.dto.site.SiteMap;
import searchengine.model.PagesRepository;
import searchengine.model.SiteRepository;
import searchengine.services.indexing.FillingLemmaAndIndexs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;

import static java.lang.Thread.sleep;

public class SiteMapRecursiveTask extends RecursiveTask<Boolean> {
    private final SiteMap siteMap;
    private final Repositiries repositiries;

    public SiteMapRecursiveTask(SiteMap siteMap, Repositiries repositiries){
        this.siteMap = siteMap;
        this.repositiries = repositiries;
    }

    @Override
    protected Boolean compute() {
        synchronized (this) {
            boolean error = false;
            try {
                FillingLemmaAndIndexs fillingLemmaAndIndexs
                        = new FillingLemmaAndIndexs();
                SiteRepository siteRepository = repositiries.getSiteRepository();
                PagesRepository pagesRepository = repositiries.getPagesRepository();
                Date currentDate = new Date();
                int siteId = siteMap.getSiteId();
                String homeUrl = siteMap.getHomeUrl();
                String url = siteMap.getUrl();
                Document document = ParseHTML.getContent(url);
                ConcurrentSkipListSet<String> links = parsingLinks(document, homeUrl);
                sleep(350);
                String linkToDataBase = url.replace(homeUrl, "");
                if (pagesRepository.getPagePath(linkToDataBase) == null) {
                    pagesRepository.createPage(siteId, linkToDataBase, document.connection().response().statusCode(),
                            String.valueOf(document));
                    fillingLemmaAndIndexs.processing(repositiries, document, linkToDataBase, siteMap.getHomeUrl());
                    siteRepository.updateStatusTimeSite(currentDate, homeUrl);
                }

                for (String link : links) {
                    siteMap.addChildren(new SiteMap(link, siteId, homeUrl));
                }
                List<SiteMapRecursiveTask> taskList = new ArrayList<>();
                for (SiteMap child : siteMap.getSiteMapChildrens()) {
                    SiteMapRecursiveTask task = new SiteMapRecursiveTask(child, repositiries);
                    task.fork();
                    taskList.add(task);
                }
                for (SiteMapRecursiveTask task : taskList) {
                    task.join();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return error;
        }
    }

    private ConcurrentSkipListSet<String> parsingLinks(Document document, String homeUrl){
        ConcurrentSkipListSet<String> links = new ConcurrentSkipListSet<>();
        PagesRepository pagesRepository = repositiries.getPagesRepository();
        Elements elements = document.select("body").select("a");
        for (Element element : elements) {
            String link = element.absUrl("href");
            if (isNeedLink(link, homeUrl) && !isFile(link)
                    && pagesRepository.getPagePath(link.replace(homeUrl, "")) == null) {
                links.add(link);
            }
        }
        return links;
    }

     private boolean isNeedLink(String link, String homeUrl) {
        StringBuilder sb = new StringBuilder(homeUrl);
        sb.delete(0, homeUrl.indexOf(':'));
        homeUrl = sb.toString();
        String regex = "http[s]?"+homeUrl+"[^#,]*";
        return link.matches(regex);
    }

    private boolean isFile(String link) {
        return link.contains(".jpg") || link.contains(".jpeg") || link.contains(".png") || link.contains(".gif")
                || link.contains(".webp") || link.contains(".pdf") || link.contains(".eps") || link.contains(".xlsx")
                || link.contains(".doc") || link.contains(".pptx") || link.contains(".docx") || link.contains("?_ga")
                || link.contains(".ppt");
    }
}
