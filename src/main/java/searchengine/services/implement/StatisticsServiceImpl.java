package searchengine.services.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.repository.SiteRepository;
import searchengine.services.interfaces.StatisticsService;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;

    @Override
    public StatisticsResponse getStatistics() {
        Iterable<Site> siteIterable = siteRepository.findAll();

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        TotalStatistics total = new TotalStatistics();
        total.setIndexing(true);

        for (Site site : siteIterable) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            item.setError(site.getLastError());
            item.setStatusTime(site.getStatusTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            item.setPages(site.getPages().size());
            item.setLemmas(site.getLemmas().size());
            item.setStatus(String.valueOf(site.getStatus()));
            total.setSites(total.getSites() + 1);
            total.setPages(total.getPages() + item.getPages());
            total.setLemmas(total.getLemmas() + item.getLemmas());
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
