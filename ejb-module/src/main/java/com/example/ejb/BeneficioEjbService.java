package com.example.ejb;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Objects;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class BeneficioEjbService {

    @PersistenceContext
    private EntityManager em;

    public BeneficioEjbService() {
    }

    BeneficioEjbService(EntityManager em) {
        this.em = em;
    }

    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Objects.requireNonNull(fromId, "fromId");
        Objects.requireNonNull(toId, "toId");
        Objects.requireNonNull(amount, "amount");

        if (fromId.equals(toId)) {
            throw new TransferException("origem e destino devem ser diferentes");
        }
        if (amount.signum() <= 0) {
            throw new TransferException("valor deve ser maior que zero");
        }

        Long firstId  = fromId < toId ? fromId : toId;
        Long secondId = fromId < toId ? toId   : fromId;

        Beneficio first  = lockOrThrow(firstId);
        Beneficio second = lockOrThrow(secondId);

        Beneficio from = fromId.equals(firstId) ? first  : second;
        Beneficio to   = fromId.equals(firstId) ? second : first;

        if (Boolean.FALSE.equals(from.getAtivo()) || Boolean.FALSE.equals(to.getAtivo())) {
            throw new TransferException("benefício inativo não pode participar de transferência");
        }
        if (from.getValor().compareTo(amount) < 0) {
            throw new TransferException("saldo insuficiente em " + fromId);
        }

        from.setValor(from.getValor().subtract(amount));
        to.setValor(to.getValor().add(amount));
    }

    private Beneficio lockOrThrow(Long id) {
        Beneficio b = em.find(Beneficio.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (b == null) {
            throw new TransferException("benefício não encontrado: " + id);
        }
        return b;
    }
}
