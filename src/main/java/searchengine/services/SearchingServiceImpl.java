package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.statistics.CustomResponse;
import searchengine.dto.statistics.Error;
import searchengine.dto.statistics.SearchingData;
import searchengine.dto.statistics.SearchingResponse;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.processors.LemmaFinder;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class SearchingServiceImpl implements SearchingService {
    Logger logger = Logger.getLogger(IndexingServiceImpl.class.getName());

    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final IndexRepository indexRepository;
    private final SitesList sites;
    private Set<String> queryLemmas;
    LemmaFinder lemmaFinder;
    private final int MAX_SNIPPED_LEN = 160;

    public SearchingServiceImpl(SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, SitesList sites) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
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
        String cleanText = Jsoup.clean(pageText, Safelist.none());

        int maxContextWords = 10; // Слів контексту навколо збігу
        String searchRegex = String.join("|", queryLemmas); // Збіг з будь-якою лемою запиту
        Matcher matcher = Pattern.compile(searchRegex).matcher(cleanText);

        if (matcher.find()) {
            int start = Math.max(0, matcher.start() - maxContextWords);
            int end = Math.min(cleanText.length(), matcher.end() + maxContextWords);
            String snippet = cleanText.substring(start, end);
            return Jsoup.clean(snippet.replaceAll(searchRegex, "<b>$0</b>"), Safelist.basic());
        }
        return ""; // Збігів не знайдено
    }


    private List<SearchingData> lemmaProcessing(List<Lemma> lemmas) {
        List<SearchingData> searchingDataList = new ArrayList<>();
        boolean isFirstLemma = true;
        lemmas.sort(Comparator.comparingInt(Lemma::getFrequency));
        for (Lemma lemma : lemmas) {
            List<Index> pageIndexList = indexRepository.findIndexByLemma(lemma);
            logger.info("Найдено " + pageIndexList.size() + " страниц с леммой "
                    + lemma.getLemma() + " "
                    + pageIndexList.stream().map(pi -> pi.getPage().getPath()).reduce((x, y) -> x + ", " + y).get());
            if (isFirstLemma) {
                isFirstLemma = false;
                searchingDataList.addAll(pageIndexList.stream().map(pi -> {
                    SearchingData searchingData = new SearchingData();
                    searchingData.setSite(lemma.getSite().getUrl());
                    searchingData.setSiteName(lemma.getSite().getName());
                    searchingData.setTitle(Jsoup.parse(pi.getPage().getContent()).title());
                    searchingData.setSnippet(pi.getPage().getContent());
                    searchingData.setUri(pi.getPage().getPath());
                    searchingData.setRelevance(pi.getRank());
                    return searchingData;
                }).toList());
            } else {
                // список страниц со следующей леммой
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
                    if (!isExists)
                        searchingDataIterator.remove();
                }
            }
        }
        return searchingDataList;
    }

    private CustomResponse makeResponse(List<SearchingData> searchingDataList) {

        if (searchingDataList.size() == 0) {
            return new Error("По вашему запросу нет результатов");
        }
        double maxRelevance = searchingDataList.stream()
                .map(SearchingData::getRelevance).max(Double::compare).get();

        searchingDataList.forEach(sd -> {
            sd.setSnippet(MakeSnipped(sd.getSnippet()));
            sd.setRelevance(sd.getRelevance() / maxRelevance);
            logger.info(sd.getSite() + " " + sd.getUri() + " релевантность: " + sd.getRelevance());
        });
        searchingDataList.sort(Comparator.comparingDouble(SearchingData::getRelevance).reversed());
        SearchingResponse searchingResponse = new SearchingResponse();
        searchingResponse.setResult(true);
        searchingResponse.setData(searchingDataList);
        searchingResponse.setCount(searchingDataList.size());
        return searchingResponse;
    }

    @Override
    public CustomResponse search(String query, String url, int offset, int limit) {
        logger.info("Запрос: " + query + " сайт: " + url + " сдвиг: " + offset + " лимит: " + limit);
        if (query.equals("")) {
            return new Error("Задан пустой поисковый запрос");
        }

        queryLemmas = lemmaFinder.getLemmaSet(query);
        if (queryLemmas.size() == 0) {
            return new Error("По вашему запросу нет результатов");
        }

        List<SearchingData> searchingDataList = new ArrayList<>();
        sites.getSites().stream().filter(s -> url == null || s.getUrl().equals(url)).forEach(s -> {
            Site site = siteRepository.findFirstSiteByUrl(s.getUrl());
            logger.info(site.getName() + " " + site.getUrl());
            List<Lemma> lemmas = new ArrayList<>();
            queryLemmas.forEach(l -> {
                lemmas.addAll(lemmaRepository.findLemmaByLemmaAndSite(l, site));
            });
            lemmas.forEach(l -> {
                logger.info(l.getLemma() + " " + l.getSite().getUrl() + " частота: " + l.getFrequency());
            });
            searchingDataList.addAll(lemmaProcessing(lemmas));
        });

        return makeResponse(searchingDataList);
    }
}
