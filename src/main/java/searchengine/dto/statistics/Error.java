package searchengine.dto.statistics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Error extends CustomResponse {
    String error;

    public Error(String error) {
        this.setResult(false);
        this.error = error;
    }
}
