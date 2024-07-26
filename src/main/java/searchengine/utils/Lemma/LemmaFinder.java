package searchengine.utils.Lemma;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class LemmaFinder {

    private final LuceneMorphology russianMorphology;
    private final LuceneMorphology englishMorphology;
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Яa-zA-Z\\s]";
    private static final String[] russianParticlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
    private static final String[] englishParticlesNames = new String[]{"CONJ", "PREP", "PRON"};

    public static LemmaFinder getInstance() throws IOException {
        LuceneMorphology russianMorphology = new RussianLuceneMorphology();
        LuceneMorphology englishMorphology = new EnglishLuceneMorphology();
        return new LemmaFinder(russianMorphology, englishMorphology);
    }

    private LemmaFinder(LuceneMorphology russianMorphology, LuceneMorphology englishMorphology) {
        this.russianMorphology = russianMorphology;
        this.englishMorphology = englishMorphology;
    }

    private LemmaFinder() {
        throw new RuntimeException("Disallow construct");
    }

    public Map<String, Integer> collectLemmas(String text) {
        String[] words = splitWords(text);
        HashMap<String, Integer> lemmas = new HashMap<>();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            LuceneMorphology morphology = getMorphology(word);
            if (morphology == null) {
                continue;
            }

            List<String> wordBaseForms = morphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms, morphology)) {
                continue;
            }

            List<String> normalForms = morphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }

            String normalWord = normalForms.get(0);

            lemmas.put(normalWord, lemmas.getOrDefault(normalWord, 0) + 1);
        }

        return lemmas;
    }

    public Set<String> getLemmaSet(String text) {
        String[] textArray = splitWords(text);
        Set<String> lemmaSet = new HashSet<>();

        for (String word : textArray) {
            if (!word.isEmpty() && isCorrectWordForm(word)) {
                LuceneMorphology morphology = getMorphology(word);
                if (morphology == null) {
                    continue;
                }

                List<String> wordBaseForms = morphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForms, morphology)) {
                    continue;
                }

                lemmaSet.addAll(morphology.getNormalForms(word));
            }
        }

        return lemmaSet;
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms, LuceneMorphology morphology) {
        String[] particlesNames = morphology instanceof RussianLuceneMorphology ? russianParticlesNames : englishParticlesNames;
        return wordBaseForms.stream().anyMatch(base -> hasParticleProperty(base, particlesNames));
    }

    private boolean hasParticleProperty(String wordBase, String[] particlesNames) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private String[] splitWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-яa-z\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private boolean isCorrectWordForm(String word) {
        LuceneMorphology morphology = getMorphology(word);
        if (morphology == null) {
            return false;
        }

        List<String> wordInfo = morphology.getMorphInfo(word);
        for (String morphInfo : wordInfo) {
            if (morphInfo.matches(WORD_TYPE_REGEX)) {
                return false;
            }
        }
        return true;
    }

    private LuceneMorphology getMorphology(String word) {
        if (word.matches("[а-яА-Я]+")) {
            return russianMorphology;
        } else if (word.matches("[a-zA-Z]+")) {
            return englishMorphology;
        }
        return null;
    }
}