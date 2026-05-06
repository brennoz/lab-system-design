package com.lab.bank.api;

import com.lab.bank.api.dto.TransferRequest;
import com.lab.bank.api.dto.TransferResponse;
import com.lab.bank.application.usecase.GetTransferUseCase;
import com.lab.bank.application.usecase.InitiateTransferUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final InitiateTransferUseCase initiateTransferUseCase;
    private final GetTransferUseCase getTransferUseCase;

    public TransferController(InitiateTransferUseCase initiateTransferUseCase,
                              GetTransferUseCase getTransferUseCase) {
        this.initiateTransferUseCase = initiateTransferUseCase;
        this.getTransferUseCase = getTransferUseCase;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> initiate(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody TransferRequest req) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(TransferResponse.from(
                initiateTransferUseCase.initiate(req.fromAccountId(), req.toAccountId(),
                        req.amount(), idempotencyKey)));
    }

    @GetMapping("/{id}")
    public TransferResponse get(@PathVariable UUID id) {
        return TransferResponse.from(getTransferUseCase.getTransfer(id));
    }
}
