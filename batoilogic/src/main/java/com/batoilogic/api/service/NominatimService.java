package com.batoilogic.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class NominatimService {

    private static final String URL = "https://nominatim.openstreetmap.org/search";

    public double[] obtenerCoords(String direccion) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "FlutterApp_2026/1.0");

            String url = URL + "?q=" + java.net.URLEncoder.encode(direccion, "UTF-8")
                    + "&format=json&limit=1";

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    JsonNode first = root.get(0);
                    double lat = first.get("lat").asDouble();
                    double lng = first.get("lon").asDouble();
                    return new double[]{lat, lng};
                }
            }
        } catch (Exception e) {
            System.out.println("Error geocodificando: " + e.getMessage());
        }
        return new double[]{0.0, 0.0};
    }
}
