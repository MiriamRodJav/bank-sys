package com.nttdata.bank.service;

import com.nttdata.bank.model.ClientModel;
import com.nttdata.bank.repository.ClientRepository;

/**
 * Service class for managing client operations.
 */
public class ClientService {

    private final ClientRepository repository = new ClientRepository();

    /**
     * Registers a new client after validating required fields and email format.
     *
     * @param client to register
     * @return the saved client
     * @throws Exception if required fields are missing, email is invalid, or DNI is already registered
     */
    public ClientModel registerClient(ClientModel client) throws Exception {

        if (client.getFirstName() == null || client.getLastName() == null || client.getDni() == null || client.getEmail() == null) {
            throw new Exception("All fields are required");
        }

        if (!client.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new Exception("Invalid email format");
        }

        ClientModel dniSearch = repository.findByDni(client.getDni());

        if (dniSearch != null){
            throw new Exception("DNI is already registered");
        }

        return repository.save(client);
    }
}
