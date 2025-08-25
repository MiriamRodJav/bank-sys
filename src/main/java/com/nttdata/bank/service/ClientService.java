package com.nttdata.bank.service;

import com.nttdata.bank.model.ClientModel;
import com.nttdata.bank.repository.ClientRepository;

public class ClientService {

    private final ClientRepository repository = new ClientRepository();

    public ClientModel registerClient(ClientModel client) throws Exception {

        if (client.getFirstName() == null || client.getLastName() == null || client.getDni() == null || client.getEmail() == null) {
            throw new Exception("All fields are required");
        }

        ClientModel dniSearch = repository.findByDni(client.getDni());

        if (dniSearch != null){
            throw new Exception("DNI is already registered");
        }

        return repository.save(client);
    }
}
