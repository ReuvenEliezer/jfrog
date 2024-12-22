package com.reuven.jfrog.dto;

import java.util.List;

public record ItemsResponse(List<Object> items, long sumTotalCount) {

}
