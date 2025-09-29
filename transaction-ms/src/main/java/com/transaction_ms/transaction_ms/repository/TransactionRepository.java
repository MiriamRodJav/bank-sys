package com.transaction_ms.transaction_ms.repository;

import com.transaction_ms.transaction_ms.domain.Transaction;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;


@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {


}
