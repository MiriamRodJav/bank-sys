package com.transaction_ms.transaction_ms.client;
import com.transaction_ms.transaction_ms.exception.TransactionException;
import com.transaction_ms.transaction_ms.model.DepositoRequest;
import com.transaction_ms.transaction_ms.model.RetiroRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import org.springframework.http.HttpStatus;

@Component

public class AccountsClientImpl implements AccountsClient {
    private final RestTemplate restTemplate;
    private final String accountsMicroserviceUrl;
    public AccountsClientImpl(
            RestTemplate restTemplate,
            @Value("${microservices.accounts.url}")
            String accountsMicroserviceUrl) {

        this.restTemplate = restTemplate;

        this.accountsMicroserviceUrl = accountsMicroserviceUrl;
    }
    @Override
    public void deposit(DepositoRequest request) {

        String url = accountsMicroserviceUrl + "/cuentas/{cuentaId}/depositar";

        Map<String, Double> requestBody = Map.of("monto", request.getMonto());

        try {
            restTemplate.put(
                    url,
                    requestBody,
                    request.getCuentaId()
            );

        } catch (HttpClientErrorException ex) {

            throw new TransactionException(
                    "Error en MS Cuentas",
                    "" + ex.getStatusCode().value(),
                    ex.getStatusCode()
            );

        } catch (RestClientException ex) {

            throw new TransactionException(
                    "Error con Microservicio de Cuentas.",
                    "503",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }


    @Override
    public void withdraw(RetiroRequest request) {

        String url = accountsMicroserviceUrl + "/cuentas/{cuentaId}/retirar";
        Map<String, Double> requestBody = Map.of("monto", request.getMonto());

        try {
            restTemplate.put(
                    url,
                    requestBody,
                    request.getCuentaId()
            );

        } catch (HttpClientErrorException ex) {

            throw new TransactionException(
                    "Error de MS Cuentas",
                    "" + ex.getStatusCode().value(),
                    ex.getStatusCode()
            );

        } catch (RestClientException ex) {

            throw new TransactionException(
                    "Error de MS Cuentas",
                    "ACC_CONN_503",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

}
