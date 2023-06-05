package searchengine.model;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LemmaRepository {
    void createLemma(int siteId, String lemma, int frequency);
    void updateIncreasedValueLemmaOfTheSite(int lemmaId, int siteId);
    void updateCountdownLemmaOfTheSite(StringBuilder insertQuery);
    int getLemmaId(String lemma, int siteId);
    List<Integer> getListLemmaFrequency(List<String> listLemma, int siteId);
    int getLemmaCountOfSite(String url);
    List<String> getAllLemma(int pageId);
    void deleteAllLemmaOfSite(int siteId);
}
