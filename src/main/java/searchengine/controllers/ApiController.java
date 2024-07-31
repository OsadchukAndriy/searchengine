package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.CustomResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Site;
import searchengine.services.interfaces.IndexingService;
import searchengine.services.interfaces.SearchingService;
import searchengine.services.interfaces.StatisticsService;

import java.util.logging.Logger;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {


    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchingService searchingService;

    Logger logger = Logger.getLogger(ApiController.class.getName());


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        logger.info("statistics - работает");
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<CustomResponse> startIndexing() {
        logger.info("startIndexing - работает");
        return ResponseEntity.ok(indexingService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<CustomResponse> stopIndexing() {
        logger.info("stopIndexing - работает");
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<CustomResponse> startIndexingPage(Site site) {
        logger.info("indexPage - работает");
        return ResponseEntity.ok(indexingService.indexPage(site.getUrl()));
    }

    @GetMapping("/search")
    public ResponseEntity<CustomResponse> startSearching(@RequestParam String query,
                                                         @RequestParam(required = false) String site,
                                                         @RequestParam int offset,
                                                         @RequestParam int limit) {
        logger.info("search - работает");
        return ResponseEntity.ok(searchingService.search(query, site, offset, limit));
    }
}
