package searchengine.services;

import searchengine.dto.CustomResponse;

public interface IndexingService {

    CustomResponse startIndexing();
    CustomResponse stopIndexing();
    CustomResponse indexPage(String url);
}
