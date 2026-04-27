package com.example.backend.beneficio;

import com.example.backend.beneficio.dto.BeneficioRequest;
import com.example.backend.beneficio.dto.BeneficioResponse;
import com.example.backend.beneficio.dto.TransferRequest;
import com.example.backend.beneficio.exception.BeneficioNotFoundException;
import com.example.ejb.Beneficio;
import com.example.ejb.BeneficioEjbService;
import com.example.ejb.TransferException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BeneficioService {

    private final BeneficioRepository repository;
    private final BeneficioEjbService ejbService;

    public BeneficioService(BeneficioRepository repository, BeneficioEjbService ejbService) {
        this.repository = repository;
        this.ejbService = ejbService;
    }

    @Transactional(readOnly = true)
    public List<BeneficioResponse> findAll() {
        return repository.findAll().stream().map(BeneficioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BeneficioResponse findById(Long id) {
        return repository.findById(id)
                .map(BeneficioResponse::from)
                .orElseThrow(() -> new BeneficioNotFoundException(id));
    }

    public BeneficioResponse create(BeneficioRequest request) {
        Beneficio entity = request.toEntity();
        return BeneficioResponse.from(repository.save(entity));
    }

    public BeneficioResponse update(Long id, BeneficioRequest request) {
        Beneficio entity = repository.findById(id)
                .orElseThrow(() -> new BeneficioNotFoundException(id));
        request.applyTo(entity);
        return BeneficioResponse.from(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BeneficioNotFoundException(id);
        }
        repository.deleteById(id);
    }

    public void transfer(TransferRequest req) {
        Long fromId = resolveId(req.getFromId(), req.getFromName(), "origem");
        Long toId   = resolveId(req.getToId(),   req.getToName(),   "destino");
        ejbService.transfer(fromId, toId, req.getAmount());
    }

    private Long resolveId(Long id, String nome, String label) {
        if (id != null) {
            return id;
        }
        if (nome == null || nome.isBlank()) {
            throw new TransferException("informe id ou nome para " + label);
        }
        List<Beneficio> matches = repository.findByNome(nome.trim());
        if (matches.isEmpty()) {
            throw new TransferException("benefício não encontrado para " + label + ": " + nome);
        }
        if (matches.size() > 1) {
            throw new TransferException("nome ambíguo para " + label + " (" + matches.size() + " resultados): " + nome);
        }
        return matches.get(0).getId();
    }
}
