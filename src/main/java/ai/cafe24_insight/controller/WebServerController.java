package ai.cafe24_insight.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class WebServerController {

    @GetMapping("/test")
    public Mono<String> doTest() {
        WebClient webClient = WebClient.builder().build();
        return webClient.get()
                        .uri("https://shoppingtip.cafe24api.com/api/v2/admin/store?shop_no=1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer s1*yhtGCh&BRTrH*2aI*EGNNjB")
                        .retrieve()
                        .bodyToMono(String.class);
    }
}
