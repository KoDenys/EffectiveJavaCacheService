package com.epam.ld.cache_service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

public class CacheListener {
    private String fileName = "default_log_file.txt";

    public CacheListener() {
    }

    public CacheListener(String fileName){
        this.fileName = fileName;
    }

    public void writeLog(String message) {
        StringBuilder logBuilder = new StringBuilder();
        StringBuilder messageBuilder = new StringBuilder();
        List<String> previousInfo = null;
        try {
            previousInfo = Files.readAllLines(Paths.get("src/test/resources/" + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String line : previousInfo){
            logBuilder.append(line)
                    .append("\n");
        }

        messageBuilder.append(LocalDateTime.now())
                .append(" [INFO] ")
                .append(message);

        logBuilder.append(messageBuilder);

        //Write log to console
        System.out.println(messageBuilder);

        try {
            Files.writeString(Paths.get("src/test/resources/" + fileName), logBuilder.toString(), StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
