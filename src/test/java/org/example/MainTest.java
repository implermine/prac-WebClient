package org.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.concurrent.atomic.AtomicReference;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@Slf4j
public class MainTest {

    private  RestTemplateBuilder restTemplateBuilder;
    private final WebClient webClient;

    public MainTest(@Autowired RestTemplateBuilder restTemplateBuilder, @LocalServerPort int randomServerPort) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.webClient = WebClient.builder().baseUrl("http://localhost:" + randomServerPort + "/delay/").build();
        this.randomServerPort = randomServerPort;
    }

    private int randomServerPort;


    private String getBaseUri(){
        return "http://localhost:" + randomServerPort + "/delay/";
    }

    @Test
    @DisplayName("restTemplate을 이용한 blocking 테스트")
    public void blockingTest(){
        RestTemplate restTemplate = restTemplateBuilder.build();

        log.info("Starting blockingTest...");
        log.info("Call api...");
        ResponseEntity<String> exchanged = restTemplate.exchange(this.getBaseUri() + "3", HttpMethod.GET, null, String.class);
        log.info("Call api finished...");
        log.info("result string: " + exchanged.getBody());
    }

    @Test
    @DisplayName("webClient를 이용한 non-blocking 테스트")
    public void nonBlockingTest() throws InterruptedException {

        log.info("Starting nonBlockingTest...");

        String par1 = "3";
        log.info("Call api with parameter: " + par1);
        Mono<String> stringMono = webClient
                .get()
                .uri(this.getBaseUri() + par1)
                .retrieve()
                .bodyToMono(String.class);
        Thread.sleep(1000); // --> 여까진 call 안함 declarative 함.
        log.info("------------");

        AtomicReference<Boolean> flag1 = new AtomicReference<>(Boolean.FALSE);
        stringMono.subscribe(result -> {
            log.info("result arrived for parameter:" + par1);

            log.info(result);
            flag1.set(Boolean.TRUE);
        });


        while(!flag1.get()){
            Thread.sleep(1000);
        }

        log.info("Finish nonBlockingTest...");

    }

    @Test
    @DisplayName("while문을 반복하며 event를 listen하지 않고, Flux merge를 통해 받아보기")
    public void nonBlockingTestWithMerge(){

        log.info("Starting nonBlockingTestWithMerge...");

        log.info("Zip Start");
        Mono<Tuple2<String, String>> zip = Mono.zip(this.call1(), this.call2());
        log.info("Zip Finish");

        log.info("Block start");
        Tuple2<String, String> block = zip.block();
        log.info("Block Finish");

        log.info(block.getT1());
        log.info(block.getT2());

        log.info("End of nonBlockingTestWithMerge...");
    }

    private Mono<String> call1(){
        log.info("Start call 1");
        return webClient.get().uri(uriBuilder -> uriBuilder.path("2").build()).retrieve().bodyToMono(String.class);
    }
    private Mono<String> call2(){
        log.info("Start call 2");
        return webClient.get().uri(uriBuilder -> uriBuilder.path("3").build()).retrieve().bodyToMono(String.class);
    }


}
