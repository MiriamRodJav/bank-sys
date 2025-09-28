package com.customer.customer_ms.api;

import com.account.client.api.CuentasApi;
import com.account.client.model.CuentaResponse;

import com.customer.customer_ms.entity.ClienteEntity;
import com.customer.customer_ms.exceptions.ClienteConCuentasActivasException;
import com.customer.customer_ms.exceptions.ClienteDuplicadoException;
import com.customer.customer_ms.exceptions.ClientesNoEncontradosException;
import com.customer.customer_ms.exceptions.ValidacionException;
import com.customer.customer_ms.model.ClienteRequest;
import com.customer.customer_ms.model.ClienteResponse;
import com.customer.customer_ms.repository.ClienteRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientesApiDelegateImpl implements ClientesApiDelegate {

    private final ClienteRepository clienteRepository;
    private final CuentasApi cuentasApi;

    public ClientesApiDelegateImpl(ClienteRepository clienteRepository, CuentasApi cuentasApi) {
        this.clienteRepository = clienteRepository;
        this.cuentasApi = cuentasApi;
    }

    // ⚠️ Firma correcta (con page, size, sort)
    @Override
    public ResponseEntity<List<ClienteResponse>> clientesGet(Integer page, Integer size, String sort) {
        // Por simplicidad, ignoro paginación aquí; puedes mapearla a Pageable si quieres.
        List<ClienteEntity> clientes = clienteRepository.findAll();

        if (clientes.isEmpty()) {
            throw new ClientesNoEncontradosException("No hay clientes registrados");
        }

        List<ClienteResponse> clienteResponses = clientes.stream()
                .map(this::mapToClienteResponse)
                .collect(Collectors.toList());

        return new ResponseEntity<>(clienteResponses, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ClienteResponse> clientesPost(ClienteRequest clienteRequest) {
        validarClienteRequest(clienteRequest);

        if (clienteRepository.findByDni(clienteRequest.getDni()).isPresent()) {
            throw new ClienteDuplicadoException(clienteRequest.getDni());
        }

        ClienteEntity nuevoCliente = mapToClienteEntity(clienteRequest);
        ClienteEntity clienteGuardado = clienteRepository.save(nuevoCliente);

        ClienteResponse clienteResponse = mapToClienteResponse(clienteGuardado);
        return new ResponseEntity<>(clienteResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ClienteResponse> clientesIdGet(Long id) {
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClientesNoEncontradosException("Cliente con ID " + id + " no encontrado."));
        return new ResponseEntity<>(mapToClienteResponse(cliente), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> clientesIdDelete(Long id) {
        clienteRepository.findById(id)
                .orElseThrow(() -> new ClientesNoEncontradosException("Cliente con ID " + id + " no encontrado."));

        // Usa el cliente generado de account.
        // Si te da "cannot find symbol", abre CuentasApi.java en target y usa el nombre real (a veces termina en UsingGET).
        List<CuentaResponse> cuentas = cuentasApi.cuentasClienteClienteIdGet(id);

        if (cuentas != null && !cuentas.isEmpty()) {
            throw new ClienteConCuentasActivasException(
                    "El cliente con ID " + id + " no puede ser eliminado porque tiene cuentas activas.");
        }

        clienteRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<ClienteResponse> clientesIdPut(Long id, ClienteRequest clienteRequest) {
        validarClienteRequest(clienteRequest);

        ClienteEntity clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClientesNoEncontradosException("Cliente con ID " + id + " no encontrado."));

        if (!clienteExistente.getDni().equals(clienteRequest.getDni())
                && clienteRepository.findByDni(clienteRequest.getDni()).isPresent()) {
            throw new ClienteDuplicadoException("El DNI " + clienteRequest.getDni() + " ya está registrado en otro cliente.");
        }

        clienteExistente.setNombre(clienteRequest.getNombre());
        clienteExistente.setApellido(clienteRequest.getApellido());
        clienteExistente.setDni(clienteRequest.getDni());
        clienteExistente.setEmail(clienteRequest.getEmail());

        ClienteEntity actualizado = clienteRepository.save(clienteExistente);
        return new ResponseEntity<>(mapToClienteResponse(actualizado), HttpStatus.OK);
    }

    private void validarClienteRequest(ClienteRequest request) {
        if (request.getNombre() == null || request.getNombre().trim().isEmpty())
            throw new ValidacionException("El nombre es requerido");
        if (request.getApellido() == null || request.getApellido().trim().isEmpty())
            throw new ValidacionException("El apellido es requerido");
        if (request.getDni() == null || request.getDni().trim().isEmpty())
            throw new ValidacionException("El DNI es requerido");
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()
                && !request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidacionException("El formato del email es inválido");
        }
    }

    private ClienteEntity mapToClienteEntity(ClienteRequest request) {
        ClienteEntity e = new ClienteEntity();
        e.setNombre(request.getNombre());
        e.setApellido(request.getApellido());
        e.setDni(request.getDni());
        e.setEmail(request.getEmail());
        return e;
    }

    private ClienteResponse mapToClienteResponse(ClienteEntity c) {
        ClienteResponse r = new ClienteResponse();
        r.setId(c.getId());
        r.setDni(c.getDni());
        r.setNombre(c.getNombre());
        r.setApellido(c.getApellido());
        r.setEmail(c.getEmail());
        return r;
    }
}
