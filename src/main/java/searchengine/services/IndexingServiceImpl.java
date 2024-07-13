package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.CustomResponse;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    private final SitesList sitesList;
    private ForkJoinPool pool;

    // Singleton
    ForkJoinPool getPoolInstance() {
        if (pool == null || pool.isShutdown()) {
            pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        }
        return pool;
    }




    @Override
    public CustomResponse startIndexing() {
        return null;
    }

    @Override
    public CustomResponse stopIndexing() {
        return null;
    }

    @Override
    public CustomResponse indexPage(String url) {
        return null;
    }
}
