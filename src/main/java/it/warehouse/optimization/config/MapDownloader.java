package it.warehouse.optimization.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.runtime.Startup;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Duration;


@ApplicationScoped
@Startup
@Slf4j

public class MapDownloader {

    @ConfigProperty(name = "warehouse.map.auto-download", defaultValue = "true")
    boolean autoDownloadEnabled;

    @ConfigProperty(name = "warehouse.map.file-path", defaultValue = "data")
    String mapDir;

    @ConfigProperty(name = "warehouse.map.file-name", defaultValue = "italy-latest.osm.pbf")
    String mapFileName;

    @ConfigProperty(name = "warehouse.map.country", defaultValue = "italy")
    String country;

    @ConfigProperty(name = "warehouse.map.continent", defaultValue = "europe")
    String continent;

    @ConfigProperty(name = "warehouse.map.connection-timeout", defaultValue = "2")
    Integer connectionTimeout;


    private  String osmUrl;
    private Path filePath;
    private static final int CHUNK_SIZE = 16 * 1024;


    @PostConstruct
    public void init() {

        osmUrl =  String.format("https://download.geofabrik.de/%s/%s-latest.osm.pbf",continent,country );
        filePath = Paths.get(mapDir, mapFileName);

        if(checkFileExist()) return;

        if(!autoDownloadEnabled){
            log.warn("WARNING: Map auto download is disabled, make sure the file is downloaded!");
            return;
        }

        try {
            downloadIfNotExists();
        } catch (IOException e) {
            log.error("MapDownloader - init: Error while download map file. Error message: {}",e.getMessage());
            throw new RuntimeException("Error while download map file." + e.getMessage());
        } catch (InterruptedException e) {
            log.error("MapDownloader - init: Download interrupted", e);
            throw new RuntimeException("Download interrupted", e);
        }
    }

    private boolean checkFileExist(){
        if (Files.exists(filePath)) {
            log.info("Map file already downloaded at: {}", filePath);
            return true;
        }
        return false;
    }

    private void downloadIfNotExists() throws IOException, InterruptedException {
        Files.createDirectories(filePath.getParent());

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMinutes(connectionTimeout))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(osmUrl))
                .build();
        downloadWithProgress(client,request);
    }

    private void downloadWithProgress(HttpClient client, HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to download map file. HTTP status code: " + response.statusCode());
        }

        long contentLength = response.headers()
                .firstValueAsLong("Content-Length")
                .orElse(-1);

        log.info("Starting download of map file...");
        try (InputStream in = response.body();
             var out = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            byte[] buffer = new byte[CHUNK_SIZE];
            long downloaded = 0;
            int lastLoggedPercent = 0;
            int read;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                downloaded += read;

                if (contentLength > 0) {
                    int percent = (int) (downloaded * 100 / contentLength);
                    if (percent / 10 > lastLoggedPercent / 10) {
                        lastLoggedPercent = percent;
                        log.info("Download progress: {}%", percent);
                    }
                }
            }
        }

        log.info("Download complete: {}", filePath.toAbsolutePath());
    }



}
