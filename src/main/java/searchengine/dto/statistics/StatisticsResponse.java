package searchengine.dto.statistics;

import lombok.Data;

@Data
public class StatisticsResponse extends CustomResponse {
    private boolean result;
    private StatisticsData statistics;
}
