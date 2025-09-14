package com.customer.customer_ms.api;

import com.customer.customer_ms.entity.ClienteEntity;
import com.customer.customer_ms.exceptions.ClienteConCuentasActivasException;
import com.customer.customer_ms.exceptions.ClienteDuplicadoException;
import com.customer.customer_ms.exceptions.ClientesNoEncontradosException;
import com.customer.customer_ms.exceptions.ValidacionException;
import com.customer.customer_ms.model.ClienteRequest;
import com.customer.customer_ms.model.ClienteResponse;
import com.customer.customer_ms.model.CuentaResponse;
import com.customer.customer_ms.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientesApiDelegateImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ClientesApiDelegateImpl delegate;

    private ClienteEntity cliente1;

    @BeforeEach
    void setUp() {
        cliente1 = new ClienteEntity();
        cliente1.setId(1L);
        cliente1.setNombre("Juan");
        cliente1.setApellido("Pérez");
        cliente1.setDni("12345678");
        cliente1.setEmail("juan.perez@example.com");
    }

    private static ClienteRequest buildRequest(
            String nombre, String apellido, String dni, String email) {
        ClienteRequest r = new ClienteRequest();
        r.setNombre(nombre);
        r.setApellido(apellido);
        r.setDni(dni);
        r.setEmail(email);
        return r;
    }

    @Nested //permite agrupar tests relacionados
    @DisplayName("clientesGet()")
    class ClientesGetTests {
        @Test
        @DisplayName("retorna lista cuando hay clientes")
        void returnsListWhenClientsExist() {
            when(clienteRepository.findAll()).thenReturn(List.of(cliente1));

            ResponseEntity<List<ClienteResponse>> resp = delegate.clientesGet();

            assertEquals(HttpStatus.OK, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals(1, resp.getBody().size());

            ClienteResponse c = resp.getBody().get(0);
            assertAll(
                    () -> assertEquals(cliente1.getId(), c.getId()),
                    () -> assertEquals(cliente1.getDni(), c.getDni()),
                    () -> assertEquals(cliente1.getNombre(), c.getNombre()),
                    () -> assertEquals(cliente1.getApellido(), c.getApellido()),
                    () -> assertEquals(cliente1.getEmail(), c.getEmail())
            );

            verify(clienteRepository).findAll();
            verifyNoMoreInteractions(clienteRepository);
        }

        @Test
        @DisplayName("lanza ClientesNoEncontradosException cuando no hay clientes")
        void throwsWhenEmpty() {
            when(clienteRepository.findAll()).thenReturn(Collections.emptyList());
            assertThrows(ClientesNoEncontradosException.class, () -> delegate.clientesGet());
        }
    }

    @Nested
    @DisplayName("clientesPost()")
    class ClientesPostTests {
        @Test
        @DisplayName("crea cliente cuando request es válido y DNI no existe")
        void createsWhenValid() {
            //Arrange
            ClienteRequest req = buildRequest("Ana", "Lopez", "99999999", "ana.lopez@example.com");
            when(clienteRepository.findByDni("99999999")).thenReturn(Optional.empty());

            ClienteEntity guardado = new ClienteEntity();
            guardado.setId(10L);
            guardado.setNombre(req.getNombre());
            guardado.setApellido(req.getApellido());
            guardado.setDni(req.getDni());
            guardado.setEmail(req.getEmail());

            when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(guardado);

            //Act
            ResponseEntity<ClienteResponse> resp = delegate.clientesPost(req);

            //Assert
            assertEquals(HttpStatus.CREATED, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals(10L, resp.getBody().getId());
            assertEquals(req.getDni(), resp.getBody().getDni());
        }

        @Test
        @DisplayName("lanza ClienteDuplicadoException si el DNI ya existe")
        void throwsOnDuplicateDni() {
            ClienteRequest req = buildRequest("Ana", "López", "12345678", "ana.lopez@example.com");
            when(clienteRepository.findByDni("12345678")).thenReturn(Optional.of(cliente1));
            assertThrows(ClienteDuplicadoException.class, () -> delegate.clientesPost(req));
        }

        @Test
        @DisplayName("lanza ValidacionException si faltan campos obligatorios")
        void throwsOnInvalidRequest() {
            // nombre vacío
            ClienteRequest req = buildRequest("  ", "López", "11111111", "ana@example.com");
            assertThrows(ValidacionException.class, () -> delegate.clientesPost(req));
        }
    }

    @Nested
    @DisplayName("clientesIdGet()")
    class ClientesIdGetTests {
        @Test
        @DisplayName("retorna 200 cuando encuentra el cliente")
        void returnsOkWhenFound() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente1));
            ResponseEntity<ClienteResponse> resp = delegate.clientesIdGet(1L);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("12345678", resp.getBody().getDni());
        }

        @Test
        @DisplayName("lanza ClientesNoEncontradosException si no existe")
        void throwsWhenNotFound() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ClientesNoEncontradosException.class, () -> delegate.clientesIdGet(99L));
        }
    }

    @Nested
    @DisplayName("clientesIdDelete()")
    class ClientesIdDeleteTests {
        @Test
        @DisplayName("borra cuando no tiene cuentas activas")
        void deletesWhenNoActiveAccounts() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente1));

            // Respuesta del ms de cuentas: lista vacía
            ResponseEntity<List<CuentaResponse>> cuentasResp =
                    new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);

            when(restTemplate.exchange(
                    eq("http://localhost:8081/cuentas/cliente/{clienteId}"),
                    eq(HttpMethod.GET),
                    isNull(),
                    ArgumentMatchers.<ParameterizedTypeReference<List<CuentaResponse>>>any(),
                    eq(1L)
            )).thenReturn(cuentasResp);

            ResponseEntity<Void> resp = delegate.clientesIdDelete(1L);
            assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
            verify(clienteRepository).deleteById(1L);
        }

        @Test
        @DisplayName("lanza ClienteConCuentasActivasException si tiene cuentas")
        void throwsWhenHasActiveAccounts() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente1));

            // Con un solo objeto ya es 'no vacía'
            List<CuentaResponse> cuentas = List.of(new CuentaResponse());
            ResponseEntity<List<CuentaResponse>> cuentasResp =
                    new ResponseEntity<>(cuentas, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq("http://localhost:8081/cuentas/cliente/{clienteId}"),
                    eq(HttpMethod.GET),
                    isNull(),
                    ArgumentMatchers.<ParameterizedTypeReference<List<CuentaResponse>>>any(),
                    eq(1L)
            )).thenReturn(cuentasResp);

            assertThrows(ClienteConCuentasActivasException.class, () -> delegate.clientesIdDelete(1L));
            verify(clienteRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("lanza ClientesNoEncontradosException si el cliente no existe")
        void deleteThrowsWhenClientNotFound() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(ClientesNoEncontradosException.class, () -> delegate.clientesIdDelete(1L));
        }
    }

    @Nested
    @DisplayName("clientesIdPut()")
    class ClientesIdPutTests {
        @Test
        @DisplayName("actualiza datos cuando existe y el DNI no cambia")
        void updatesWhenExistsAndSameDni() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente1));

            ClienteRequest req = buildRequest("Juana", "Pérez", "12345678", "juana@example.com");

            ClienteEntity actualizado = new ClienteEntity();
            actualizado.setId(1L);
            actualizado.setNombre("Juana");
            actualizado.setApellido("Pérez");
            actualizado.setDni("12345678");
            actualizado.setEmail("juana@example.com");

            when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(actualizado);

            ResponseEntity<ClienteResponse> resp = delegate.clientesIdPut(1L, req);
            assertEquals(HttpStatus.OK, resp.getStatusCode());
            assertNotNull(resp.getBody());
            assertEquals("Juana", resp.getBody().getNombre());
            assertEquals("12345678", resp.getBody().getDni());
        }

        @Test
        @DisplayName("lanza ClienteDuplicadoException si se intenta cambiar a un DNI ya existente")
        void putThrowsOnDuplicateDniChange() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente1));

            // Se cambia a '2222...' y ya existe otro con ese DNI
            ClienteRequest req = buildRequest("Juan", "Pérez", "22222222", "juan2@example.com");
            when(clienteRepository.findByDni("22222222")).thenReturn(Optional.of(new ClienteEntity()));

            assertThrows(ClienteDuplicadoException.class, () -> delegate.clientesIdPut(1L, req));
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza ClientesNoEncontradosException si no existe el cliente")
        void putThrowsWhenNotFound() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
            ClienteRequest req = buildRequest("X", "Y", "11111111", "x@y.com");
            assertThrows(ClientesNoEncontradosException.class, () -> delegate.clientesIdPut(99L, req));
        }

        @Test
        @DisplayName("lanza ValidacionException si request inválido")
        void putThrowsOnInvalidRequest() {
            // La validación falla antes de acceder a dependencias
            ClienteRequest req = buildRequest("", "Pérez", "12345678", "mail@x.com"); // nombre vacío

            assertThrows(ValidacionException.class, () -> delegate.clientesIdPut(1L, req));

            // Asegura que no se interactuó con colaboraciones
            verifyNoInteractions(clienteRepository, restTemplate);
        }
    }
}