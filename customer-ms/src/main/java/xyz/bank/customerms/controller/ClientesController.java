package xyz.bank.customerms.controller;

import com.xyzbank.api.controller.V1Api;
import com.xyzbank.api.model.Cliente;
import com.xyzbank.api.model.ClienteCreate;
import com.xyzbank.api.model.ClienteUpdate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.bank.customerms.service.ClienteService;

import java.util.List;
import java.util.UUID;

@RestController
public class ClientesController implements V1Api {

    private final ClienteService service;

    public ClientesController(ClienteService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<Cliente>> listClientes() {
        return ResponseEntity.ok(service.list());
    }

    @Override
    public ResponseEntity<Cliente> getCliente(UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Override
    public ResponseEntity<Cliente> createCliente(ClienteCreate body) {
        var created = service.create(body);
        return ResponseEntity.status(201).body(created);
    }

    @Override
    public ResponseEntity<Cliente> updateCliente(UUID id, ClienteUpdate body) {
        return ResponseEntity.ok(service.update(id, body));
    }

    @Override
    public ResponseEntity<Void> deleteCliente(UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

