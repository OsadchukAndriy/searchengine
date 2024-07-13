package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.CustomResponse;

@Service
@RequiredArgsConstructor
public class SearchingServiceImpl implements SearchingService {

    @Override
    public CustomResponse search(String query, String site, int offset, int limit) {
        return null;
    }
}
