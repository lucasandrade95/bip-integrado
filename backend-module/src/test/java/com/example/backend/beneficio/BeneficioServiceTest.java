package com.example.backend.beneficio;

import com.example.backend.beneficio.dto.BeneficioRequest;
import com.example.backend.beneficio.dto.BeneficioResponse;
import com.example.backend.beneficio.dto.TransferRequest;
import com.example.backend.beneficio.exception.BeneficioNotFoundException;
import com.example.ejb.Beneficio;
import com.example.ejb.BeneficioEjbService;
import com.example.ejb.TransferException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficioServiceTest {

    @Mock BeneficioRepository repository;

    private final RecordingEjbService ejbService = new RecordingEjbService();

    private BeneficioService newService() {
        return new BeneficioService(repository, ejbService);
    }

    @Test
    void deleteRejectsMissingId() {
        when(repository.existsById(99L)).thenReturn(false);
        assertThrows(BeneficioNotFoundException.class, () -> newService().delete(99L));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void createPersistsAndReturnsResponse() {
        BeneficioRequest request = new BeneficioRequest();
        request.setNome("X");
        request.setValor(new BigDecimal("1.00"));
        Beneficio saved = request.toEntity();
        saved.setId(42L);
        when(repository.save(any(Beneficio.class))).thenReturn(saved);

        BeneficioResponse out = newService().create(request);
        assertEquals(42L, out.getId());
    }

    @Test
    void transferByIdsDelegatesToEjb() {
        TransferRequest req = new TransferRequest();
        req.setFromId(1L);
        req.setToId(2L);
        req.setAmount(new BigDecimal("10.00"));

        newService().transfer(req);

        assertEquals(1L, ejbService.fromId);
        assertEquals(2L, ejbService.toId);
        assertEquals(new BigDecimal("10.00"), ejbService.amount);
    }

    @Test
    void transferByNamesResolvesIdsAndDelegates() {
        Beneficio a = new Beneficio("Beneficio A", null, new BigDecimal("100"), Boolean.TRUE);
        a.setId(7L);
        Beneficio b = new Beneficio("Beneficio B", null, new BigDecimal("100"), Boolean.TRUE);
        b.setId(9L);
        when(repository.findByNome("Beneficio A")).thenReturn(List.of(a));
        when(repository.findByNome("Beneficio B")).thenReturn(List.of(b));

        TransferRequest req = new TransferRequest();
        req.setFromName("Beneficio A");
        req.setToName("Beneficio B");
        req.setAmount(new BigDecimal("5.00"));

        newService().transfer(req);

        assertEquals(7L, ejbService.fromId);
        assertEquals(9L, ejbService.toId);
    }

    @Test
    void transferRejectsAmbiguousName() {
        Beneficio a1 = new Beneficio("Repetido", null, new BigDecimal("1"), Boolean.TRUE); a1.setId(1L);
        Beneficio a2 = new Beneficio("Repetido", null, new BigDecimal("1"), Boolean.TRUE); a2.setId(2L);
        when(repository.findByNome("Repetido")).thenReturn(List.of(a1, a2));

        TransferRequest req = new TransferRequest();
        req.setFromName("Repetido");
        req.setToId(99L);
        req.setAmount(new BigDecimal("1.00"));

        assertThrows(TransferException.class, () -> newService().transfer(req));
        assertEquals(null, ejbService.amount);
    }

    @Test
    void transferRejectsUnknownName() {
        when(repository.findByNome("Inexistente")).thenReturn(List.of());

        TransferRequest req = new TransferRequest();
        req.setFromName("Inexistente");
        req.setToId(2L);
        req.setAmount(new BigDecimal("1.00"));

        assertThrows(TransferException.class, () -> newService().transfer(req));
        assertEquals(null, ejbService.amount);
    }

    @Test
    void transferRejectsWhenNeitherIdNorNameProvided() {
        TransferRequest req = new TransferRequest();
        req.setToId(2L);
        req.setAmount(new BigDecimal("1.00"));

        assertThrows(TransferException.class, () -> newService().transfer(req));
        assertEquals(null, ejbService.amount);
    }

    private static final class RecordingEjbService extends BeneficioEjbService {
        Long fromId;
        Long toId;
        BigDecimal amount;

        @Override
        public void transfer(Long fromId, Long toId, BigDecimal amount) {
            this.fromId = fromId;
            this.toId = toId;
            this.amount = amount;
        }
    }
}
