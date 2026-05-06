package com.lab.bank.application.usecase;

import com.lab.bank.domain.model.Transfer;
import com.lab.bank.domain.port.TransferRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class GetTransferUseCase {

    private final TransferRepository transferRepository;

    public GetTransferUseCase(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    public Transfer getTransfer(UUID id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found: " + id));
    }
}
