package com.lab.bank.api;

import com.lab.bank.api.dto.AccountResponse;
import com.lab.bank.api.dto.AmountRequest;
import com.lab.bank.application.usecase.CreateAccountUseCase;
import com.lab.bank.application.usecase.DepositUseCase;
import com.lab.bank.application.usecase.GetAccountUseCase;
import com.lab.bank.application.usecase.WithdrawUseCase;
import com.lab.bank.domain.model.AccountEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase,
                             GetAccountUseCase getAccountUseCase,
                             DepositUseCase depositUseCase,
                             WithdrawUseCase withdrawUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
        this.depositUseCase = depositUseCase;
        this.withdrawUseCase = withdrawUseCase;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountResponse.from(createAccountUseCase.create(auth.getName())));
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable UUID id) {
        return AccountResponse.from(getAccountUseCase.getAccount(id));
    }

    @GetMapping("/{id}/events")
    public List<AccountEvent> events(@PathVariable UUID id) {
        return getAccountUseCase.getEvents(id);
    }

    @PostMapping("/{id}/deposit")
    public AccountEvent deposit(@PathVariable UUID id, @RequestBody AmountRequest req) {
        return depositUseCase.deposit(id, req.amount());
    }

    @PostMapping("/{id}/withdraw")
    public AccountEvent withdraw(@PathVariable UUID id, @RequestBody AmountRequest req) {
        return withdrawUseCase.withdraw(id, req.amount());
    }
}
