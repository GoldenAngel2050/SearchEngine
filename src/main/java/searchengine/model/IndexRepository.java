package searchengine.model;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndexRepository {

    void createIndex(int pageId, int lemmaId, int rank);
    List<Integer> getAllPagesIdOfLemma(String lemma, int siteId);
    List<Integer> getListRanksOfLemmas(List<String> listLemmas, int siteId, String pagePath);
    void deleteAllDataOfPage(int pageId);
    void deleteFromIndexsPagesOfSite(int siteId);

}
