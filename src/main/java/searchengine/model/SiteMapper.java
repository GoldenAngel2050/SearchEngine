package searchengine.model;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import searchengine.model.entity.EnumStatus;
import searchengine.model.entity.Site;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class SiteMapper implements RowMapper<Site> {
    @Override
    public Site mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Site(EnumStatus.valueOf(rs.getString("status")), rs.getString("last_error"));
    }
}
