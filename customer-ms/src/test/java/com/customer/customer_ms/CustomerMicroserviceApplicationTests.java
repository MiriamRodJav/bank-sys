package com.customer.customer_ms;

import com.account.client.ApiClient;
import com.account.client.api.CuentasApi;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class CustomerMicroserviceApplicationTests {

    @MockBean private CuentasApi cuentasApi;
    @MockBean private ApiClient apiClient;

    @Test
    void contextLoads() {
        // Pasa si el contexto arranca.
    }
}
