package xyz.bank.customerms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Cliente HTTP muy simple para consultar cuentas por cliente.
 * Se asume que AccountMs expone GET /account-ms/v1/cuentas?clienteId=UUID
 * devolviendo 200 con lista (posible empty).
 */
@Component
public class AccountClient {

    private final RestTemplate rest;
    private final String baseUrl;

    public AccountClient(
            @Value("${accountms.base-url:}") String baseUrl,
            RestTemplateBuilder builder
    ) {
        this.baseUrl = baseUrl;
        this.rest = builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }

    @SuppressWarnings("unchecked")
    public boolean hasAccounts(UUID clienteId) {
        if (baseUrl == null || baseUrl.isBlank()) {
            // Sin integración → conservador: no borrar
            return true;
        }
        try {
            var uri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .path("/v1/cuentas")
                    .queryParam("clienteId", clienteId.toString())
                    .build(true)
                    .toUri();

            ResponseEntity<List> res =
                    rest.exchange(uri, HttpMethod.GET, null, List.class);

            return res.getStatusCode().is2xxSuccessful()
                    && res.getBody() != null
                    && !res.getBody().isEmpty();

        } catch (RestClientException ex) {
            // En error de red, evitar borrado para cumplir regla
            return true;
        }
    }
}
