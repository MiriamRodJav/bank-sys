package com.nttdata.bank.service;

import com.nttdata.bank.model.ClientModel;
import com.nttdata.bank.repository.ClientRepository;

public class ClientService {
    private final ClientRepository repository = new ClientRepository();

    public void registerClient(ClientModel client) throws Exception {
        if (client.getFirstName() == null || client.getLastName() == null || client.getDni() == null || client.getEmail() == null) {
            throw new Exception("All fields are required");
        }
        repository.save(client);
    }
}
