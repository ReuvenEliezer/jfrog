package com.reuven.jfrog.dto;

import java.util.List;

public record GitHubResponse(int total_count,
                             boolean incomplete_results,
                             List<Object> items) {
}
