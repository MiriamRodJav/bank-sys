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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientesApiDelegateImpl implements ClientesApiDelegate {
    private final ClienteRepository clienteRepository;
    private final RestTemplate restTemplate;
    public ClientesApiDelegateImpl(ClienteRepository clienteRepository, RestTemplate restTemplate) {
        this.clienteRepository = clienteRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public ResponseEntity<List<ClienteResponse>> clientesGet() {
        try {
            List<ClienteEntity> clientes = clienteRepository.findAll();

            if (clientes.isEmpty()) {
                throw new ClientesNoEncontradosException("No hay clientes registrados");
            }

            List<ClienteResponse> clienteResponses = clientes.stream()
                    .map(this::mapToClienteResponse)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(clienteResponses, HttpStatus.OK);

        } catch (ClientesNoEncontradosException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error interno al obtener clientes", ex);
        }
    }

    @Override
    public ResponseEntity<ClienteResponse> clientesPost(ClienteRequest clienteRequest) {
        try {
            validarClienteRequest(clienteRequest);

            if (clienteRepository.findByDni(clienteRequest.getDni()).isPresent()) {
                throw new ClienteDuplicadoException(clienteRequest.getDni());
            }

            ClienteEntity nuevoCliente = mapToClienteEntity(clienteRequest);
            ClienteEntity clienteGuardado = clienteRepository.save(nuevoCliente);

            ClienteResponse clienteResponse = mapToClienteResponse(clienteGuardado);

            return new ResponseEntity<>(clienteResponse, HttpStatus.CREATED);

        } catch (ValidacionException | ClienteDuplicadoException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error interno al crear cliente", ex);
        }
    }

    @Override
    public ResponseEntity<ClienteResponse> clientesIdGet(Long id) {
        Optional<ClienteEntity> clienteOptional = clienteRepository.findById(id);

        if (clienteOptional.isEmpty()) {
            throw new ClientesNoEncontradosException("Cliente con ID " + id + " no encontrado.");
        }

        ClienteResponse clienteResponse = mapToClienteResponse(clienteOptional.get());
        return new ResponseEntity<>(clienteResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> clientesIdDelete(Long id) {
        Optional<ClienteEntity> clienteOptional = clienteRepository.findById(id);

        if (clienteOptional.isEmpty()) {
            throw new ClientesNoEncontradosException("Cliente con ID " + id + " no encontrado.");
        }

        ResponseEntity<List<CuentaResponse>> cuentasResponse = restTemplate.exchange(
                "http://localhost:8081/cuentas/cliente/{clienteId}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() { },
                id
        );

        List<CuentaResponse> cuentas = cuentasResponse.getBody();

        boolean tieneCuentasActivas = cuentas != null && !cuentas.isEmpty();

        if (tieneCuentasActivas) {
            throw new ClienteConCuentasActivasException("El cliente con ID " + id +
                    " no puede ser eliminado porque tiene cuentas activas.");
        }

        clienteRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<ClienteResponse> clientesIdPut(Long id, ClienteRequest clienteRequest) {
        validarClienteRequest(clienteRequest);

        Optional<ClienteEntity> clienteOptional = clienteRepository.findById(id);

        if (clienteOptional.isEmpty()) {
            throw new ClientesNoEncontradosException("Cliente con ID " + id + " no encontrado.");
        }

        ClienteEntity clienteExistente = clienteOptional.get();


        if (!clienteExistente.getDni().equals(clienteRequest.getDni())) {
            if (clienteRepository.findByDni(clienteRequest.getDni()).isPresent()) {
                throw new ClienteDuplicadoException("El DNI " + clienteRequest.getDni() +
                        " ya está registrado en otro cliente.");
            }
        }


        clienteExistente.setNombre(clienteRequest.getNombre());
        clienteExistente.setApellido(clienteRequest.getApellido());
        clienteExistente.setDni(clienteRequest.getDni());
        clienteExistente.setEmail(clienteRequest.getEmail());


        ClienteEntity clienteActualizado = clienteRepository.save(clienteExistente);

        ClienteResponse clienteResponse = mapToClienteResponse(clienteActualizado);

        return new ResponseEntity<>(clienteResponse, HttpStatus.OK);
    }
    private void validarClienteRequest(ClienteRequest request) {
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new ValidacionException("El nombre es requerido");
        }
        if (request.getApellido() == null || request.getApellido().trim().isEmpty()) {
            throw new ValidacionException("El apellido es requerido");
        }
        if (request.getDni() == null || request.getDni().trim().isEmpty()) {
            throw new ValidacionException("El DNI es requerido");
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!isEmailValido(request.getEmail())) {
                throw new ValidacionException("El formato del email es inválido");
            }
        }
    }

    private boolean isEmailValido(String email) {

        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private ClienteEntity mapToClienteEntity(ClienteRequest request) {
        ClienteEntity entity = new ClienteEntity();
        entity.setNombre(request.getNombre());
        entity.setApellido(request.getApellido());
        entity.setDni(request.getDni());
        entity.setEmail(request.getEmail());
        return entity;
    }

    private ClienteResponse mapToClienteResponse(ClienteEntity cliente) {
        ClienteResponse response = new ClienteResponse();
        response.setId(cliente.getId());
        response.setDni(cliente.getDni());
        response.setNombre(cliente.getNombre());
        response.setApellido(cliente.getApellido());
        response.setEmail(cliente.getEmail());
        return response;
    }
}
