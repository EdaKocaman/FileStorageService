package com.example.FileStorageService.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class RestClientService {
    private final RestClient restClient = RestClient.create();

    public String getExternalData() {
        String url = "https://jsonplaceholder.typicode.com/todos/1";
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }

    public String sendPostRequest() {
        String url = "https://jsonplaceholder.typicode.com/posts";

        Map<String, Object> requestBody = Map.of(
                "title", "Spring Boot RestClient",
                "body", "RestClient ile HTTP istekleri çok kolay!",
                "userId", 1
        );

        return restClient.post()
                .uri(url)
                .body(requestBody)
                .retrieve()
                .body(String.class);
    }

    public String getPostById(int id) {
        return restClient.get()
                .uri("https://jsonplaceholder.typicode.com/posts/{id}", id)
                .retrieve()
                .body(String.class);
    }

    public String getPostWithParams(int userId) {
        return restClient.get()
                .uri("https://jsonplaceholder.typicode.com/posts?userId={userId}", userId)
                .retrieve()
                .body(String.class);
    }

    public String getWithHeaders() {
        return restClient.get()
                .uri("https://jsonplaceholder.typicode.com/posts/1")
                .header("Authorization", "Bearer my-secret-token")
                .retrieve()
                .body(String.class);
    }
}
