package com.reuven.jfrog.controllers;

import com.reuven.jfrog.dto.ItemsResponse;

import com.reuven.jfrog.services.GitHubItemsRetrieverService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/")
public class GitHubRetrieverController {

    private static final Logger logger = LogManager.getLogger(GitHubRetrieverController.class);

    private final GitHubItemsRetrieverService gitHubItemsRetrieverService;

    public GitHubRetrieverController(GitHubItemsRetrieverService gitHubItemsRetrieverService) {
        this.gitHubItemsRetrieverService = gitHubItemsRetrieverService;
    }

    @PostMapping("/retriever-items-reactive")
    public Mono<ItemsResponse> retrieverItemsReactive(@RequestBody List<String> urls) {
        return gitHubItemsRetrieverService.retrieverDataReactive(urls);
    }

    @PostMapping("/retriever-items")
    public ItemsResponse retrieverItems(@RequestBody List<String> urls) {
        return gitHubItemsRetrieverService.retrieverData(urls);
    }

}
