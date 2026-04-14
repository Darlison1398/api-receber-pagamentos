package com.auth.demo.integration.repository;

import com.auth.demo.enums.MetodoPagamento;
import com.auth.demo.enums.StatusPagamento;
import com.auth.demo.model.PagamentoModel;
import com.auth.demo.model.UserModel;
import com.auth.demo.repository.PagamentoRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PagamentoRepositoryTest {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    // =========================
    // 🧪 HELPERS
    // =========================

    private UserModel criarUsuario(Long id) {
        UserModel user = new UserModel();
        user.setId(id);
        return entityManager.persistAndFlush(user);
    }

    private PagamentoModel criarPagamento(UserModel user, StatusPagamento status, MetodoPagamento metodo) {
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setUser(user);
        pagamento.setValor(BigDecimal.TEN);
        pagamento.setMetodo(metodo);
        pagamento.setStatus(status);
        return entityManager.persistAndFlush(pagamento);
    }

    // =========================
    // ✅ findByUserId
    // =========================

    @Test
    @DisplayName("Deve retornar pagamentos do usuário")
    void deveRetornarPagamentosDoUsuario() {
        UserModel user = criarUsuario(1L);

        criarPagamento(user, StatusPagamento.PENDENTE, MetodoPagamento.PIX);
        criarPagamento(user, StatusPagamento.CONFIRMADO, MetodoPagamento.CARTAO);

        List<PagamentoModel> lista = pagamentoRepository.findByUserId(user.getId());

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não possui pagamentos")
    void deveRetornarListaVazia_QuandoUsuarioSemPagamentos() {
        UserModel user = criarUsuario(2L);

        List<PagamentoModel> lista = pagamentoRepository.findByUserId(user.getId());

        assertTrue(lista.isEmpty());
    }

    @Test
    @DisplayName("Não deve retornar pagamentos de outro usuário")
    void naoDeveMisturarPagamentosDeUsuarios() {
        UserModel user1 = criarUsuario(1L);
        UserModel user2 = criarUsuario(2L);

        criarPagamento(user1, StatusPagamento.PENDENTE, MetodoPagamento.PIX);
        criarPagamento(user2, StatusPagamento.PENDENTE, MetodoPagamento.PIX);

        List<PagamentoModel> lista = pagamentoRepository.findByUserId(user1.getId());

        assertEquals(1, lista.size());
    }

    // =========================
    // ✅ findByUserIdAndStatus
    // =========================

    @Test
    void deveFiltrarPorStatus() {
        UserModel user = criarUsuario(1L);

        criarPagamento(user, StatusPagamento.PENDENTE, MetodoPagamento.PIX);
        criarPagamento(user, StatusPagamento.CONFIRMADO, MetodoPagamento.PIX);

        List<PagamentoModel> lista =
                pagamentoRepository.findByUserIdAndStatus(user.getId(), StatusPagamento.PENDENTE);

        assertEquals(1, lista.size());
        assertEquals(StatusPagamento.PENDENTE, lista.get(0).getStatus());
    }

    @Test
    void deveRetornarVazio_QuandoNaoExistirStatus() {
        UserModel user = criarUsuario(1L);

        criarPagamento(user, StatusPagamento.CONFIRMADO, MetodoPagamento.PIX);

        List<PagamentoModel> lista =
                pagamentoRepository.findByUserIdAndStatus(user.getId(), StatusPagamento.PENDENTE);

        assertTrue(lista.isEmpty());
    }

    // =========================
    // ✅ findByUserIdAndMetodo
    // =========================

    @Test
    void deveFiltrarPorMetodo() {
        UserModel user = criarUsuario(1L);

        criarPagamento(user, StatusPagamento.PENDENTE, MetodoPagamento.PIX);
        criarPagamento(user, StatusPagamento.PENDENTE, MetodoPagamento.CARTAO);

        List<PagamentoModel> lista =
                pagamentoRepository.findByUserIdAndMetodo(user.getId(), MetodoPagamento.PIX);

        assertEquals(1, lista.size());
        assertEquals(MetodoPagamento.PIX, lista.get(0).getMetodo());
    }

    @Test
    void deveRetornarVazio_QuandoMetodoNaoExistir() {
        UserModel user = criarUsuario(1L);

        criarPagamento(user, StatusPagamento.PENDENTE, MetodoPagamento.CARTAO);

        List<PagamentoModel> lista =
                pagamentoRepository.findByUserIdAndMetodo(user.getId(), MetodoPagamento.PIX);

        assertTrue(lista.isEmpty());
    }
}