package searchengine.model;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@AllArgsConstructor
@Repository
public class PagesRepositoryImpl implements PagesRepository{
    private static final String SQL_CREATE_PAGE =
            "insert into pages (site_id, path, code, content) values (:siteId, :path, :code, :content)";
    private static final String SQL_GET_PATH_OF_PAGE = "select path from pages where path = :path";
    private static final String SQL_GET_PAGE_ID_BY_URL = "select id from pages where path = :path " +
            "and site_id = :siteId";
    private static final String SQL_GET_PATH_OF_PAGE_BY_ID = "select path from pages where id in (:listId)";
    private static final String SQL_GET_CONTENT_OF_PAGES = "select content from pages where path in (:listPaths)";
    private static final String SQL_GET_COUNT_OF_PAGES = "select count(*) from pages where site_id = " +
            "(select id from site where url = :url)";
    private static final String SQL_DELETE_PAGE = "delete from pages where path = :path";
    private static final String SQL_DELETE_ALL_PAGES_CERTAIN_SITE = "delete from pages where site_id = :siteId";


    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void createPage(int sideId, String path, int code, String content) {
        var params = new MapSqlParameterSource();
        params.addValue("siteId", sideId);
        params.addValue("path", path);
        params.addValue("code", code);
        params.addValue("content", content);
        jdbcTemplate.update(SQL_CREATE_PAGE, params);
    }
    @Override
    public String getPagePath(String path) {
        var params = new MapSqlParameterSource();
        params.addValue("path", path);
        String result;
        try {
            result = jdbcTemplate.queryForObject(SQL_GET_PATH_OF_PAGE, params, String.class);
        } catch (EmptyResultDataAccessException e){
            return null;
        }
        return result;
    }

    @Override
    public List<String> getPathByListId(List<Integer> listId) {
        var params = new MapSqlParameterSource();
        params.addValue("listId", listId);
        return jdbcTemplate.queryForList(SQL_GET_PATH_OF_PAGE_BY_ID, params, String.class);
    }

    @Override
    public List<String> getContentOfPages(List<String> listPaths) {
        var params = new MapSqlParameterSource();
        params.addValue("listPaths", listPaths);
        return jdbcTemplate.queryForList(SQL_GET_CONTENT_OF_PAGES, params, String.class);
    }

    @Override
    public int getPageIdByPath(String path, int siteId) {
        var params = new MapSqlParameterSource();
        params.addValue("path", path);
        params.addValue("siteId", siteId);
        Integer result;
        try {
            result = jdbcTemplate.queryForObject(SQL_GET_PAGE_ID_BY_URL, params, int.class);
        } catch (EmptyResultDataAccessException | NullPointerException ex){
            return 0;
        }
        return result;
    }

    @Override
    public int getCountPagesOfSite(String url) {
        var params = new MapSqlParameterSource();
        params.addValue("url", url);
        Integer result;
        try {
            result = jdbcTemplate.queryForObject(SQL_GET_COUNT_OF_PAGES, params, int.class);
        } catch (EmptyResultDataAccessException | NullPointerException ex){
            return 0;
        }
        return result;
    }

    @Override
    public void deletePage(String path) {
        var params = new MapSqlParameterSource();
        params.addValue("path", path);
        jdbcTemplate.update(SQL_DELETE_PAGE, params);
    }

    @Override
    public void deleteAllPagesOfSite(int siteId) {
        var params = new MapSqlParameterSource();
        params.addValue("siteId", siteId);
        jdbcTemplate.update(SQL_DELETE_ALL_PAGES_CERTAIN_SITE, params);

    }
}
