package com.example.backend.beneficio;

import com.example.backend.beneficio.dto.TransferRequest;
import com.example.ejb.Beneficio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TransferConcurrencyIT {

    private static final BigDecimal SALDO_INICIAL_A = new BigDecimal("1000.00");
    private static final BigDecimal SALDO_INICIAL_B = new BigDecimal("500.00");

    @Autowired BeneficioRepository repository;
    @Autowired BeneficioService service;

    @BeforeEach
    void resetBalances() {
        Beneficio a = repository.findById(1L).orElseThrow();
        a.setValor(SALDO_INICIAL_A);
        Beneficio b = repository.findById(2L).orElseThrow();
        b.setValor(SALDO_INICIAL_B);
        repository.saveAll(List.of(a, b));
    }

    @Test
    void concurrentCrossTransfersPreserveTotalBalance() throws Exception {
        int totalTransfers = 200;
        BigDecimal amount = new BigDecimal("1.00");
        BigDecimal totalAntes = SALDO_INICIAL_A.add(SALDO_INICIAL_B);

        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger completas = new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < totalTransfers; i++) {
            final boolean direction = i % 2 == 0;
            futures.add(pool.submit(() -> {
                try {
                    start.await();
                    TransferRequest req = new TransferRequest();
                    if (direction) {
                        req.setFromId(1L);
                        req.setToId(2L);
                    } else {
                        req.setFromId(2L);
                        req.setToId(1L);
                    }
                    req.setAmount(amount);
                    try {
                        service.transfer(req);
                    } catch (Exception expectedDuringContention) {
                    }
                    completas.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        start.countDown();
        for (Future<?> f : futures) {
            f.get(20, TimeUnit.SECONDS);
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS),
                "pool não encerrou em tempo hábil — possível deadlock");

        Beneficio finalA = repository.findById(1L).orElseThrow();
        Beneficio finalB = repository.findById(2L).orElseThrow();
        BigDecimal totalDepois = finalA.getValor().add(finalB.getValor());

        assertEquals(0, totalAntes.compareTo(totalDepois),
                "saldo total deve ser invariante (antes=" + totalAntes + " depois=" + totalDepois + ")");
        assertTrue(finalA.getValor().signum() >= 0,
                "saldo de A não pode ser negativo: " + finalA.getValor());
        assertTrue(finalB.getValor().signum() >= 0,
                "saldo de B não pode ser negativo: " + finalB.getValor());
        assertEquals(totalTransfers, completas.get(),
                "todas as transferências devem ter terminado");
    }

    @Test
    void concurrentDebitsNeverProduceNegativeBalance() throws Exception {
        int totalTransfers = 100;
        BigDecimal amount = SALDO_INICIAL_A.divide(new BigDecimal("10"));

        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger falhasControladas = new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < totalTransfers; i++) {
            futures.add(pool.submit(() -> {
                try {
                    start.await();
                    TransferRequest req = new TransferRequest();
                    req.setFromId(1L);
                    req.setToId(2L);
                    req.setAmount(amount);
                    try {
                        service.transfer(req);
                        sucessos.incrementAndGet();
                    } catch (Exception expected) {
                        falhasControladas.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        start.countDown();
        for (Future<?> f : futures) {
            f.get(20, TimeUnit.SECONDS);
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS),
                "pool não encerrou — possível deadlock");

        Beneficio finalA = repository.findById(1L).orElseThrow();
        assertTrue(finalA.getValor().signum() >= 0,
                "saldo de A não pode ficar negativo (resultou em " + finalA.getValor() + ")");
        assertTrue(falhasControladas.get() > 0,
                "ao menos uma transferência deveria ter sido rejeitada por saldo insuficiente");
        assertTrue(sucessos.get() > 0,
                "ao menos uma transferência deveria ter passado");
    }
}
