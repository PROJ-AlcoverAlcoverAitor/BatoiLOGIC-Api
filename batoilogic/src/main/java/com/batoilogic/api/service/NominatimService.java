package com.batoilogic.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class NominatimService {

    private static final String URL =
            "https://nominatim.openstreetmap.org/search";

    public double[] obtenerCoords(String direccion) {

        try {

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();

            headers.set("User-Agent", "FlutterApp_2026/1.0");
            headers.set("Accept", "application/json");

            String url = UriComponentsBuilder
                    .fromHttpUrl(URL)
                    .queryParam("q", direccion)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build()
                    .toUriString();

            System.out.println("Geocodificando URL: " + url);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

            System.out.println("STATUS: " + response.getStatusCode());
            System.out.println("BODY: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null) {

                ObjectMapper mapper = new ObjectMapper();

                JsonNode root =
                        mapper.readTree(response.getBody());

                if (root.isArray() && root.size() > 0) {

                    JsonNode first = root.get(0);

                    double lat = first.get("lat").asDouble();
                    double lng = first.get("lon").asDouble();

                    return new double[]{lat, lng};
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return new double[]{0.0, 0.0};
    }
}