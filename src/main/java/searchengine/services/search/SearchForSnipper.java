package searchengine.services.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchForSnipper
{

    private static StringBuilder sbtext = null;

    public static List<Integer> searchAndHighlightingWords(String text, String word) {
        if (sbtext == null) {
            sbtext = new StringBuilder(text);
        }
        List<Integer> positionStartEnd = new ArrayList<>();
        String regex = word + "[а-я]*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sbtext);
        while (matcher.find()) {
            sbtext.insert(matcher.start(), "<b>");
            positionStartEnd.add(matcher.start()-3);
            int endWord = sbtext.indexOf(" ", matcher.end());
            sbtext.insert(endWord, "</b>");
            positionStartEnd.add(endWord+9);
        }
        return positionStartEnd;
    }

    public static StringBuilder getSbtext() {
        return sbtext;
    }
}