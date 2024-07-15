package searchengine.dto.statistics;

import lombok.Data;

import java.util.List;

@Data
public class SearchingResponse  extends CustomResponse {
    private int count;
    private List<SearchingData> data;
}
