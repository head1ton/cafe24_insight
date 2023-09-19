package ai.cafe24_insight;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableBatchProcessing
@SpringBootApplication
public class Cafe24InsightApplication {

    public static void main(String[] args) {
        SpringApplication.run(Cafe24InsightApplication.class, args);
    }

}
