package com.reuven.jfrog.services;

import com.reuven.jfrog.dto.ItemsResponse;

import java.util.List;

public interface GitHubItemsRetrieverService {

    ItemsResponse RetrieverData(List<String> urls);
}
