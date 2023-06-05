package searchengine.model;

import org.springframework.stereotype.Repository;
import searchengine.model.entity.EnumStatus;
import searchengine.model.entity.Site;

import java.util.Date;
import java.util.Optional;

@Repository()
public interface SiteRepository{
    void createSite(EnumStatus enumStatus, Date statusTime, String url, String name);
    void updateStatusTimeSite(Date statusTime, String url);
    void updateStatusSite(EnumStatus enumStatus, String url);
    void updateStopIndexing();
    void updateLastError(String errorMassage, String url);
    int getSiteIdByUrl(String url);
    int getCountSite();
    Optional<Site> getSiteStatusError(int siteId);
    Date getStatusTime(int siteId);
    void deleteSite(int id);
}
