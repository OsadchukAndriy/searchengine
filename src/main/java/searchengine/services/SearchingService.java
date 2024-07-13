package searchengine.services;

import searchengine.dto.CustomResponse;

public interface SearchingService {
    CustomResponse search(String query, String site, int offset, int limit);
}
