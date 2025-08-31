package xyz.bank.customerms.service;

import com.xyzbank.api.model.ClienteCreate;
import com.xyzbank.api.model.ClienteUpdate;
import org.springframework.stereotype.Service;
import xyz.bank.customerms.config.AccountClient;
import xyz.bank.customerms.domain.Cliente;
import xyz.bank.customerms.repo.ClienteRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository repo;
    private final AccountClient accountClient;

    public ClienteService(ClienteRepository repo, AccountClient accountClient) {
        this.repo = repo;
        this.accountClient = accountClient;
    }

    public List<com.xyzbank.api.model.Cliente> list() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public com.xyzbank.api.model.Cliente get(UUID id) {
        var c = repo.findById(id).orElseThrow(() -> new NotFound("Cliente no encontrado"));
        return toDto(c);
    }

    @Transactional
    public com.xyzbank.api.model.Cliente create(ClienteCreate in) {
        if (repo.existsByDni(in.getDni())) throw new Conflict("DNI duplicado");
        var c = new Cliente();
        c.setNombre(in.getNombre());
        c.setApellido(in.getApellido());
        c.setDni(in.getDni());
        c.setEmail(in.getEmail());
        c = repo.save(c);
        return toDto(c);
    }

    @Transactional
    public com.xyzbank.api.model.Cliente update(UUID id, ClienteUpdate in) {
        var c = repo.findById(id).orElseThrow(() -> new NotFound("Cliente no encontrado"));
        if (in.getNombre() != null) c.setNombre(in.getNombre());
        if (in.getApellido() != null) c.setApellido(in.getApellido());
        if (in.getEmail() != null) c.setEmail(in.getEmail());
        return toDto(repo.save(c));
    }

    @Transactional
    public void delete(UUID id) {
        var c = repo.findById(id).orElseThrow(() -> new NotFound("Cliente no encontrado"));
        if (accountClient.hasAccounts(id)) {
            throw new Conflict("No se puede eliminar: el cliente tiene cuentas activas o no se pudo verificar");
        }
        repo.delete(c);
    }

    private com.xyzbank.api.model.Cliente toDto(Cliente c) {
        var dto = new com.xyzbank.api.model.Cliente();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        dto.setApellido(c.getApellido());
        dto.setDni(c.getDni());
        dto.setEmail(c.getEmail());
        return dto;
    }

    public static class NotFound extends RuntimeException {
        public NotFound(String m) {
            super(m);
        }
    }

    public static class Conflict extends RuntimeException {
        public Conflict(String m) {
            super(m);
        }
    }
}

