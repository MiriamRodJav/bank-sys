package xyz.bank.accountms.controller;

import com.xyzbank.api.controller.V1Api;
import com.xyzbank.api.model.Cuenta;
import com.xyzbank.api.model.CuentaCreate;
import com.xyzbank.api.model.Movimiento;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.bank.accountms.service.CuentaService;

import java.util.List;
import java.util.UUID;

@RestController
public class CuentasController implements V1Api {

    private final CuentaService service;

    public CuentasController(CuentaService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<Cuenta>> listCuentas(UUID clienteId) {
        return ResponseEntity.ok(service.list(clienteId));
    }

    @Override
    public ResponseEntity<Cuenta> createCuenta(CuentaCreate cuentaCreate) {
        var created = service.create(cuentaCreate);
        return ResponseEntity.status(201).body(created);
    }

    @Override
    public ResponseEntity<Cuenta> getCuenta(UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Override
    public ResponseEntity<Void> deleteCuenta(UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Cuenta> depositar(UUID cuentaId, Movimiento movimiento) {
        return ResponseEntity.ok(service.depositar(cuentaId, movimiento));
    }

    @Override
    public ResponseEntity<Cuenta> retirar(UUID cuentaId, Movimiento movimiento) {
        return ResponseEntity.ok(service.retirar(cuentaId, movimiento));
    }
}
