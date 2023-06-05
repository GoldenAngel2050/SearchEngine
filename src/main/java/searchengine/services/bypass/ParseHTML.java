package searchengine.services.bypass;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.lang.Thread.sleep;

@Service
public class ParseHTML{
    private static searchengine.config.Connection connection;

    @Autowired
    public ParseHTML(searchengine.config.Connection connection) {
        ParseHTML.connection = connection;
    }

    protected static Document getContent(String url) {
        Document document = null;
        String userAgent = connection.getUserAgent();
        String referrer = connection.getReferrer();
        try {
            sleep(250);
            Connection.Response connection = Jsoup.connect(url).userAgent(userAgent)
                    .referrer(referrer).timeout(5 * 1000).followRedirects(false).execute();
            document = connection.parse();
        } catch (InterruptedException | IOException e) {
            System.out.println(e + " - " + url);
        }
        return document;
    }
}

