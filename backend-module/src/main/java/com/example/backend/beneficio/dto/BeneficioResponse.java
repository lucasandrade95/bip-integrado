package com.example.backend.beneficio.dto;

import com.example.ejb.Beneficio;
import java.math.BigDecimal;

public class BeneficioResponse {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal valor;
    private Boolean ativo;
    private Long version;

    public BeneficioResponse() {
    }

    public static BeneficioResponse from(Beneficio b) {
        BeneficioResponse dto = new BeneficioResponse();
        dto.id = b.getId();
        dto.nome = b.getNome();
        dto.descricao = b.getDescricao();
        dto.valor = b.getValor();
        dto.ativo = b.getAtivo();
        dto.version = b.getVersion();
        return dto;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public Boolean getAtivo() { return ativo; }
    public Long getVersion() { return version; }
}
