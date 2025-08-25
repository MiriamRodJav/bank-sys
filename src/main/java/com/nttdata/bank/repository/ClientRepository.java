package com.nttdata.bank.repository;

import com.nttdata.bank.model.ClientModel;

import java.util.Optional;

public interface ClientRepository {
    ClientModel save(ClientModel client);
    Optional<ClientModel> findByDni(String dni);

    boolean delete(String dni);
    boolean existsByDni(String dni);
}
