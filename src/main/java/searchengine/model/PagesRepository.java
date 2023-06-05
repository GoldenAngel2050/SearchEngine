package searchengine.model;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagesRepository {
    String getPagePath(String path);
    int getPageIdByPath(String path, int siteId);
    List<String> getPathByListId(List<Integer> listId);
    List<String> getContentOfPages(List<String> pathsPages);
    int getCountPagesOfSite(String url);
    void createPage(int sideId, String path, int code, String content);
    void deletePage(String path);
    void deleteAllPagesOfSite(int siteId);


}
