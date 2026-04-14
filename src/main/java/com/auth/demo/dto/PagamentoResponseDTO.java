package com.auth.demo.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.auth.demo.enums.MetodoPagamento;
import com.auth.demo.enums.StatusPagamento;
import com.auth.demo.model.PagamentoModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagamentoResponseDTO {

    private Long id;
    private BigDecimal valor;
    private MetodoPagamento metodo;
    private StatusPagamento status;
    private OffsetDateTime createdAt;

    public PagamentoResponseDTO(PagamentoModel pagamento) {
        this.id = pagamento.getId();
        this.valor = pagamento.getValor();
        this.metodo = pagamento.getMetodo();
        this.status = pagamento.getStatus();
        this.createdAt = pagamento.getCreatedAt();
    }

}