package com.customer.customer_ms;

import com.account.client.ApiClient;
import com.account.client.api.CuentasApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ApiClient accountApiClient(@Value("${account.api.base-url}") String baseUrl) {
        ApiClient client = new ApiClient();
        client.setBasePath(baseUrl); // ej: http://localhost:8081
        return client;
    }

    @Bean
    public CuentasApi cuentasApi(ApiClient accountApiClient) {
        return new CuentasApi(accountApiClient);
    }
}
