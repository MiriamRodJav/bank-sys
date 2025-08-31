package xyz.bank.accountms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.UUID;

/**
 * Verifica que exista el cliente en CustomerMs: GET /customer-ms/v1/clientes/{id}
 */
@Component
public class CustomerClient {

    private final RestTemplate rest;
    private final String baseUrl;

    // Boot 2.7: inject RestTemplateBuilder, configure timeouts
    public CustomerClient(
            @Value("${customerms.base-url:}") String baseUrl,
            RestTemplateBuilder builder
    ) {
        this.baseUrl = baseUrl;
        this.rest = builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }

    public boolean customerExists(UUID clienteId) {
        if (baseUrl == null || baseUrl.isBlank()) {
            // Sin integración configurada → conservador: no permitir crear cuenta
            return false;
        }
        try {
            var uri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)               // e.g. http://host:port/customer-ms
                    .pathSegment("v1", "clientes", clienteId.toString())
                    .build(true)
                    .toUri();

            ResponseEntity<Void> res = rest.exchange(uri, HttpMethod.GET, null, Void.class);
            return res.getStatusCode().is2xxSuccessful();

        } catch (HttpClientErrorException.NotFound e) {
            // 404 = no existe el cliente
            return false;
        } catch (RestClientException e) {
            // timeouts, 5xx, DNS, etc. → falla cerrada
            return false;
        }
    }
}
