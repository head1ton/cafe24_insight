package ai.cafe24_insight.batch.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrendingObject {

    private TrendingMovie[] results;

    @Builder
    public TrendingObject(TrendingMovie[] results) {
        this.results = results;
    }
}
