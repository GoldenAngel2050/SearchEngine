package searchengine.model;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@AllArgsConstructor
@Repository
public class IndexRepositoryImpl implements IndexRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final String SQL_CREATE_INDEX =
            "insert into indexs (page_id, lemma_id, ranked) values (:pageId, :lemmaId, :ranked)";
    private static final String SQL_GET_ALL_PAGES_ID_OF_LEMMA =
            "select page_id from indexs where lemma_id = " +
                    "(select id from lemma where lemma = :lemma and site_id  = :siteId)";
    private static final String SQL_GET_RANKS_OF_LEMMAS =
            "select ranked from indexs where lemma_id in " +
                    "(select id from lemma where lemma in (:listLemmas) and site_id  = :siteId) and page_id in " +
                    "(select id from pages where path = :pagePath)";
    private static final String SQL_DELETE_ALL_DATA_OF_PAGE = "delete from indexs where page_id = :pageId";
    private static final String SQL_DELETE_FROM_INDEXS_PAGES_BY_SITE =
            "delete from indexs where page_id in (select id from pages where site_id = :siteId)";


    @Override
    public void createIndex(int pageId, int lemmaId, int rank) {
        var params = new MapSqlParameterSource();
        params.addValue("pageId", pageId);
        params.addValue("lemmaId", lemmaId);
        params.addValue("ranked", rank);
        jdbcTemplate.update(SQL_CREATE_INDEX, params);
    }


    @Override
    public List<Integer> getAllPagesIdOfLemma(String lemma, int siteId) {
        var params = new MapSqlParameterSource();
        params.addValue("lemma", lemma);
        params.addValue("siteId", siteId);
        return jdbcTemplate.queryForList(SQL_GET_ALL_PAGES_ID_OF_LEMMA, params, Integer.class);
    }

    @Override
    public List<Integer> getListRanksOfLemmas(List<String> listLemmas, int siteId, String pagePath) {
        var params = new MapSqlParameterSource();
        params.addValue("listLemmas", listLemmas);
        params.addValue("siteId", siteId);
        params.addValue("pagePath", pagePath);
        return jdbcTemplate.queryForList(SQL_GET_RANKS_OF_LEMMAS, params, Integer.class);
    }

    @Override
    public void deleteAllDataOfPage(int pageId) {
        var params = new MapSqlParameterSource();
        params.addValue("pageId", pageId);
        jdbcTemplate.update(SQL_DELETE_ALL_DATA_OF_PAGE, params);
    }

    @Override
    public void deleteFromIndexsPagesOfSite(int siteId) {
        var params = new MapSqlParameterSource();
        params.addValue("siteId", siteId);
        jdbcTemplate.update(SQL_DELETE_FROM_INDEXS_PAGES_BY_SITE, params);
    }
}
