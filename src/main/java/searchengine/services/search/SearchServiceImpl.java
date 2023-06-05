package searchengine.services.search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.search.CollectionData;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponse;
import searchengine.model.IndexRepository;
import searchengine.model.LemmaRepository;
import searchengine.model.PagesRepository;
import searchengine.model.SiteRepository;
import searchengine.services.indexing.Lemmanization;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PagesRepository pagesRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    private final SitesList sites;
    private SearchResponse searchResponse = new SearchResponse();
    private SearchData searchData = new SearchData();

    public SearchServiceImpl(SitesList sites){
        this.sites = sites;
    }

    @Override
    public SearchResponse search(String query, int offset, int limit, String siteUrl) {
        List<Site> sitesList = sites.getSites();
        for(Site siteFromList : sitesList) {
            if (siteUrl != null) {
                if (siteFromList.getUrl().equals(siteUrl)) {
                    processingSite(query, offset, limit, siteFromList);
                }
            } else {
                processingSite(query, offset, limit, siteFromList);
            }
        }
        return searchResponse;
    }

    private void processingSite(String query, int offset, int limit, Site site){
        int siteId = siteRepository.getSiteIdByUrl(site.getUrl());
        List<String> listOfLemma = Lemmanization.getListOfLemma(query);
        List<Integer> listLemmaFrequency = lemmaRepository.getListLemmaFrequency(listOfLemma, siteId);
        HashMap<String, Integer> filteredLemmasCount = new HashMap<>();

        if(!listLemmaFrequency.isEmpty()) {
            for (int i = 0; i < listOfLemma.size(); i++) {
                int lemmaFrequency = listLemmaFrequency.get(i);
                if (lemmaFrequency < 100 && lemmaFrequency != 0) {
                    filteredLemmasCount.put(listOfLemma.get(i), lemmaFrequency);
                }
            }

            if (!filteredLemmasCount.isEmpty()) {
                Map<String, Integer> ascendingByFrequency = ascendingSort(filteredLemmasCount);
                List<String> listOfPagesWithEachLemma = pageListShortening(ascendingByFrequency, siteId);

                if (!listOfPagesWithEachLemma.isEmpty()) {
                    List<Integer> listRelativeRelevance = calculationOfRelativeRelevance(listOfPagesWithEachLemma,
                            listOfLemma, siteId);
                    List<String> listContentOfPages = pagesRepository.getContentOfPages(listOfPagesWithEachLemma);
                    List<SearchData> searchDataList = new ArrayList<>();
                    for (int pageIndex = 0; pageIndex < listOfPagesWithEachLemma.size(); pageIndex++) {
                        CollectionData collectionData = new CollectionData();
                        collectionData.setListOfPagesWithEachLemma(listOfPagesWithEachLemma);
                        collectionData.setListRelativeRelevance(listRelativeRelevance);
                        collectionData.setListContentOfPages(listContentOfPages);
                        collectionData.setFilteredLemmasCount(filteredLemmasCount);
                        collectionData.setPageIndex(pageIndex);
                        collectionData.setSite(site);
                        searchDataList.add(collectionSearchDataOfPage(collectionData));
                    }

                    List<SearchData> limitSearchDataList = new ArrayList<>();
                    if (listOfPagesWithEachLemma.size() + offset >= limit)
                        for (int j = offset; j < limit; j++) {
                            limitSearchDataList.add(searchDataList.get(j));
                            searchResponse.setData(limitSearchDataList);
                        }
                    else {
                        searchResponse.setData(searchDataList);
                    }
                    searchResponse.setCount(searchDataList.size());
                    searchResponse.setResult(true);
                }
            }
        }
    }

    private Map<String, Integer>  ascendingSort(HashMap<String, Integer> lemmasCount){
         return lemmasCount.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private List<String> pageListShortening(Map<String, Integer> ascendingByFrequencyMap, int siteId){
        List<String> listPagesPathOfFirstLemma = new ArrayList<>();
        List<String> listPagesPathOfLemma = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : ascendingByFrequencyMap.entrySet()) {
            List<Integer> listPagesIdOfLemma = indexRepository.getAllPagesIdOfLemma(entry.getKey() , siteId);
            if (listPagesPathOfFirstLemma.isEmpty()) {
                listPagesPathOfFirstLemma.addAll(pagesRepository.getPathByListId(listPagesIdOfLemma));
            } else {
                listPagesPathOfLemma.addAll(pagesRepository.getPathByListId(listPagesIdOfLemma));
                listPagesPathOfFirstLemma.retainAll(listPagesPathOfLemma);
            }
        }
        return listPagesPathOfFirstLemma;
    }

    private List<Integer> calculationOfRelativeRelevance(List<String> listOfPagesWithEachLemma,
                                                         List<String> listOfLemma, int siteId){
        List<Integer> listABSRelevance = new ArrayList<>();
        for (String pagePath : listOfPagesWithEachLemma) {
            int ABSRelevance = 0;
            List<Integer> listRankOfLemmasOnPage = indexRepository.getListRanksOfLemmas(listOfLemma, siteId, pagePath);
            for (int rankOfLemma : listRankOfLemmasOnPage) {
                ABSRelevance += rankOfLemma;
            }
            listABSRelevance.add(ABSRelevance);
        }
        int maxABSRelevance = Collections.max(listABSRelevance);
        List<Integer> listRelativeRevelance = new ArrayList<>();
        for (int ABSRelevance : listABSRelevance) {
            int relativeRevelance = ABSRelevance / maxABSRelevance;
            listRelativeRevelance.add(relativeRevelance);
        }
        Collections.reverse(listRelativeRevelance);
        return listRelativeRevelance;
    }

    private SearchData collectionSearchDataOfPage(CollectionData collectionData){
        HashMap<String, Integer> filteredLemmasCount = collectionData.getFilteredLemmasCount();
        List<String> listOfPagesWithEachLemma = collectionData.getListOfPagesWithEachLemma();
        List<Integer> listRelativeRelevance = collectionData.getListRelativeRelevance();
        List<String> listContentOfPages = collectionData.getListContentOfPages();
        int pageIndex = collectionData.getPageIndex();
        Site site = collectionData.getSite();

        Document document = Jsoup.parse(listContentOfPages.get(pageIndex));
        String text = document.text();

        int minPositionWords = 1_000_000;
        int maxPositionWords = 0;
        for (Map.Entry<String, Integer> entry : filteredLemmasCount.entrySet()) {
            List<Integer> listStartEndWord = SearchForSnipper.searchAndHighlightingWords(text, entry.getKey());
            for (Integer positionWord : listStartEndWord) {
                if (minPositionWords > positionWord && positionWord > 0) {
                    minPositionWords = positionWord;
                } else if (maxPositionWords < positionWord) {
                    maxPositionWords = positionWord;
                }
            }
        }
        if(!(minPositionWords == 1_000_000) && !(maxPositionWords == 0)) {
            StringBuilder sbtext = SearchForSnipper.getSbtext();
            String substringText = sbtext.substring(minPositionWords, maxPositionWords);
            if (substringText.length() > 350) {
                substringText = substringText.substring(0, 350);
            }
            searchData.setSite(site.getUrl());
            searchData.setSiteName(site.getName());
            searchData.setUri(listOfPagesWithEachLemma.get(pageIndex));
            searchData.setTitle(document.title());
            searchData.setSnippet(substringText);
            searchData.setRelevance(listRelativeRelevance.get(pageIndex));
        }
        return searchData;
    }

}
