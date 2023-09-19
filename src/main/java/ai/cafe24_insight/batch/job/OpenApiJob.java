package ai.cafe24_insight.batch.job;

import ai.cafe24_insight.batch.domain.TrendingMovie;
import ai.cafe24_insight.batch.domain.TrendingObject;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OpenApiJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // Job build 및 순서 정의
    @Bean
    public Job trendingMovieJob() {

        Job trendingMovieJob = jobBuilderFactory.get("trendingMovieJob")
                                                .start(
                                                    openApiFristStep())    // 데이터를 한번만 받으면 되기 때문에 단일 스텝으로 구성
                                                .build();

        return trendingMovieJob;
    }

    @Bean
    @JobScope    // JobParameter를 보내므로 설정
    public Step openApiFristStep() {
        return stepBuilderFactory.get("openApiFristStep")
                                 .<Mono<TrendingMovie[]>, TrendingMovie[]>chunk(
                                     1) // Input, Output, chunk 사이즈
                                 .reader(openApiReader())
                                 .processor(dataEditProcessor())
                                 .writer(dataInsertWrite())
                                 .build();
    }

    // 데이터를 읽어오는 ItemReader 인터페이스의 커스텀 구현체
    @Bean
    @StepScope
    public OpenApiReader openApiReader() {
        return new OpenApiReader();
    }

    // 읽어온 데이터를 가공 후 반환하는 ItemProcessor 인터페이스의 커스텀 구현체
    @Bean
    @StepScope
    public OpenApiProcessor dataEditProcessor() {
        return new OpenApiProcessor();
    }

    // 가공 되어진 데이터들(Chunk)를 DB 혹은 특정 파일에 작성하는 ItemWriter 인터페이스의 커스텀 구현체
    @Bean
    @StepScope
    public OpenApiWriter dataInsertWrite() {
        return new OpenApiWriter();
    }

    public class OpenApiReader implements ItemReader<Mono<TrendingMovie[]>> {

        //        @Value("${movie.openApi.trending.uri}")
        private final String TRENDING_MOVIE_URL = "https://api.themoviedb.org/3";

        //        @Value("${movie.openApi.apiKey}")
        private final String API_KEY = "a5355071c234b96166b4f48b52d2ee19";

        @Autowired
        private WebClient.Builder wcBuilder;

        private int cnt = 0;

        @Override
        public Mono<TrendingMovie[]> read()
            throws Exception {
            cnt++;
            return cnt == 1
                ? wcBuilder.build().get()
                           .uri(TRENDING_MOVIE_URL + "/trending/movie/day?api_key={API_KEY}",
                               API_KEY)
                           .accept(MediaType.APPLICATION_JSON)
                           .retrieve()
                           .bodyToMono(TrendingObject.class)
                           .map(trendingObject -> trendingObject.getResults())
                : null;
        }
    }

    // exception이 발생하였을 때 Roll Back
// 적용된 범위에서는 트랜잭션 기능이 포함된 프록시 객체가 생성되어 자동으로 commit 혹은 rollback을 진행해준다.
    @Transactional(rollbackFor = Exception.class)
    public class OpenApiProcessor implements ItemProcessor<Mono<TrendingMovie[]>, TrendingMovie[]> {

        @Override
        public TrendingMovie[] process(Mono<TrendingMovie[]> item) throws Exception {
            return item.block();
        }
    }

    public class OpenApiWriter implements ItemWriter<TrendingMovie[]> {

        @Autowired
        private MongoOperations mongoOperations;

        @Override
        public void write(List<? extends TrendingMovie[]> items) throws Exception {
            // chunk 사이즈가 1이므로 한번만 돌음
            for (int i = 0; i < items.size(); i++) {
                TrendingMovie[] movies = items.get(i);
                for (TrendingMovie movie : movies) {
                    log.info("movie : {}", movie.toString());
                    mongoOperations.save(movie);
                }
            }
        }
    }
}