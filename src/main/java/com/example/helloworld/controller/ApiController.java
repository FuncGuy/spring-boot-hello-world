package com.example.helloworld.controller;

import com.example.helloworld.service.RateLimiterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@RestController
public class ApiController {

    private final RateLimiterService rateLimiterService;

    public ApiController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/api")
    public String apiEndpoint(@RequestParam String userId) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (rateLimiterService.isRateLimited(userId)) {
            return "Rate limit exceeded. Try again later.";
        }
        return "Request successful.";
    }

    @GetMapping("/evictKey")
    public String remove(@RequestParam String userId) {
        rateLimiterService.removeKeyFromCache(userId);
        return "Key removed from cache.";
    }
}

