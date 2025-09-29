package com.transaction_ms.transaction_ms.service;
import com.transaction_ms.transaction_ms.client.AccountsClient;
import com.transaction_ms.transaction_ms.domain.Transaction;
import com.transaction_ms.transaction_ms.exception.TransactionException;
import com.transaction_ms.transaction_ms.model.DepositoRequest;
import com.transaction_ms.transaction_ms.model.RetiroRequest;
import com.transaction_ms.transaction_ms.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private TransactionCreator transactionCreator;

    @InjectMocks
    private TransactionService transactionService;

    private DepositoRequest depositRequest;
    private Transaction successTransaction;
    private Transaction failedTransaction;

    private RetiroRequest withdrawRequest;
    private Transaction successWithdrawal;
    private Transaction failedWithdrawal;
    @BeforeEach
    void setUp() {
        depositRequest = new DepositoRequest("12345", 100.0);

        successTransaction = new Transaction("T-1", "DEPOSIT", 100.0,
                LocalDateTime.now(), "12345",
                null, "DEP_REF", "SUCCESS");

        failedTransaction = new Transaction("T-2", "DEPOSIT", 100.0,
                LocalDateTime.now(), "12345",
                null, "DEP_REF", "FAILED");

        withdrawRequest = new RetiroRequest("67890", 50.0);

        successWithdrawal = new Transaction("T-3", "WITHDRAW", 50.0,
                LocalDateTime.now(), "67890",
                null, "WIT_REF", "SUCCESS");

        failedWithdrawal = new Transaction("T-4", "WITHDRAW", 50.0,
                LocalDateTime.now(), "67890",
                null, "WIT_REF", "FAILED");
    }

    @Test
    void processDeposit_Success_ShouldCallClientAndSaveSuccessTransaction() {
        when(transactionCreator.createTransactionDeposit(depositRequest, "SUCCESS"))
                .thenReturn(successTransaction);
        when(transactionRepository.save(successTransaction))
                .thenReturn(Mono.just(successTransaction));

        StepVerifier.create(transactionService.processDeposit(depositRequest))
                .expectNext(successTransaction)
                .verifyComplete();

        verify(accountsClient, times(1)).deposit(depositRequest);
        verify(transactionCreator, times(1)).createTransactionDeposit(depositRequest,
                "SUCCESS");
        verify(transactionRepository, times(1)).save(successTransaction);
        verify(transactionRepository, never()).save(failedTransaction);
    }

    @Test
    void processDeposit_Failure_ShouldSaveFailedTransactionAndPropagateError() {
        TransactionException clientException = new TransactionException(
                "Saldo insuficiente",
                "C001",
                HttpStatus.BAD_REQUEST
        );
        doThrow(clientException).when(accountsClient).deposit(depositRequest);
        when(transactionCreator.createTransactionDeposit(depositRequest, "FAILED"))
                .thenReturn(failedTransaction);
        when(transactionRepository.save(failedTransaction))
                .thenReturn(Mono.just(failedTransaction));

        StepVerifier.create(transactionService.processDeposit(depositRequest))
                .verifyErrorSatisfies(error -> {
                    assertTrue(error instanceof TransactionException);
                    assertEquals("Saldo insuficiente", error.getMessage());
                });

        verify(accountsClient, times(1)).deposit(depositRequest);
        verify(transactionCreator, times(1)).createTransactionDeposit(depositRequest,
                "FAILED");
        verify(transactionRepository, times(1)).save(failedTransaction);
        verify(transactionRepository, never()).save(successTransaction);
    }

    @Test
    void processWithdraw_Success_ShouldCallClientAndSaveSuccessTransaction() {

        when(transactionCreator.createTransactionWithdrawal(withdrawRequest, "SUCCESS"))
                .thenReturn(successWithdrawal);
        when(transactionRepository.save(successWithdrawal))
                .thenReturn(Mono.just(successWithdrawal));

        StepVerifier.create(transactionService.processWithdraw(withdrawRequest))
                .expectNext(successWithdrawal)
                .verifyComplete();

        verify(accountsClient, times(1)).withdraw(withdrawRequest);
        verify(transactionCreator, times(1)).createTransactionWithdrawal(withdrawRequest,
                "SUCCESS");
        verify(transactionRepository, times(1)).save(successWithdrawal);
        verify(transactionRepository, never()).save(failedWithdrawal);
    }

    @Test
    void processWithdraw_Failure_ShouldSaveFailedTransactionAndPropagateError() {

        TransactionException clientException = new TransactionException(
                "Fondos insuficientes",
                "W002",
                HttpStatus.BAD_REQUEST
        );

        doThrow(clientException).when(accountsClient).withdraw(withdrawRequest);

        when(transactionCreator.createTransactionWithdrawal(withdrawRequest, "FAILED"))
                .thenReturn(failedWithdrawal);
        when(transactionRepository.save(failedWithdrawal))
                .thenReturn(Mono.just(failedWithdrawal));

        StepVerifier.create(transactionService.processWithdraw(withdrawRequest))
                .verifyErrorSatisfies(error -> {
                    assertTrue(error instanceof TransactionException);
                    assertEquals("Fondos insuficientes", error.getMessage());
                });

        verify(accountsClient, times(1)).withdraw(withdrawRequest);
        verify(transactionCreator, times(1)).createTransactionWithdrawal(withdrawRequest,
                "FAILED");
        verify(transactionRepository, times(1)).save(failedWithdrawal);
        verify(transactionRepository, never()).save(successWithdrawal);
    }

    @Test
    void getTransactionHistory_NoFilters_ShouldReturnAllTransactions() {
        Transaction t1 = successWithdrawal;
        Transaction t2 = failedTransaction;

        when(transactionRepository.findAll())
                .thenReturn(Flux.just(t1, t2));

        Flux<Transaction> resultFlux = transactionService.getTransactionHistory(null, null);

        StepVerifier.create(resultFlux)
                .expectNext(t1, t2)
                .verifyComplete();

        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void getTransactionHistory_FilterByStateAndType_ShouldReturnOnlyFilteredTransaction() {
        Transaction t1 = successWithdrawal;
        Transaction t2 = failedTransaction;
        Transaction t3 = successTransaction;

        when(transactionRepository.findAll())
                .thenReturn(Flux.just(t1, t2, t3));

        Flux<Transaction> resultFlux = transactionService.getTransactionHistory("SUCCESS", "DEPOSIT");

        StepVerifier.create(resultFlux)
                .expectNext(t3)
                .verifyComplete();

        verify(transactionRepository, times(1)).findAll();
    }
}
