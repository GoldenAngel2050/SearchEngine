package searchengine.services.indexing;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import searchengine.dto.Repositiries;
import searchengine.model.IndexRepository;
import searchengine.model.LemmaRepository;
import searchengine.model.PagesRepository;
import searchengine.model.SiteRepository;

import java.util.HashMap;
import java.util.Map;

public class FillingLemmaAndIndexs {

    public void processing(@NotNull Repositiries repositiries, Document document, String link, String siteUrl) {
        SiteRepository siteRepository = repositiries.getSiteRepository();
        PagesRepository pagesRepository = repositiries.getPagesRepository();
        IndexRepository indexRepository = repositiries.getIndexRepository();
        LemmaRepository lemmaRepository = repositiries.getLemmaRepository();

        int siteId = siteRepository.getSiteIdByUrl(siteUrl);
        int pageId = pagesRepository.getPageIdByPath(link, siteId);
        HashMap<String, Integer> countedLemma = Lemmanization.HTMLCountLemma(document);

        for (Map.Entry<String, Integer> entry : countedLemma.entrySet()) {
            int lemmaId = lemmaRepository.getLemmaId(entry.getKey(), siteId);
            if (lemmaId != 0) {
                lemmaRepository.updateIncreasedValueLemmaOfTheSite(lemmaId, siteId);
            } else {
                lemmaRepository.createLemma(siteId, entry.getKey(), 1);
                lemmaId = lemmaRepository.getLemmaId(entry.getKey(), siteId);
            }
            indexRepository.createIndex(pageId, lemmaId, entry.getValue());
        }
    }
}
