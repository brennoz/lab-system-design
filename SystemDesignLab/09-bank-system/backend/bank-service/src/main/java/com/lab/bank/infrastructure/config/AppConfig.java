package com.lab.bank.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lab.bank.application.saga.TransferSagaOrchestrator;
import com.lab.bank.application.usecase.*;
import com.lab.bank.domain.port.*;
import com.lab.bank.infrastructure.outbox.*;
import com.lab.bank.infrastructure.persistence.*;
import com.lab.bank.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableScheduling
public class AppConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public JwtService jwtService() {
        return new JwtService(jwtSecret, jwtExpirationMs);
    }

    @Bean
    public AccountRepository accountRepository(SpringDataAccountRepository delegate) {
        return new JpaAccountRepository(delegate);
    }

    @Bean
    public AccountEventRepository accountEventRepository(SpringDataAccountEventRepository delegate) {
        return new JpaAccountEventRepository(delegate);
    }

    @Bean
    public TransferRepository transferRepository(SpringDataTransferRepository delegate) {
        return new JpaTransferRepository(delegate);
    }

    @Bean
    public UserRepository userRepository(SpringDataUserRepository delegate) {
        return new JpaUserRepository(delegate);
    }

    @Bean
    public OutboxPort outboxPort(SpringDataOutboxRepository delegate) {
        return new JpaOutboxRepository(delegate);
    }

    @Bean
    public OutboxPoller outboxPoller(OutboxPort outboxPort, KafkaTemplate<String, String> kafka) {
        return new OutboxPoller(outboxPort, kafka);
    }

    @Bean
    public TransferSagaOrchestrator transferSagaOrchestrator(AccountRepository accountRepository,
                                                              AccountEventRepository accountEventRepository,
                                                              TransferRepository transferRepository,
                                                              OutboxPort outboxPort,
                                                              ObjectMapper mapper,
                                                              PlatformTransactionManager txManager) {
        return new TransferSagaOrchestrator(accountRepository, accountEventRepository,
                transferRepository, outboxPort, mapper, new TransactionTemplate(txManager));
    }

    @Bean
    public CreateAccountUseCase createAccountUseCase(AccountRepository accountRepository) {
        return new CreateAccountUseCase(accountRepository);
    }

    @Bean
    public DepositUseCase depositUseCase(AccountRepository accountRepository,
                                         AccountEventRepository accountEventRepository) {
        return new DepositUseCase(accountRepository, accountEventRepository);
    }

    @Bean
    public WithdrawUseCase withdrawUseCase(AccountRepository accountRepository,
                                           AccountEventRepository accountEventRepository) {
        return new WithdrawUseCase(accountRepository, accountEventRepository);
    }

    @Bean
    public InitiateTransferUseCase initiateTransferUseCase(TransferRepository transferRepository,
                                                            TransferSagaOrchestrator orchestrator) {
        return new InitiateTransferUseCase(transferRepository, orchestrator);
    }

    @Bean
    public GetAccountUseCase getAccountUseCase(AccountRepository accountRepository,
                                               AccountEventRepository accountEventRepository) {
        return new GetAccountUseCase(accountRepository, accountEventRepository);
    }

    @Bean
    public GetTransferUseCase getTransferUseCase(TransferRepository transferRepository) {
        return new GetTransferUseCase(transferRepository);
    }
}
