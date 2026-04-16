package com.auth.demo.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.auth.demo.enums.MetodoPagamento;
import com.auth.demo.enums.StatusPagamento;

@Entity
@Table(name = "pagamento")
@Getter
@Setter
@NoArgsConstructor
public class PagamentoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O valor do pagamento é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor mínimo do pagamento é 0.01")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal valor;

    @NotNull(message = "O método de pagamento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPagamento metodo;

    //@NotNull(message = "O status do pagamento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    //@NotNull(message = "A data de criação é obrigatória")
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt;

    @Column(name = "mp_payment_id", unique = true)
    private Long mpPaymentId;

    //@NotNull(message = "O usuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UserModel user;



    // Construtor com campos obrigatórios
    public PagamentoModel(BigDecimal valor, MetodoPagamento metodo, UserModel user) {
        this.valor = valor;
        this.metodo = metodo;
        this.user = user;
    }

    public void confirmar() {
        if (this.status == StatusPagamento.PENDENTE) {
            this.status = StatusPagamento.CONFIRMADO;
            this.confirmedAt = OffsetDateTime.now();
        } else {
            throw new IllegalStateException("Apenas pagamentos pendentes podem ser confirmados");
        }
    } 

    public void cancelar() {
        if (this.status == StatusPagamento.PENDENTE) {
            this.status = StatusPagamento.CANCELADO;
            this.canceledAt = OffsetDateTime.now();
        } else {
                throw new IllegalStateException("Apenas pagamentos pendentes podem ser cancelados");
        }
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }

        if (this.status == null) {
            this.status = StatusPagamento.PENDENTE;
        }
    }
}