package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class DelayController {

    @GetMapping("/delay/{sec}")
    public String delay(@PathVariable String sec) throws InterruptedException{
        log.info("      DelayController::delay started with parameter: " + sec);

        Thread.sleep(Integer.parseInt(sec) * 1000);

        log.info("      DelayController::delay ended with parameter: " + sec);

        return "        Result of DelayController::delay with parameter " + sec;
    }

    @GetMapping("/delay2/{sec}")
    public Map<String,String> delay2(@PathVariable String sec) throws InterruptedException{
        log.info("      DelayController::delay2 started with parameter: " + sec);

        Thread.sleep(Integer.parseInt(sec) * 1000);

        log.info("      DelayController::delay2 ended with parameter: " + sec);

        HashMap<String, String> map = new HashMap<>();
        map.put("value",sec.toString());

        return map;
    }
}
