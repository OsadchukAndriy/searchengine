package searchengine.services;

import searchengine.dto.statistics.CustomResponse;

public interface SearchingService {
    CustomResponse search(String query, String site, int offset, int limit);
}
