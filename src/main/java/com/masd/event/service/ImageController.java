package com.masd.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masd.event.analysis.TensorflowAnalyzer;
import com.masd.event.analysis.TensorflowResult;
import com.masd.event.db.ExecutionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.Set;

@Slf4j
@RestController
public class ImageController {

    @Autowired
    private TensorflowAnalyzer analyzer;

    @Autowired
    private ExecutionMapper mapper;

    private final ObjectMapper jsonMapper = new ObjectMapper();

    @GetMapping(path = "/analyze", produces = "application/json")
    public ResponseEntity<String> analyze(@RequestParam("image") String url) {
        log.info("Analyzing image: {}", url);

        Set<String> previousExecution = mapper.selectPreviousExecution((long) url.hashCode());
        if (previousExecution.isEmpty()) {
            log.info("No previous score found");
            try {
                TensorflowResult result = analyzer.analyze(url);
                mapper.insertExecution((long) url.hashCode(), Date.from(Instant.now()), result.getScore(), result.getLabel());

                log.info("Saved result to cache");

                return ResponseEntity.ok(jsonMapper.writeValueAsString(result));
            } catch (IOException e) {
                return ResponseEntity.status(500).body(e.getMessage());
            }
        }

        log.info("Previous score found");
        if (previousExecution.size() > 1) {
            return ResponseEntity.status(500).body("Too many executions");
        }


        TensorflowResult exisitingResult = new TensorflowResult(previousExecution.stream().findFirst().get(), 0f);

        try {
            return ResponseEntity.ok(jsonMapper.writeValueAsString(exisitingResult));
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(500).body("Could not create existing response");
        }
    }


}
