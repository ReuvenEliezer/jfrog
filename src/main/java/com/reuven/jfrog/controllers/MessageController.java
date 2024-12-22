package com.reuven.jfrog.controllers;

import com.reuven.jfrog.dto.ItemsResponse;

import com.reuven.jfrog.services.GitHubItemsRetrieverService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class MessageController {

    private static final Logger logger = LogManager.getLogger(MessageController.class);
    private final GitHubItemsRetrieverService gitHubItemsRetrieverService;

    public MessageController(GitHubItemsRetrieverService gitHubItemsRetrieverService) {
        this.gitHubItemsRetrieverService = gitHubItemsRetrieverService;
    }

    @PostMapping("/collect-items")
    public ItemsResponse collectItems(@RequestBody List<String> urls) {
        return gitHubItemsRetrieverService.RetrieverData(urls);
    }

}
