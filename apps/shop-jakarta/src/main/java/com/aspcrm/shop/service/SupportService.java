package com.aspcrm.shop.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class SupportService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String apiBase = System.getenv().getOrDefault("CRM_API_URL", "http://crm:8080/api/shop/tickets");

    public boolean submitTicket(String email, String firstName, String lastName, String phone, String title, String description, String priority) {
        try {
            String json = String.format(
                    "{\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"phone\":\"%s\",\"title\":\"%s\",\"description\":\"%s\",\"priority\":\"%s\"}",
                    escape(email), escape(firstName), escape(lastName), escape(phone), escape(title), escape(description), escape(priority));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiBase))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception ex) {
            return false;
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
