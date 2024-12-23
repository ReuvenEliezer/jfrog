package com.reuven.jfrog.services;

import com.reuven.jfrog.dto.GitHubResponse;
import com.reuven.jfrog.dto.ItemsResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class GitHubItemsRetrieverServiceImpl implements GitHubItemsRetrieverService {

    private static final Logger logger = LogManager.getLogger(GitHubItemsRetrieverServiceImpl.class);

    private static final int MAX_RETRY = 1;
    private static final Duration FIXED_DELAY_ON_RETRY = Duration.ofSeconds(1);
    private static final int MAX_ENTITIES_FOR_PAGE = 100;
    private final RestClient restClient;
    private final WebClient webClient;

    public GitHubItemsRetrieverServiceImpl(RestClient restClient, WebClient webClient) {
        this.restClient = restClient;
        this.webClient = webClient;
    }

    @Override
    public Mono<ItemsResponse> retrieverDataReactive(List<String> urls) {
        return Flux.fromIterable(urls)
                .flatMap(url -> {
                    UriComponentsBuilder baseUriBuilder = buildUri(url);
                    return retrieveDataReactive(baseUriBuilder);
                })
                .collectList()
                .map(items -> {
                    List<Object> flattenedItems = items.stream()
                            .flatMap(e -> e.items().stream())
                            .collect(Collectors.toList());
                    logger.info("Total items found: {} on {} pages for {} urls", flattenedItems.size(), items.size(), urls.size());
                    return new ItemsResponse(flattenedItems, flattenedItems.size());
                });
    }

    @Override
    public ItemsResponse retrieverData(List<String> urls) {
        //TODO validation
        List<Object> items = new ArrayList<>();
        for (String url : urls) {
            int page = 1;
            UriComponentsBuilder uriBuilder = buildUri(url);
            GitHubResponse response;
            do {
                String decodedUri = URLDecoder.decode(uriBuilder.toUriString(), StandardCharsets.UTF_8);
                response = restClient.get()
                        .uri(decodedUri)
                        .retrieve()
                        .body(GitHubResponse.class);
                logger.info("found {} items on page {} for url {}", response.items().size(), page, decodedUri);
                if (!response.items().isEmpty()) {
                    items.addAll(response.items());
                    uriBuilder = uriBuilder.replaceQueryParam("page", ++page);
                }
                //call until the response body is empty
            } while (hasNextPage(response));
        }
        logger.info("Total items found: {} for {} urls", items.size(), urls.size());
        return new ItemsResponse(items, items.size());
    }

    private Flux<GitHubResponse> retrieveDataReactive(UriComponentsBuilder baseUriBuilder) {
        AtomicInteger pageNum = new AtomicInteger(1);
        return retrieveDataReactive(baseUriBuilder, pageNum.get())
                .expand(response -> {
                    if (hasNextPage(response)) {
                        int nextPage = pageNum.incrementAndGet();
                        return retrieveDataReactive(baseUriBuilder, nextPage);
                    } else {
                        return Mono.empty();
                    }
                });
    }

    private static boolean hasNextPage(GitHubResponse gitHubResponse) {
        return gitHubResponse.items().size() == MAX_ENTITIES_FOR_PAGE;
    }

    private Mono<GitHubResponse> retrieveDataReactive(UriComponentsBuilder uriComponentsBuilder, int pageNum) {
        uriComponentsBuilder = uriComponentsBuilder.replaceQueryParam("page", pageNum);
        String decodedUri = URLDecoder.decode(uriComponentsBuilder.toUriString(), StandardCharsets.UTF_8);
        return webClient.get()
                .uri(decodedUri)
                .retrieve()
                .bodyToMono(GitHubResponse.class)
                .retryWhen(Retry.fixedDelay(MAX_RETRY, FIXED_DELAY_ON_RETRY))
                .doOnNext(gitHubResponse -> logger.info("found {} items on page {} for url {}", gitHubResponse.items().size(), pageNum, decodedUri))
                .onErrorResume(e -> {
                    printErrorMessage(e);
                    return Mono.empty();
                });
    }

    private static void printErrorMessage(Throwable e) {
        if (e.getCause() instanceof WebClientResponseException webClientResponseException) {
            HttpStatusCode statusCode = webClientResponseException.getStatusCode();
            if (statusCode.value() == HttpStatus.NOT_FOUND.value()) {
                logger.info("ignore from repo that not including pom.xml {}", e.getMessage());
            } else if (statusCode.value() == HttpStatus.FORBIDDEN.value()
                    || statusCode.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                logger.error("Error occurred: API rate limit exceeded for user ID  {}", e.getMessage(), e);
                /**
                 * {
                 *     "message": "API rate limit exceeded for user ID 144931323. If you reach out to GitHub Support for help, please include the request ID EF65:CBAE:132BD4B7:136A4626:6565C271.",
                 *     "documentation_url": "https://docs.github.com/rest/overview/rate-limits-for-the-rest-api"
                 * }
                 */
            } else {
                logger.error("Error occurred: {}", e.getMessage(), e);
            }
        } else {
            logger.error("Error occurred: {}", e.getMessage(), e);
        }
    }

    private static UriComponentsBuilder buildUri(String url) {
        String[] uriSplit = url.split("\\?");
        String baseUri = uriSplit[0];
        String[] querySplit = uriSplit[1].split("=");

        String queryAtt = querySplit[0];
        String queryValue = querySplit[1];

        return UriComponentsBuilder.fromUri(URI.create(baseUri))
                .queryParam(queryAtt, queryValue)
                .queryParam("page", 1)
                .queryParam("per_page", MAX_ENTITIES_FOR_PAGE);
    }


}
