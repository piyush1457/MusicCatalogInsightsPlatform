package com.musiccatalog.dto;

import java.time.Instant;

public class ErrorResponse {

    private int status;
    private String message;
    private Instant timestamp;
    private String path;

    public ErrorResponse(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.timestamp = Instant.now();
        this.path = path;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public String getPath() { return path; }
}
