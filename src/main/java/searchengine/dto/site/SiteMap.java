package searchengine.dto.site;

import lombok.Data;

import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class SiteMap {
    private final String url;
    private final int siteId;
    private final String homeUrl;
    private CopyOnWriteArrayList<SiteMap> siteMapChildrens;
    public SiteMap(String url, int siteId, String homeUrl) {
        siteMapChildrens = new CopyOnWriteArrayList<>();
        this.url = url;
        this.siteId = siteId;
        this.homeUrl = homeUrl;
    }
    public void addChildren(SiteMap children) {
        siteMapChildrens.add(children);
    }
}
