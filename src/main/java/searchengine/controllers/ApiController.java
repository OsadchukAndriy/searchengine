package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.CustomResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Site;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {


    private final StatisticsService statisticsService;
    @Autowired
    private final IndexingService indexingService;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<CustomResponse> startIndexing(){
         return ResponseEntity.ok(indexingService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<CustomResponse> stopIndexing(){
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @GetMapping("/indexPage")
    public ResponseEntity<CustomResponse> StartIndexingPage(Site site){
        return ResponseEntity.ok(indexingService.indexPage(site.getUrl()));
    }
}
