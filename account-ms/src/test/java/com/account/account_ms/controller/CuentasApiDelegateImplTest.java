package com.account.account_ms.controller;

import com.account.account_ms.entity.CuentaEntity;
import com.account.account_ms.exceptions.ClienteNoEncontradoException;
import com.account.account_ms.exceptions.CuentaNoEncontradaException;
import com.account.account_ms.exceptions.SaldoInsuficienteException;
import com.account.account_ms.exceptions.ValidacionCuentaException;
import com.account.account_ms.model.*;
import com.account.account_ms.repository.CuentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentasApiDelegateImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CuentasApiDelegateImpl delegate;

    private CuentaRequest buildCuentaRequest(double saldoInicial) {
        CuentaRequest req = new CuentaRequest();
        req.setClienteId(10L);
        req.setTipoCuenta(TipoCuenta.AHORROS); // usa enum del contrato
        req.setSaldoInicial(saldoInicial);
        return req;
    }

    private CuentaEntity buildCuentaEntity(Long id, long clienteId, CuentaEntity.TipoCuentaEnum tipo, double saldo) {
        CuentaEntity e = new CuentaEntity();
        e.setId(id);
        e.setClienteId(clienteId);
        e.setTipoCuenta(tipo);
        e.setSaldo(saldo);
        e.setNumeroCuenta("ABC1234567");
        return e;
    }
    // helper en tu clase de tests
    private CuentasCuentaIdDepositarPutRequest buildDepositarBody(double monto) {
        CuentasCuentaIdDepositarPutRequest b = new CuentasCuentaIdDepositarPutRequest();
        b.setMonto(monto);
        return b;
    }

    // helper para el body del retiro
    private CuentasCuentaIdRetirarPutRequest buildRetirarBody(double monto) {
        CuentasCuentaIdRetirarPutRequest b = new CuentasCuentaIdRetirarPutRequest();
        b.setMonto(monto);
        return b;
    }



// ---------- PUT /cuentas/{id}/depositar ----------

    @Test
    void cuentasCuentaIdDepositarPut_noExiste_lanzaCuentaNoEncontradaException() {
        long idInexistente = 999L;
        when(cuentaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class,
                () -> delegate.cuentasCuentaIdDepositarPut(idInexistente, buildDepositarBody(100.0)));

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void cuentasCuentaIdDepositarPut_montoInvalido_lanzaValidacionCuentaException() {
        long id = 7L;
        // La cuenta SÍ existe para alcanzar la validación del monto
        var existente = buildCuentaEntity(id, 10L, CuentaEntity.TipoCuentaEnum.AHORROS, 200.0);
        when(cuentaRepository.findById(id)).thenReturn(Optional.of(existente));

        // monto <= 0
        assertThrows(ValidacionCuentaException.class,
                () -> delegate.cuentasCuentaIdDepositarPut(id, buildDepositarBody(0.0)));

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void cuentasPost_clienteNoOk_lanzaClienteNoEncontradoException() {
        // given
        CuentaRequest req = buildCuentaRequest(100.0); // pasa la validación de saldo
        // el servicio de clientes responde, pero NOT OK (ej. 204 o 400)
        when(restTemplate.exchange(
                startsWith("http://localhost:8080/clientes/"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Void.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build());

        // when / then
        assertThrows(ClienteNoEncontradoException.class, () -> delegate.cuentasPost(req));

        // no debe intentar guardar nada
        verifyNoInteractions(cuentaRepository);
    }

    @Test
    void cuentasPost_clienteNotFound_exception_lanzaClienteNoEncontradoException() {
        // given
        CuentaRequest req = buildCuentaRequest(150.0);
        // simulamos 404 lanzado por RestTemplate
        when(restTemplate.exchange(
                startsWith("http://localhost:8080/clientes/"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Void.class)
        )).thenThrow(HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        // when / then
        assertThrows(ClienteNoEncontradoException.class, () -> delegate.cuentasPost(req));

        // no debe intentar guardar nada
        verifyNoInteractions(cuentaRepository);
    }


    // ---------- PUT /cuentas/{id}/retirar ----------

    @Test
    void cuentasCuentaIdRetirarPut_noExiste_lanzaCuentaNoEncontradaException() {
        long id = 404L;
        when(cuentaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class,
                () -> delegate.cuentasCuentaIdRetirarPut(id, buildRetirarBody(10.0)));

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void cuentasCuentaIdRetirarPut_montoInvalido_lanzaValidacionCuentaException() {
        long id = 1L;
        var ahorros = buildCuentaEntity(id, 10L, CuentaEntity.TipoCuentaEnum.AHORROS, 100.0);
        when(cuentaRepository.findById(id)).thenReturn(Optional.of(ahorros));

        assertThrows(ValidacionCuentaException.class,
                () -> delegate.cuentasCuentaIdRetirarPut(id, buildRetirarBody(0.0)));

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void cuentasCuentaIdRetirarPut_ahorros_saldoInsuficiente_lanzaExcepcion() {
        long id = 2L;
        var ahorros = buildCuentaEntity(id, 10L, CuentaEntity.TipoCuentaEnum.AHORROS, 50.0);
        when(cuentaRepository.findById(id)).thenReturn(Optional.of(ahorros));

        // 50 - 60 < 0  -> debe lanzar SaldoInsuficienteException
        assertThrows(SaldoInsuficienteException.class,
                () -> delegate.cuentasCuentaIdRetirarPut(id, buildRetirarBody(60.0)));

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void cuentasCuentaIdRetirarPut_corriente_excedeSobregiro_lanzaExcepcion() {
        long id = 3L;
        var corriente = buildCuentaEntity(id, 11L, CuentaEntity.TipoCuentaEnum.CORRIENTE, 0.0);
        when(cuentaRepository.findById(id)).thenReturn(Optional.of(corriente));

        // 0 - 600 = -600 < -500  -> excede sobregiro permitido
        assertThrows(SaldoInsuficienteException.class,
                () -> delegate.cuentasCuentaIdRetirarPut(id, buildRetirarBody(600.0)));

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void cuentasCuentaIdRetirarPut_ok_actualizaSaldo_yRetorna200() {
        long id = 4L;
        var corriente = buildCuentaEntity(id, 12L, CuentaEntity.TipoCuentaEnum.CORRIENTE, 100.0);
        when(cuentaRepository.findById(id)).thenReturn(Optional.of(corriente));
        when(cuentaRepository.save(any(CuentaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // retiro dentro del límite de sobregiro (queda -400, permitido)
        var resp = delegate.cuentasCuentaIdRetirarPut(id, buildRetirarBody(500.0));

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(id, resp.getBody().getId());
        assertEquals(-400.0, resp.getBody().getSaldo());

        ArgumentCaptor<CuentaEntity> captor = ArgumentCaptor.forClass(CuentaEntity.class);
        verify(cuentaRepository).save(captor.capture());
        assertEquals(-400.0, captor.getValue().getSaldo());

        verifyNoInteractions(restTemplate);
    }

    @Test
    void cuentasClienteClienteIdGet_sinCuentas_retornaListaVacia() {
        Long clienteId = 99L;
        when(cuentaRepository.findByClienteId(clienteId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<CuentaResponse>> response = delegate.cuentasClienteClienteIdGet(clienteId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(cuentaRepository).findByClienteId(clienteId);
    }

    @Test
    void cuentasClienteClienteIdGet_conCuentas_retornaListaMapeada() {
        Long clienteId = 1L;
        CuentaEntity cuenta = buildCuentaEntity(1L, clienteId, CuentaEntity.TipoCuentaEnum.AHORROS, 100.0);
        when(cuentaRepository.findByClienteId(clienteId)).thenReturn(List.of(cuenta));

        ResponseEntity<List<CuentaResponse>> response = delegate.cuentasClienteClienteIdGet(clienteId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("ABC1234567", response.getBody().get(0).getNumeroCuenta());
        verify(cuentaRepository).findByClienteId(clienteId);
    }



    @Test
    void cuentasCuentaIdDepositarPut_ok_actualizaSaldo_yRetorna200() {
        long id = 8L;
        var existente = buildCuentaEntity(id, 11L, CuentaEntity.TipoCuentaEnum.CORRIENTE, 100.0);
        when(cuentaRepository.findById(id)).thenReturn(Optional.of(existente));

        // el repo devuelve la entidad actualizada al guardar
        when(cuentaRepository.save(any(CuentaEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var body = buildDepositarBody(50.0); // +50 al saldo

        var resp = delegate.cuentasCuentaIdDepositarPut(id, body);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(id, resp.getBody().getId());
        assertEquals(150.0, resp.getBody().getSaldo()); // 100 + 50
        assertEquals(TipoCuenta.CORRIENTE, resp.getBody().getTipoCuenta());

        // verificar que se guardó con el saldo actualizado
        ArgumentCaptor<CuentaEntity> captor = ArgumentCaptor.forClass(CuentaEntity.class);
        verify(cuentaRepository).save(captor.capture());
        assertEquals(150.0, captor.getValue().getSaldo());

        verifyNoInteractions(restTemplate);
    }

    // ---------- POST /cuentas ----------

    @Test
    void cuentasPost_creaCuenta_ok() {
        // given
        CuentaRequest req = buildCuentaRequest(100.0);

        // Cliente existe → RestTemplate 200 OK
        when(restTemplate.exchange(
                startsWith("http://localhost:8080/clientes/"),
                eq(HttpMethod.GET),
                isNull(),
                eq(Void.class)
        )).thenReturn(ResponseEntity.ok().build());

        // Repo guarda y retorna entidad con ID
        when(cuentaRepository.save(any(CuentaEntity.class)))
                .thenAnswer(inv -> {
                    CuentaEntity e = inv.getArgument(0);
                    e.setId(1L);
                    return e;
                });

        // when
        ResponseEntity<CuentaResponse> resp = delegate.cuentasPost(req);

        // then
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(1L, resp.getBody().getId());
        assertEquals(100.0, resp.getBody().getSaldo());

        // además, verificamos que el mapping tomó el tipo correcto
        assertEquals(TipoCuenta.AHORROS, resp.getBody().getTipoCuenta());

        // capturar lo que se guardó
        ArgumentCaptor<CuentaEntity> captor = ArgumentCaptor.forClass(CuentaEntity.class);
        verify(cuentaRepository).save(captor.capture());
        CuentaEntity saved = captor.getValue();
        assertEquals(10L, saved.getClienteId());
        assertEquals(CuentaEntity.TipoCuentaEnum.AHORROS, saved.getTipoCuenta());
        assertEquals(100.0, saved.getSaldo());
        assertNotNull(saved.getNumeroCuenta());
    }

    @Test
    void cuentasPost_saldoInicialInvalido_lanzaValidacionCuentaException() {
        // given
        CuentaRequest req = buildCuentaRequest(0.0);

        // when / then
        assertThrows(ValidacionCuentaException.class, () -> delegate.cuentasPost(req));

        // no debe tocar el repo ni el restTemplate
        verifyNoInteractions(restTemplate);
        verifyNoInteractions(cuentaRepository);
    }


    // ---------- GET /cuentas ----------

    @Test
    void cuentasGet_listaVacia_lanzaCuentaNoEncontradaException() {
        when(cuentaRepository.findAll()).thenReturn(List.of());

        assertThrows(CuentaNoEncontradaException.class, () -> delegate.cuentasGet());
    }

    // ---------- DELETE /cuentas/{id} ----------

    @Test
    void cuentasIdDelete_noExiste_lanzaCuentaNoEncontradaException() {
        when(cuentaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class, () -> delegate.cuentasIdDelete(999L));

        verify(cuentaRepository, never()).deleteById(anyLong());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void cuentasIdDelete_saldoNoCero_lanzaSaldoInsuficienteException() {
        var conSaldo = buildCuentaEntity(7L, 10L, CuentaEntity.TipoCuentaEnum.AHORROS, 5.0); // saldo != 0
        when(cuentaRepository.findById(7L)).thenReturn(Optional.of(conSaldo));

        assertThrows(SaldoInsuficienteException.class, () -> delegate.cuentasIdDelete(7L));

        verify(cuentaRepository, never()).deleteById(anyLong());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void cuentasIdDelete_ok_saldoCero_eliminaYRetornaNoContent() {
        var saldoCero = buildCuentaEntity(8L, 10L, CuentaEntity.TipoCuentaEnum.CORRIENTE, 0.0); // saldo == 0
        when(cuentaRepository.findById(8L)).thenReturn(Optional.of(saldoCero));

        var resp = delegate.cuentasIdDelete(8L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(cuentaRepository).deleteById(8L);
        verifyNoInteractions(restTemplate);
    }


    @Test
    void cuentasGet_ok_retornaListaMapeada() {
        var e1 = buildCuentaEntity(1L, 10L, CuentaEntity.TipoCuentaEnum.AHORROS, 120.0);
        var e2 = buildCuentaEntity(2L, 11L, CuentaEntity.TipoCuentaEnum.CORRIENTE, 50.0);
        when(cuentaRepository.findAll()).thenReturn(List.of(e1, e2));

        var resp = delegate.cuentasGet();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());
        assertEquals(1L, resp.getBody().get(0).getId());
        assertEquals(2L, resp.getBody().get(1).getId());
    }

    // ---------- GET /cuentas/{id} ----------

    @Test
    void cuentasIdGet_noExiste_lanzaCuentaNoEncontradaException() {
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(CuentaNoEncontradaException.class, () -> delegate.cuentasIdGet(99L));
    }

    @Test
    void cuentasIdGet_ok() {
        var e = buildCuentaEntity(5L, 10L, CuentaEntity.TipoCuentaEnum.CORRIENTE, 300.0);
        when(cuentaRepository.findById(5L)).thenReturn(Optional.of(e));

        var resp = delegate.cuentasIdGet(5L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(5L, resp.getBody().getId());
        assertEquals(300.0, resp.getBody().getSaldo());
        assertEquals(TipoCuenta.CORRIENTE, resp.getBody().getTipoCuenta());
    }



}
