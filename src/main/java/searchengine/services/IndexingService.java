package searchengine.services;

import searchengine.dto.statistics.CustomResponse;

public interface IndexingService {

    CustomResponse startIndexing();
    CustomResponse stopIndexing();
    CustomResponse indexPage(String url);
}
