package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@Slf4j
public class JsonTest {

    private RestTemplateBuilder restTemplateBuilder;
    private final WebClient webClient;

    public JsonTest(@Autowired RestTemplateBuilder restTemplateBuilder, @LocalServerPort int randomServerPort) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.webClient = WebClient.builder().baseUrl("http://localhost:" + randomServerPort + "/delay2/").build();
        this.randomServerPort = randomServerPort;
        this.objectMapper = new ObjectMapper();
    }

    private int randomServerPort;
    private final ObjectMapper objectMapper;



    private String getBaseUri(){
        return "http://localhost:" + randomServerPort + "/delay2/";
    }





    @Test
    @DisplayName("while문을 반복하며 event를 listen하지 않고, Flux merge를 통해 받아보기")
    public void nonBlockingTestWithMerge() throws JsonProcessingException {

        log.info("Starting nonBlockingTestWithMerge...");

        log.info("Zip Start");
        Mono<Tuple2<String, String>> zip = Mono.zip(this.call1(), this.call2());
        log.info("Zip Finish");

        log.info("Block start");
        Tuple2<String, String> block = zip.block();
        log.info("Block Finish");

        JsonNode jsonNode1 = objectMapper.readTree(block.getT1());
        JsonNode jsonNode2 = objectMapper.readTree(block.getT2());

        log.info(jsonNode1.get("value").textValue());
        log.info(jsonNode2.get("value").textValue());

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
