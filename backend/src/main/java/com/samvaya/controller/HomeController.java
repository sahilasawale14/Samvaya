package com.samvaya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping(value = {"/", "/api"})
    public ResponseEntity<Map<String, Object>> getApiStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("service", "SAMVAYA Housing Society Management API");
        status.put("status", "ONLINE");
        status.put("database", "MySQL Connected");
        status.put("message", "The backend REST API is running. Access the user interface via your Vercel or Netlify link.");
        status.put("version", "1.0.0");
        return ResponseEntity.ok(status);
    }
}
