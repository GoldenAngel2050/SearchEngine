package searchengine.dto;

import lombok.Data;
import searchengine.model.IndexRepository;
import searchengine.model.LemmaRepository;
import searchengine.model.PagesRepository;
import searchengine.model.SiteRepository;

@Data
public class Repositiries {
    private SiteRepository siteRepository;
    private PagesRepository pagesRepository;
    private IndexRepository indexRepository;
    private LemmaRepository lemmaRepository;
}
