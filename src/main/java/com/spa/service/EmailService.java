package com.spa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.api.url:https://api.brevo.com/v3/smtp/email}")
    private String apiUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public void enviarcorreo(String para, String asunto, String mensaje) {

        try {
            String jsonBody = """
                {
                  "sender": {
                    "name": "H&B SPA",
                    "email": "hbspa.contacto@gmail.com"
                  },
                  "to": [
                    { "email": "%s" }
                  ],
                  "subject": "%s",
                  "htmlContent": "<p>%s</p>"
                }
            """.formatted(para, asunto, mensaje);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("api-key", apiKey)
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Respuesta de Brevo:");
            System.out.println("Status: " + response.statusCode());
            System.out.println(response.body());

        } catch (Exception e) {
            throw new RuntimeException("Error enviando correo: " + e.getMessage(), e);
        }
    }

    public void enviarCodigoReset(String para, String codigo, Duration ttl) {
        String asunto = "Código de verificación";
        String mensaje = """
                Tu código para restablecer la contraseña es: %s <br><br>
                Este código caduca en %d minutos.<br><br>
                Si no lo solicitaste, ignora este correo.
                """.formatted(codigo, ttl.toMinutes());

        enviarcorreo(para, asunto, mensaje);
    }
}
