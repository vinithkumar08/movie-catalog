package com.symphonyai.catalog.controller;

import com.symphonyai.catalog.dto.AppInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/info")
public class InfoController {

    // Read straight from application.properties
    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @GetMapping
    public AppInfoResponse getInfo() {
        return new AppInfoResponse(appName, appVersion);
    }
}
