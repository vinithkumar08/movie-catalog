package com.symphonyai.catalog.dto;

public class AppInfoResponse {

    private String name;
    private String version;

    public AppInfoResponse(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() { return name; }
    public String getVersion() { return version; }
}
