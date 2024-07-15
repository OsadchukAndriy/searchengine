package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.statistics.CustomResponse;
import searchengine.dto.statistics.Error;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.processors.SiteIndexator;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    private final SitesList sites;
    private ForkJoinPool pool;

    private final Logger logger = Logger.getLogger(IndexingServiceImpl.class.getName());

    // Singleton
    ForkJoinPool getPoolInstance() {
        if (pool == null || pool.isShutdown()) {
            pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        }
        return pool;
    }

    private SiteIndexator InitSiteIndexator(String name, String url) {
        Site site = new Site();
        site.setName(name);
        site.setUrl(url);
        site.setStatus(SiteStatus.INDEXING);
        site.setLastError("");
        SiteIndexator siteIndexator = new SiteIndexator();
        siteIndexator.setSiteRepository(siteRepository);
        siteIndexator.setPageRepository(pageRepository);
        siteIndexator.setLemmaRepository(lemmaRepository);
        siteIndexator.setIndexRepository(indexRepository);
        siteIndexator.setSite(site);
        siteIndexator.setPages(new HashSet<>());
        siteIndexator.setLemmas(new HashMap<>());
        siteIndexator.setIndexes(new HashSet<>());
        return siteIndexator;
    }

    public CustomResponse startAll() {
        pool = getPoolInstance();

        if (pool.getActiveThreadCount() > 0) {
            return new Error("Индексация уже запущена");
        } else {
            sites.getSites().forEach(s -> {
                pool.execute(InitSiteIndexator(s.getName(), s.getUrl()));
            });
            return new CustomResponse();
        }
    }

    public CustomResponse stopAll() {
        pool = getPoolInstance();
        if (pool.getActiveThreadCount() > 0) {
            pool.shutdownNow();
            try {
                pool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Iterable<Site> siteIterable = siteRepository.findAll();
            for (Site site : siteIterable) {
                if (site.getStatus() != SiteStatus.INDEXED) {
                    site.setLastError("Индексация прервана пользователем");
                    site.setStatus(SiteStatus.FAILED);
                    site.setStatusTime(LocalDateTime.now());
                    siteRepository.save(site);
                }
            }
            return new CustomResponse();
        } else {
            return new Error("Индексация не запущена");
        }
    }

    public CustomResponse startPage(String url) {
        pool = getPoolInstance();
        if (pool.getActiveThreadCount() > 0) {
            return new Error("Индексация уже запущена");
        } else {
            if (sites.getSites().stream().noneMatch(s -> s.getUrl().equals(url))) {
                return new Error("Данная страница находится за пределами сайтов, " +
                        "указанных в конфигурационном файле");
            }
            sites.getSites()
                    .stream()
                    .filter(s -> s.getUrl().equals(url))
                    .forEach(s -> {
                        pool.execute(InitSiteIndexator(s.getName(), s.getUrl()));
                    });
            return new CustomResponse();
        }
    }


    @Override
    public CustomResponse startIndexing() {
        logger.info("Запуск индексации");
        return startAll();
    }

    @Override
    public CustomResponse stopIndexing() {
        logger.info("Остановка индексации ");
        return stopAll();
    }

    @Override
    public CustomResponse indexPage(String url) {
        logger.info("Индексации страницы: " + url);
        return startPage(url);
    }
}
