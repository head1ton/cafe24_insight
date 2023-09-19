package ai.cafe24_insight.batch.domain;

import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "TrendingMovie")    // MongoDb에 저장되어 있는 collection을 정의해줍니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)    // 빈 생성자가 만들어지는 것을 방지해줍니다.
public class TrendingMovie {

    @Id
    private int id;
    private boolean adult;
    private String overview;
    private int[] genre_ids;
    private String title;
    private int vote_count;
    private int vote_average;

    @Builder
    public TrendingMovie(int id, boolean adult, String overview, int[] genre_ids, String title,
        int vote_count,
        int vote_average) {
        this.id = id;
        this.adult = adult;
        this.overview = overview;
        this.genre_ids = genre_ids;
        this.title = title;
        this.vote_count = vote_count;
        this.vote_average = vote_average;
    }

}
