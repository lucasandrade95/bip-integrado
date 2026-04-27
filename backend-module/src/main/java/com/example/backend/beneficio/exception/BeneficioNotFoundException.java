package com.example.backend.beneficio.exception;

public class BeneficioNotFoundException extends RuntimeException {

    public BeneficioNotFoundException(Long id) {
        super("benefício não encontrado: " + id);
    }
}
