package searchengine.model;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import searchengine.model.entity.EnumStatus;
import searchengine.model.entity.Site;

import java.util.Date;
import java.util.Optional;

@AllArgsConstructor
@Repository
public class SiteRepositoryImpl implements SiteRepository {
    private static final String SQL_CREATE_SITE =
            "insert into site (status, status_time, last_error, url, name) " +
                    "values (:status, :statusTime, null, :url, :name)";
    private static final String SQL_UPDATE_STATUS_TIME_SITE = "update site set status_time = :statusTime " +
            "where url = :url";
    private static final String SQL_UPDATE_STATUS_SITE = "update site set status = :status where url = :url";
    private static final String SQL_UPDATE_LAST_ERROR = "update site set last_error = :lastError where url = :url";
    private static final String SQL_UPDATE_STOP_INDEXING = "update site set status = 'FAILED', " +
            "last_error = 'Индексация прервана пользователем' where status = 'INDEXING'";
    private static final String SQL_GET_SITE_ID_BY_URL = "select id from site where url = :url";
    private static final String SQL_GET_COUNT_OF_SITES = "select count(*) from site";
    private static final String SQL_GET_SITE_STATUS_ERROR_TIME = "select status, status_time, last_error from site " +
            "where id = :id";
    private static final String SQL_DELETE_SITE = "delete from site where id = :id";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SiteMapper siteMapper;

    @Override
    public void createSite(EnumStatus enumStatus, Date statusTime, String url, String name) {
        var params = new MapSqlParameterSource();
        params.addValue("status", String.valueOf(enumStatus));
        params.addValue("statusTime", statusTime);
        params.addValue("url", url);
        params.addValue("name", name);
        jdbcTemplate.update(SQL_CREATE_SITE, params);
    }

    @Override
    public void updateStatusTimeSite(Date statusTime, String url) {
        var params = new MapSqlParameterSource();
        params.addValue("statusTime", statusTime);
        params.addValue("url", url);
        jdbcTemplate.update(SQL_UPDATE_STATUS_TIME_SITE, params);
    }

    @Override
    public void updateStatusSite(EnumStatus enumStatus, String url) {
        var params = new MapSqlParameterSource();
        params.addValue("status", String.valueOf(enumStatus));
        params.addValue("url", url);
        jdbcTemplate.update(SQL_UPDATE_STATUS_SITE, params);
    }

    @Override
    public void updateStopIndexing() {
        var params = new MapSqlParameterSource();
        jdbcTemplate.update(SQL_UPDATE_STOP_INDEXING, params);
    }

    @Override
    public void updateLastError(String errorMassage, String url) {
        var params = new MapSqlParameterSource();
        params.addValue("lastError", errorMassage);
        params.addValue("url", url);
        jdbcTemplate.update(SQL_UPDATE_LAST_ERROR, params);
    }

    @Override
    public int getSiteIdByUrl(String url) {
        var params = new MapSqlParameterSource();
        params.addValue("url", url);
        Integer result;
        try {
            result = jdbcTemplate.queryForObject(SQL_GET_SITE_ID_BY_URL, params, int.class);
        } catch (EmptyResultDataAccessException | NullPointerException e){
            return 0;
        }
        return result;
    }

    @Override
    public int getCountSite() {
        var params = new MapSqlParameterSource();
        params.addValue("siteId", null);
        Integer result;
        try {
            result = jdbcTemplate.queryForObject(SQL_GET_COUNT_OF_SITES, params, int.class);
        } catch (EmptyResultDataAccessException | NullPointerException ex){
            return 0;
        }
        return result;
    }

    @Override
    public Optional<Site> getSiteStatusError(int siteId) {
        var params = new MapSqlParameterSource();
        params.addValue("id", siteId);
        return jdbcTemplate.query(SQL_GET_SITE_STATUS_ERROR_TIME, params, siteMapper).stream().findFirst();
    }

    @Override
    public Date getStatusTime(int siteId) {
        var params = new MapSqlParameterSource();
        params.addValue("id", siteId);
        return jdbcTemplate.queryForObject("select status_time from site where id = :id", params, Date.class);
    }

    @Override
    public void deleteSite(int id) {
        var params = new MapSqlParameterSource();
        params.addValue("id", id);
        jdbcTemplate.update(SQL_DELETE_SITE, params);
    }
}
