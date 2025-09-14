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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientesApiDelegateImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ClientesApiDelegateImpl delegate;

    // ---------- Helpers ----------
    private static ClienteRequest buildRequest(String nombre, String apellido, String dni, String email) {
        ClienteRequest r = new ClienteRequest();
        r.setNombre(nombre);
        r.setApellido(apellido);
        r.setDni(dni);
        r.setEmail(email);
        return r;
    }

    private static ClienteEntity buildEntity(Long id, String nombre, String apellido, String dni, String email) {
        ClienteEntity c = new ClienteEntity();
        c.setId(id);
        c.setNombre(nombre);
        c.setApellido(apellido);
        c.setDni(dni);
        c.setEmail(email);
        return c;
    }

    // ---------- GET /clientes ----------
    @Test
    void clientesGet_listaVacia_lanzaClientesNoEncontradosException() {
        when(clienteRepository.findAll()).thenReturn(Collections.emptyList());
        assertThrows(ClientesNoEncontradosException.class, () -> delegate.clientesGet());
        verify(clienteRepository).findAll();
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void clientesGet_ok_retornaListaMapeada() {
        var e = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findAll()).thenReturn(List.of(e));

        ResponseEntity<List<ClienteResponse>> resp = delegate.clientesGet();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(1, resp.getBody().size());
        ClienteResponse c = resp.getBody().get(0);
        assertAll(
                () -> assertEquals(1L, c.getId()),
                () -> assertEquals("12345678", c.getDni()),
                () -> assertEquals("Juan", c.getNombre()),
                () -> assertEquals("Pérez", c.getApellido()),
                () -> assertEquals("juan@x.com", c.getEmail())
        );
        verify(clienteRepository).findAll();
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void clientesGet_ok_varios_retornaListaCompleta() {
        var e1 = buildEntity(1L, "Ana", "López", "11111111", "ana@x.com");
        var e2 = buildEntity(2L, "Luis", "Gómez", "22222222", "luis@x.com");
        when(clienteRepository.findAll()).thenReturn(List.of(e1, e2));

        var resp = delegate.clientesGet();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());
        assertEquals("11111111", resp.getBody().get(0).getDni());
        assertEquals("22222222", resp.getBody().get(1).getDni());
        verify(clienteRepository).findAll();
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(restTemplate);
    }

    // ---------- POST /clientes ----------
    @Test
    void clientesPost_creaCliente_ok() {
        ClienteRequest req = buildRequest("Ana", "Lopez", "99999999", "ana@example.com");
        when(clienteRepository.findByDni("99999999")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(ClienteEntity.class)))
                .thenAnswer(inv -> {
                    ClienteEntity x = inv.getArgument(0);
                    x.setId(10L);
                    return x;
                });

        ResponseEntity<ClienteResponse> resp = delegate.clientesPost(req);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(10L, resp.getBody().getId());
        assertEquals("99999999", resp.getBody().getDni());

        ArgumentCaptor<ClienteEntity> captor = ArgumentCaptor.forClass(ClienteEntity.class);
        verify(clienteRepository).findByDni("99999999");
        verify(clienteRepository).save(captor.capture());
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(restTemplate);

        ClienteEntity guardado = captor.getValue();
        assertAll(
                () -> assertEquals("Ana", guardado.getNombre()),
                () -> assertEquals("Lopez", guardado.getApellido()),
                () -> assertEquals("99999999", guardado.getDni()),
                () -> assertEquals("ana@example.com", guardado.getEmail())
        );
    }

    @Test
    void clientesPost_dniDuplicado_lanzaClienteDuplicadoException() {
        ClienteRequest req = buildRequest("Ana", "Lopez", "12345678", "ana@example.com");
        var existente = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findByDni("12345678")).thenReturn(Optional.of(existente));

        assertThrows(ClienteDuplicadoException.class, () -> delegate.clientesPost(req));
        verify(clienteRepository, never()).save(any());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void clientesPost_requestInvalido_lanzaValidacionException() {
        ClienteRequest req = buildRequest(" ", "Lopez", "11111111", "ana@example.com");
        assertThrows(ValidacionException.class, () -> delegate.clientesPost(req));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void clientesPost_dniLongitudInvalida_lanzaExcepcion() {
        var req = buildRequest("Ana", "Lopez", "1234567", "ana@example.com"); // 7 dígitos
        assertThrows(RuntimeException.class, () -> delegate.clientesPost(req));
    }

    @Test
    void clientesPost_emailVacio_lanzaExcepcion() {
        var req = buildRequest("Ana", "Lopez", "12345678", " ");
        assertThrows(RuntimeException.class, () -> delegate.clientesPost(req));
    }

    @Test
    void clientesPost_nombreNull_lanzaValidacionException() {
        ClienteRequest req = buildRequest(null, "Lopez", "11111111", "ana@example.com");
        assertThrows(ValidacionException.class, () -> delegate.clientesPost(req));
        verifyNoInteractions(clienteRepository, restTemplate);
    }

    @Test
    void clientesPost_apellidoNull_lanzaValidacionException() {
        ClienteRequest req = buildRequest("Ana", null, "11111111", "ana@example.com");
        assertThrows(ValidacionException.class, () -> delegate.clientesPost(req));
        verifyNoInteractions(clienteRepository, restTemplate);
    }

    @Test
    void clientesPost_repositoryLanzaExcepcion_propagada() {
        ClienteRequest req = buildRequest("Ana", "Lopez", "99999999", "ana@example.com");
        when(clienteRepository.findByDni("99999999")).thenReturn(Optional.empty());
        when(clienteRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> delegate.clientesPost(req));
        verify(clienteRepository).findByDni("99999999");
        verify(clienteRepository).save(any());
    }

    // ---------- GET /clientes/{id} ----------
    @Test
    void clientesIdGet_noExiste_lanzaClientesNoEncontradosException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ClientesNoEncontradosException.class, () -> delegate.clientesIdGet(99L));
        verify(clienteRepository).findById(99L);
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void clientesIdGet_ok() {
        var e = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(e));

        var resp = delegate.clientesIdGet(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("12345678", resp.getBody().getDni());
        verify(clienteRepository).findById(1L);
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(restTemplate);
    }

    // ---------- DELETE /clientes/{id} ----------
    @Test
    void clientesIdDelete_noExiste_lanzaClientesNoEncontradosException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ClientesNoEncontradosException.class, () -> delegate.clientesIdDelete(1L));
        verify(clienteRepository).findById(1L);
        verify(clienteRepository, never()).deleteById(anyLong());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void clientesIdDelete_conCuentasActivas_lanzaClienteConCuentasActivasException() {
        var e = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(e));

        List<CuentaResponse> cuentas = List.of(new CuentaResponse()); // no vacía
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
    void clientesIdDelete_sinCuentasActivas_elimina_NoContent() {
        var e = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(e));

        ResponseEntity<List<CuentaResponse>> cuentasResp =
                new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8081/cuentas/cliente/{clienteId}"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<CuentaResponse>>>any(),
                eq(1L)
        )).thenReturn(cuentasResp);

        var resp = delegate.clientesIdDelete(1L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(clienteRepository).deleteById(1L);
        verifyNoMoreInteractions(clienteRepository);
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    void clientesIdDelete_bodyNullEnCuentas_elimina_204() {
        var e = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(e));

        ResponseEntity<List<CuentaResponse>> cuentasResp =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8081/cuentas/cliente/{clienteId}"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<CuentaResponse>>>any(),
                eq(1L)
        )).thenReturn(cuentasResp);

        var resp = delegate.clientesIdDelete(1L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(clienteRepository).deleteById(1L);
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void clientesIdDelete_cuentasNoContent_trataComoSinCuentas_Elimina() {
        var e = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(e));

        when(restTemplate.exchange(
                eq("http://localhost:8081/cuentas/cliente/{clienteId}"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<CuentaResponse>>>any(),
                eq(1L)
        )).thenReturn(ResponseEntity.noContent().build());

        var resp = delegate.clientesIdDelete(1L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(clienteRepository).deleteById(1L);
    }

    @Test
    void clientesIdDelete_cuentas5xx_lanzaRuntime() {
        var e = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(e));

        when(restTemplate.exchange(
                anyString(), any(), any(),
                ArgumentMatchers.<ParameterizedTypeReference<List<CuentaResponse>>>any(), anyLong()
        )).thenThrow(new RuntimeException("cuentas-ms down"));

        assertThrows(RuntimeException.class, () -> delegate.clientesIdDelete(1L));
        verify(clienteRepository, never()).deleteById(anyLong());
    }

    // ---------- PUT /clientes/{id} ----------
    @Test
    void clientesIdPut_noExiste_lanzaClientesNoEncontradosException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        var req = buildRequest("X", "Y", "11111111", "x@y.com");
        assertThrows(ClientesNoEncontradosException.class, () -> delegate.clientesIdPut(99L, req));
        verify(clienteRepository).findById(99L);
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void clientesIdPut_cambiaADniExistente_lanzaClienteDuplicadoException() {
        var existente = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));

        var req = buildRequest("Juan", "Pérez", "22222222", "juan2@x.com");
        when(clienteRepository.findByDni("22222222"))
                .thenReturn(Optional.of(buildEntity(2L, "Otro", "Cliente", "22222222", "otro@x.com")));

        assertThrows(ClienteDuplicadoException.class, () -> delegate.clientesIdPut(1L, req));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void clientesIdPut_ok_mismoDni_actualiza_200() {
        var existente = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));

        var req = buildRequest("Juana", "Pérez", "12345678", "juana@x.com");
        when(clienteRepository.save(any(ClienteEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var resp = delegate.clientesIdPut(1L, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Juana", resp.getBody().getNombre());
        assertEquals("12345678", resp.getBody().getDni());

        ArgumentCaptor<ClienteEntity> captor = ArgumentCaptor.forClass(ClienteEntity.class);
        verify(clienteRepository).save(captor.capture());
        ClienteEntity actualizado = captor.getValue();
        assertAll(
                () -> assertEquals(1L, actualizado.getId()),
                () -> assertEquals("Juana", actualizado.getNombre()),
                () -> assertEquals("Pérez", actualizado.getApellido()),
                () -> assertEquals("12345678", actualizado.getDni()),
                () -> assertEquals("juana@x.com", actualizado.getEmail())
        );
        verifyNoInteractions(restTemplate);
    }

    @Test
    void clientesIdPut_requestInvalido_lanzaValidacionException() {
        // VALIDACIÓN ANTES DEL REPO → no se consulta findById
        var req = buildRequest("", "Pérez", "12345678", "mail@x.com"); // nombre vacío
        assertThrows(ValidacionException.class, () -> delegate.clientesIdPut(1L, req));
        verifyNoInteractions(clienteRepository, restTemplate);
    }

    @Test
    void clientesIdPut_mismoDni_noConsultaDuplicado_Guarda() {
        var existente = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));

        var req = buildRequest("Juan", "Pérez", "12345678", "nuevo@x.com"); // mismo DNI
        when(clienteRepository.save(any(ClienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = delegate.clientesIdPut(1L, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        verify(clienteRepository).findById(1L);
        verify(clienteRepository, never()).findByDni(anyString()); // clave
        verify(clienteRepository).save(any(ClienteEntity.class));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void clientesIdPut_cambiaADniNuevo_ok_guarda() {
        var existente = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));

        var req = buildRequest("Juan", "Pérez", "87654321", "juan2@x.com");
        when(clienteRepository.findByDni("87654321")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(ClienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = delegate.clientesIdPut(1L, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("87654321", resp.getBody().getDni());
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).findByDni("87654321");
        verify(clienteRepository).save(any(ClienteEntity.class));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void clientesIdPut_mapeaTodosLosCampos_Actualizacion() {
        var existente = buildEntity(1L, "Juan", "Pérez", "12345678", "juan@x.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));

        var req = buildRequest("Juana", "García", "12345678", "juana@x.com");
        when(clienteRepository.save(any(ClienteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = delegate.clientesIdPut(1L, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Juana", resp.getBody().getNombre());
        assertEquals("García", resp.getBody().getApellido());
        assertEquals("juana@x.com", resp.getBody().getEmail());

        var captor = ArgumentCaptor.forClass(ClienteEntity.class);
        verify(clienteRepository).save(captor.capture());
        var actualizado = captor.getValue();
        assertEquals("Juana", actualizado.getNombre());
        assertEquals("García", actualizado.getApellido());
        assertEquals("juana@x.com", actualizado.getEmail());
    }
}
