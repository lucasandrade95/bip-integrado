package com.example.ejb;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class TransferException extends RuntimeException {
    public TransferException(String message) {
        super(message);
    }
}
