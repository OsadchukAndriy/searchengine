package searchengine.services.implement;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.statistics.CustomResponse;
import searchengine.dto.statistics.Error;
import searchengine.dto.statistics.SearchingData;
import searchengine.dto.statistics.SearchingResponse;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.utils.Lemma.LemmaFinder;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.interfaces.SearchingService;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class SearchingServiceImpl implements SearchingService {
    Logger logger = Logger.getLogger(IndexingServiceImpl.class.getName());

    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SitesList sites;
    private Set<String> queryLemmas;
    private LemmaFinder lemmaFinder;

    private final int MAX_SNIPPED_LEN = 30;

    public SearchingServiceImpl(SiteRepository siteRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, SitesList sites) {
        this.siteRepository = siteRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.sites = sites;
        try {
            this.lemmaFinder = LemmaFinder.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String MakeSnipped(String htmlText) {
        String pageText = Jsoup.parse(htmlText).text();
        pageText = Jsoup.clean(pageText, Safelist.none());

        List<String> snippets = new ArrayList<>();
        String REGEX = "[а-яА-Я]+";
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(pageText);

        Map<Integer, String> lemmaPositions = new TreeMap<>();
        while (matcher.find()) {
            String word = matcher.group();
            String lemma = lemmaFinder.getLemmaSet(word).stream().reduce("", String::concat);
            if (queryLemmas.contains(lemma)) {
                lemmaPositions.put(matcher.start(), word);
            }
        }

        if (lemmaPositions.isEmpty()) {
            return "";
        }

        Set<String> foundLemmas = new HashSet<>();

        for (Map.Entry<Integer, String> entry : lemmaPositions.entrySet()) {
            int wordStart = entry.getKey();
            String word = entry.getValue();
            String lemma = lemmaFinder.getLemmaSet(word).stream().reduce("", String::concat);

            if (foundLemmas.contains(lemma)) {
                continue;
            }

            int start = Math.max(0, wordStart - MAX_SNIPPED_LEN / 2);
            int end = Math.min(pageText.length(), wordStart + word.length() + MAX_SNIPPED_LEN / 2);

            String snippet = pageText.substring(start, end);
            snippet = snippet.replace(word, "<b>" + word + "</b>");

            snippets.add(snippet);
            foundLemmas.add(lemma);

            if (foundLemmas.size() >= queryLemmas.size()) {
                break;
            }
        }
        return String.join(" ... ", snippets);
    }

    private List<SearchingData> lemmaProcessing(List<Lemma> lemmas) {
        List<SearchingData> searchingDataList = new ArrayList<>();
        lemmas.sort(Comparator.comparingInt(Lemma::getFrequency));
        boolean isFirstLemma = true;

        for (Lemma lemma : lemmas) {
            List<Index> pageIndexList = indexRepository.findIndexByLemma(lemma);
            logger.info("Найдено " + pageIndexList.size() + " страниц с леммой "
                    + lemma.getLemma() + " "
                    + pageIndexList.stream().map(pi -> pi.getPage().getPath()).reduce((x, y) -> x + ", " + y).isPresent());

            if (isFirstLemma) {
                isFirstLemma = false;
                searchingDataList.addAll(createInitialSearchingDataList(pageIndexList, lemma));
            } else {
                updateSearchingDataList(searchingDataList, pageIndexList);
            }
        }
        return searchingDataList;
    }

    private List<SearchingData> createInitialSearchingDataList(List<Index> pageIndexList, Lemma lemma) {
        return pageIndexList.stream().map(pi -> {
            SearchingData searchingData = new SearchingData();
            searchingData.setSite(lemma.getSite().getUrl());
            searchingData.setSiteName(lemma.getSite().getName());
            searchingData.setTitle(Jsoup.parse(pi.getPage().getContent()).title());
            searchingData.setSnippet(pi.getPage().getContent());
            searchingData.setUri(pi.getPage().getPath());
            searchingData.setRelevance(pi.getRank());
            return searchingData;
        }).collect(Collectors.toList());
    }

    private void updateSearchingDataList(List<SearchingData> searchingDataList, List<Index> pageIndexList) {
        Iterator<SearchingData> searchingDataIterator = searchingDataList.iterator();
        while (searchingDataIterator.hasNext()) {
            SearchingData searchingData = searchingDataIterator.next();
            boolean isExists = false;
            for (Index pageIndex : pageIndexList) {
                if (searchingData.getUri().equals(pageIndex.getPage().getPath())) {
                    searchingData.setRelevance(searchingData.getRelevance() + pageIndex.getRank());
                    isExists = true;
                    break;
                }
            }
            if (!isExists) {
                searchingDataIterator.remove();
            }
        }
    }

    private CustomResponse makeResponse(List<SearchingData> searchingDataList, int offset, int limit) {
        if (searchingDataList.isEmpty()) {
            return new Error("По вашему запросу нет результатов");
        }

        double maxRelevance = searchingDataList.stream()
                .map(SearchingData::getRelevance).max(Double::compare).orElse(0.0);

        searchingDataList.forEach(sd -> {
            sd.setSnippet(MakeSnipped(sd.getSnippet()));
            sd.setRelevance(sd.getRelevance() / maxRelevance);
            logger.info(sd.getSite() + " " + sd.getUri() + " релевантность: " + sd.getRelevance());
        });
        searchingDataList.sort(Comparator.comparingDouble(SearchingData::getRelevance).reversed());

        //pagination
        int totalResults = searchingDataList.size();
        int endIndex = Math.min(totalResults, offset + limit);
        List<SearchingData> paginatedResults = searchingDataList.subList(offset, endIndex);

        SearchingResponse searchingResponse = new SearchingResponse();
        searchingResponse.setResult(true);
        searchingResponse.setData(paginatedResults);
        searchingResponse.setCount(totalResults);
        return searchingResponse;
    }

    @Override
    public CustomResponse search(String query, String url, int offset, int limit) {
        logger.info("Запрос: " + query + " сайт: " + url + " сдвиг: " + offset + " лимит: " + limit);
        if (query.isEmpty()) {
            return new Error("Задан пустой поисковый запрос");
        }

        queryLemmas = lemmaFinder.getLemmaSet(query);
        if (queryLemmas.isEmpty()) {
            return new Error("По вашему запросу нет результатов");
        }

        List<SearchingData> searchingDataList = new ArrayList<>();
        sites.getSites().stream().filter(s -> url == null || s.getUrl().equals(url)).forEach(s -> {
            Site site = siteRepository.findFirstSiteByUrl(s.getUrl());
            logger.info(site.getName() + " " + site.getUrl());
            List<Lemma> lemmas = new ArrayList<>(queryLemmas.stream()
                    .flatMap(l -> lemmaRepository.findLemmaByLemmaAndSite(l, site).stream())
                    .toList());
            lemmas.forEach(l -> logger.info(String.format("%s %s частота: %d", l.getLemma(), l.getSite().getUrl(), l.getFrequency())));
            searchingDataList.addAll(lemmaProcessing(lemmas));
        });

        return makeResponse(searchingDataList, offset, limit);
    }
}
