package com.lab.bank.api.dto;

import com.lab.bank.domain.model.Account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(UUID accountId, String ownerId, BigDecimal balance, long version) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.id(), a.ownerId(), a.balance(), a.version());
    }
}
