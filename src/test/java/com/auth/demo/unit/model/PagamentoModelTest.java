package com.auth.demo.unit.model;

import com.auth.demo.enums.StatusPagamento;
import com.auth.demo.model.PagamentoModel;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoModelTest {

    @Test
    void deveConfirmarPagamento_QuandoStatusForPendente() {
        // Arrange
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setStatus(StatusPagamento.PENDENTE);

        // Act
        pagamento.confirmar();

        // Assert
        assertEquals(StatusPagamento.CONFIRMADO, pagamento.getStatus());
        assertNotNull(pagamento.getConfirmedAt());
    }

    @Test
    void deveLancarExcecao_QuandoConfirmarPagamentoNaoPendente() {
        // Arrange
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setStatus(StatusPagamento.CONFIRMADO);

        // Act & Assert
        assertThrows(IllegalStateException.class, pagamento::confirmar);
    }

    @Test
    void deveCancelarPagamento_QuandoStatusForPendente() {
        // Arrange
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setStatus(StatusPagamento.PENDENTE);

        // Act
        pagamento.cancelar();

        // Assert
        assertEquals(StatusPagamento.CANCELADO, pagamento.getStatus());
        assertNotNull(pagamento.getCanceledAt());
    }

    @Test
    void deveLancarExcecao_QuandoCancelarPagamentoNaoPendente() {
        // Arrange
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setStatus(StatusPagamento.CONFIRMADO);

        // Act & Assert
        assertThrows(IllegalStateException.class, pagamento::cancelar);
    }

    @Test
    void deveDefinirCreatedAtEStatus_QuandoPrePersist() {
        // Arrange
        PagamentoModel pagamento = new PagamentoModel();

        // Act
        pagamento.prePersist();

        // Assert
        assertNotNull(pagamento.getCreatedAt());
        assertEquals(StatusPagamento.PENDENTE, pagamento.getStatus());
    }

    @Test
    void naoDeveSobrescreverCreatedAt_SeJaExistir() {
        // Arrange
        PagamentoModel pagamento = new PagamentoModel();
        OffsetDateTime dataExistente = OffsetDateTime.now().minusDays(1);

        pagamento.setCreatedAt(dataExistente);

        // Act
        pagamento.prePersist();

        // Assert
        assertEquals(dataExistente, pagamento.getCreatedAt());
    }
}