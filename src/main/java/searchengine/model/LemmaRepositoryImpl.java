package searchengine.model;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@AllArgsConstructor
@Repository
public class LemmaRepositoryImpl implements LemmaRepository{
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final String SQL_CREATE_LEMMA = "insert into lemma (site_id, lemma, frequency)" +
            " values (:siteId, :lemma, :frequency)";
    private static final String SQL_UPDATE_INCREASED_VALUE_LEMMA_OF_SITE =
            "update lemma set frequency = frequency + 1  where id = :id and site_id = :siteId";
    private static final String SQL_GET_LEMMA_BY_LEMMA =
            "select lemma from lemma where site_id = :siteId and lemma = :lemma";
    private static final String SQL_GET_LEMMA_ID_BY_LEMMA =
            "select id from lemma where lemma = :lemma and site_id  = :siteId";
    private static final String SQL_GET_LIST_LEMMA_FREQUENCY =
            "select frequency from lemma where lemma in (:listLemma) and site_id = :siteId";
    private static final String SQL_GET_LEMMA_COUNT_OF_SITE =
            "select count(*) from lemma where site_id = (select id from site where url = :url)";
    private static final String SQL_GET_ALL_LEMMA_OF_PAGE =
            "select lemma from lemma where id in (select lemma_id from indexs where page_id = :pageId)";
    private static final String SQL_DELETE_LEMMA_OF_SITE = "delete from lemma where site_id = :siteId";

    @Override
    public void createLemma(int siteId, String lemma, int frequency) {
        var params = new MapSqlParameterSource();
        params.addValue("siteId", siteId);
        params.addValue("lemma", lemma);
        params.addValue("frequency", frequency);
        jdbcTemplate.update(SQL_CREATE_LEMMA, params);
    }

    @Override
    public void updateIncreasedValueLemmaOfTheSite(int lemmaId, int siteId) {
        var params = new MapSqlParameterSource();
        params.addValue("id", lemmaId);
        params.addValue("siteId", siteId);
        jdbcTemplate.update(SQL_UPDATE_INCREASED_VALUE_LEMMA_OF_SITE, params);
    }

    public void updateCountdownLemmaOfTheSite(StringBuilder insertQuery) {
        var params = new MapSqlParameterSource();
        params.addValue("lemma", null);
        params.addValue("site_id", null);
        params.addValue("frequency", null);
        String SQL_UPDATE_COUNTDOWN_LEMMA_OF_SITE = "insert into lemma (lemma, site_id, `frequency`) values" +
                insertQuery.toString() + "on duplicate key update `frequency` = `frequency` - 1";
        jdbcTemplate.update(SQL_UPDATE_COUNTDOWN_LEMMA_OF_SITE, params);
    }

    @Override
    public int getLemmaId(String lemma, int siteId) {
        var params = new MapSqlParameterSource();
        params.addValue("lemma", lemma);
        params.addValue("siteId", siteId);
        Integer result;
        try {
            result = jdbcTemplate.queryForObject(SQL_GET_LEMMA_ID_BY_LEMMA, params, int.class);
        } catch (EmptyResultDataAccessException | NullPointerException e){
            return 0;
        }
        return result;
    }

    @Override
    public List<Integer> getListLemmaFrequency(List<String> listLemmas, int siteId) {
        var params = new MapSqlParameterSource();
        params.addValue("listLemma", listLemmas);
        params.addValue("siteId", siteId);
        return jdbcTemplate.queryForList(SQL_GET_LIST_LEMMA_FREQUENCY, params, int.class);
    }

    @Override
    public int getLemmaCountOfSite(String url) {
        var params = new MapSqlParameterSource();
        params.addValue("url", url);
        Integer result;
        try {
            result = jdbcTemplate.queryForObject(SQL_GET_LEMMA_COUNT_OF_SITE, params, int.class);
        } catch (EmptyResultDataAccessException | NullPointerException e){
            return 0;
        }
        return result;
    }

    @Override
    public List<String> getAllLemma(int pageId) {
        var params = new MapSqlParameterSource();
        params.addValue("pageId", pageId);
        return jdbcTemplate.queryForList(SQL_GET_ALL_LEMMA_OF_PAGE, params, String.class);
    }

    @Override
    public void deleteAllLemmaOfSite(int siteId) {
        var params = new MapSqlParameterSource();
        params.addValue("siteId", siteId);
        jdbcTemplate.update(SQL_DELETE_LEMMA_OF_SITE, params);
    }
}
