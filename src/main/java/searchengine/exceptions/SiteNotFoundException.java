package searchengine.exceptions;

public class SiteNotFoundException extends RuntimeException {
    private final int siteId;

    public SiteNotFoundException(int siteId) {
        this.siteId = siteId;
    }

    @Override
    public String getMessage() {
        return "Сайт с id = " + siteId + " не найден";
    }
}
