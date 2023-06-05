package searchengine.services.indexing;

import lombok.AllArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@AllArgsConstructor
public class Lemmanization {

    protected static HashMap<String, Integer> HTMLCountLemma(Document document){
        return getLemmasCount(document.text());
    }

    public static HashMap<String, Integer> getLemmasCount(String text) {
        HashMap<String, Integer> lemmas = new HashMap<>();
        String[] words = textClean(text);
        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            for (String word : words) {
                if (!word.isEmpty()) {
                    List<String> lemmaForms = luceneMorph.getNormalForms(word);
                    String lemmaForm = lemmaForms.get(0);
                    if (lemmas.containsKey(lemmaForm)) {
                        lemmas.put(lemmaForm, lemmas.get(lemmaForm) + 1);
                    } else {
                        lemmas.put(lemmaForm, 1);
                    }
                }
            }
        } catch (IOException ex){
            System.out.println(ex.getMessage());
        }
        return lemmas;
    }

    public static List<String> getListOfLemma(String text){
        List<String> lemmas = new ArrayList<>();
        String[] words = textClean(text);
        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            for (String word : words) {
                List<String> lemmaForms;
                if (!word.isEmpty()) {
                    lemmaForms = luceneMorph.getNormalForms(word);
                    String lemmaForm = lemmaForms.get(0);
                    if (!lemmas.contains(lemmaForm)) {
                        lemmas.add(lemmaForm);
                    }
                }
            }
        } catch (IOException ex){
            System.out.println(ex.getMessage());
        }
        return lemmas;
    }

    private static String[] textClean(String text) {
        String[] words = text
                .toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            for (int i = 0; i < words.length; i++) {
                List<String> wordBaseForms = luceneMorph.getMorphInfo(words[i]);
                for (String wordForm : wordBaseForms) {
                    if (wordForm.contains("МЕЖД") || wordForm.contains("СОЮЗ") || wordForm.contains("ПРЕДЛ")
                            || wordForm.contains("МС") || wordForm.contains("МС-П")) {
                        words[i] = "";
                        break;
                    }
                }
            }
        } catch (IOException ex){
            System.out.println(ex.getMessage());
        }
        return words;
    }
}
