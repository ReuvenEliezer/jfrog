package com.reuven.jfrog.services;

import com.reuven.jfrog.dto.ItemsResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GitHubItemsRetrieverService {

    Mono<ItemsResponse> retrieverDataReactive(List<String> urls);

    ItemsResponse retrieverData(List<String> urls);

}
