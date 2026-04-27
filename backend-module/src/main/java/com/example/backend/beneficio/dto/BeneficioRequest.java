package com.example.backend.beneficio.dto;

import com.example.ejb.Beneficio;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class BeneficioRequest {

    @NotBlank
    @Size(max = 100)
    private String nome;

    @Size(max = 255)
    private String descricao;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal valor;

    private Boolean ativo;

    public Beneficio toEntity() {
        return new Beneficio(nome, descricao, valor, ativo == null ? Boolean.TRUE : ativo);
    }

    public void applyTo(Beneficio entity) {
        entity.setNome(nome);
        entity.setDescricao(descricao);
        entity.setValor(valor);
        if (ativo != null) {
            entity.setAtivo(ativo);
        }
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
