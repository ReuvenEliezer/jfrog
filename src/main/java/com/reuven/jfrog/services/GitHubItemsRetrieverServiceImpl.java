package com.reuven.jfrog.services;

import com.reuven.jfrog.dto.GitHubResponse;
import com.reuven.jfrog.dto.ItemsResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class GitHubItemsRetrieverServiceImpl implements GitHubItemsRetrieverService {


    private final RestClient restClient;

    public GitHubItemsRetrieverServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public ItemsResponse RetrieverData(List<String> urls) {
        //TODO make urls record
        long totalItems = 0;
        List<Object> items = new ArrayList<>();
        for (String url : urls) {
            //TODO validation
            for (long i = 1; i < Long.MAX_VALUE; i++) {
                String[] split = url.split("\\?");
                String[] query = split[1].split("=");
//            https://api.github.com/search/issues?q=cache in:file repo:scala/scala&page=2&per_page=100
                String eqDelimiter = query[0];
                String s = query[1];
//            https://api.github.com/search/issues?q=while in:file repo:openjdk/jdk
                String uriString = UriComponentsBuilder.fromHttpUrl(split[0])
                        .queryParam(eqDelimiter, s)
                        .queryParam("page", i)
                        .queryParam("per_page", 100)//TODO final
                        .toUriString();
                String finalUri = uriString.replaceAll("%20", " ");
                GitHubResponse response = restClient.get()
                        .uri(finalUri)
                        .retrieve()
                        .body(GitHubResponse.class);
                if (response != null) {
                    items.add(response.items());
                    totalItems += response.total_count();
                }
                int i1 = 0;
                //call until the response body is empty
            }
        }

        return new ItemsResponse(items, totalItems);
    }
}
