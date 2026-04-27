package com.example.backend.beneficio;

import com.example.backend.beneficio.dto.BeneficioRequest;
import com.example.backend.beneficio.dto.BeneficioResponse;
import com.example.backend.beneficio.dto.TransferRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/beneficios")
@Tag(name = "Beneficios", description = "CRUD de benefícios e transferência de valor entre eles")
public class BeneficioController {

    private final BeneficioService service;

    public BeneficioController(BeneficioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todos os benefícios")
    public List<BeneficioResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um benefício por id")
    public BeneficioResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Cria um novo benefício")
    public ResponseEntity<BeneficioResponse> create(@Valid @RequestBody BeneficioRequest body) {
        BeneficioResponse created = service.create(body);
        return ResponseEntity.created(URI.create("/api/v1/beneficios/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um benefício existente")
    public BeneficioResponse update(@PathVariable Long id, @Valid @RequestBody BeneficioRequest body) {
        return service.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um benefício")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Transfere valor entre dois benefícios (origem/destino por id OU nome; valida saldo e usa locking)")
    public void transfer(@Valid @RequestBody TransferRequest req) {
        service.transfer(req);
    }
}
