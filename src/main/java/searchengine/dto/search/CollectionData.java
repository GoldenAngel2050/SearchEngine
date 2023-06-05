package searchengine.dto.search;

import lombok.Data;
import searchengine.config.Site;

import java.util.HashMap;
import java.util.List;

@Data
public class CollectionData {
    private List<String> listContentOfPages;
    private List<Integer> listRelativeRelevance;
    private List<String> listOfPagesWithEachLemma;
    private HashMap<String, Integer> filteredLemmasCount;
    private Site site;
    private int pageIndex;
}
