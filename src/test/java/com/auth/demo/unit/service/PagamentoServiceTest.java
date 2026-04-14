package com.auth.demo.unit.service;

import com.auth.demo.enums.MetodoPagamento;
import com.auth.demo.enums.StatusPagamento;
import com.auth.demo.model.PagamentoModel;
import com.auth.demo.model.UserModel;
import com.auth.demo.repository.PagamentoRepository;
import com.auth.demo.service.PagamentoService;
import com.auth.demo.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PagamentoService pagamentoService;

    private UserModel usuario;

    @BeforeEach
    void setup() {
        usuario = new UserModel();
        usuario.setId(1L);
    }

    // =========================
    // ✅ CRIAR PAGAMENTO
    // =========================

    @Test
    void deveCriarPagamentoComSucesso() {
        when(userService.getUsuarioLogado()).thenReturn(usuario);

        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.TEN);
        pagamento.setMetodo(MetodoPagamento.PIX);

        pagamentoService.criarPagamento(pagamento);

        assertEquals(usuario, pagamento.getUser());
        verify(pagamentoRepository).save(pagamento);
    }

    @Test
    void deveLancarExcecao_QuandoValorForZero() {
        when(userService.getUsuarioLogado()).thenReturn(usuario);

        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.ZERO);
        pagamento.setMetodo(MetodoPagamento.PIX);

        assertThrows(IllegalArgumentException.class,
                () -> pagamentoService.criarPagamento(pagamento));

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoMetodoForNulo() {
        when(userService.getUsuarioLogado()).thenReturn(usuario);

        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.TEN);

        assertThrows(IllegalArgumentException.class,
                () -> pagamentoService.criarPagamento(pagamento));

        verify(pagamentoRepository, never()).save(any());
    }

    // =========================
    // 🔐 BUSCAR POR ID
    // =========================

    @Test
    void deveRetornarPagamento_QuandoPertencerAoUsuario() {
        when(userService.getUsuarioLogado()).thenReturn(usuario);

        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setUser(usuario);

        when(pagamentoRepository.findById(1L))
                .thenReturn(Optional.of(pagamento));

        PagamentoModel resultado = pagamentoService.buscarPorId(1L);

        assertNotNull(resultado);
    }

    @Test
    void deveLancarExcecao_QuandoPagamentoNaoEncontrado() {
        when(userService.getUsuarioLogado()).thenReturn(usuario);

        when(pagamentoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> pagamentoService.buscarPorId(1L));
    }

    @Test
    void deveLancarExcecao_QuandoPagamentoNaoPertenceAoUsuario() {
        UserModel outroUsuario = new UserModel();
        outroUsuario.setId(2L);

        when(userService.getUsuarioLogado()).thenReturn(usuario);

        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setUser(outroUsuario);

        when(pagamentoRepository.findById(1L))
                .thenReturn(Optional.of(pagamento));

        assertThrows(IllegalStateException.class,
                () -> pagamentoService.buscarPorId(1L));
    }

    // =========================
    // 🔄 CONFIRMAR PAGAMENTO
    // =========================

    @Test
    void deveConfirmarPagamentoComSucesso() {
        when(userService.getUsuarioLogado()).thenReturn(usuario);

        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setUser(usuario);
        pagamento.setStatus(StatusPagamento.PENDENTE);

        when(pagamentoRepository.findById(1L))
                .thenReturn(Optional.of(pagamento));

        pagamentoService.confirmarPagamento(1L);

        assertEquals(StatusPagamento.CONFIRMADO, pagamento.getStatus());
        verify(pagamentoRepository).save(pagamento);
    }

    @Test
    void deveLancarExcecao_QuandoConfirmarPagamentoJaConfirmado() {
        when(userService.getUsuarioLogado()).thenReturn(usuario);

        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setUser(usuario);
        pagamento.setStatus(StatusPagamento.CONFIRMADO);

        when(pagamentoRepository.findById(1L))
                .thenReturn(Optional.of(pagamento));

        assertThrows(IllegalStateException.class,
                () -> pagamentoService.confirmarPagamento(1L));
    }

    // =========================
    // ❌ CANCELAR PAGAMENTO
    // =========================

    @Test
    void deveCancelarPagamentoComSucesso() {
        when(userService.getUsuarioLogado()).thenReturn(usuario);

        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setUser(usuario);
        pagamento.setStatus(StatusPagamento.PENDENTE);

        when(pagamentoRepository.findById(1L))
                .thenReturn(Optional.of(pagamento));

        pagamentoService.cancelarPagamento(1L);

        assertEquals(StatusPagamento.CANCELADO, pagamento.getStatus());
        verify(pagamentoRepository).save(pagamento);
    }

    @Test
    void deveLancarExcecao_QuandoCancelarPagamentoJaConfirmado() {
        when(userService.getUsuarioLogado()).thenReturn(usuario);

        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setUser(usuario);
        pagamento.setStatus(StatusPagamento.CONFIRMADO);

        when(pagamentoRepository.findById(1L))
                .thenReturn(Optional.of(pagamento));

        assertThrows(IllegalStateException.class,
                () -> pagamentoService.cancelarPagamento(1L));
    }

    // =========================
    // 🔍 FILTROS
    // =========================

    @Test
    void deveListarPagamentosPorStatus() {
        when(userService.getUsuarioLogado()).thenReturn(usuario);

        when(pagamentoRepository.findByUserIdAndStatus(1L, StatusPagamento.PENDENTE))
                .thenReturn(List.of(new PagamentoModel()));

        var lista = pagamentoService.listarPorStatus(StatusPagamento.PENDENTE);

        assertFalse(lista.isEmpty());
    }

    @Test
    void deveListarPagamentosPorMetodo() {
        when(userService.getUsuarioLogado()).thenReturn(usuario);

        when(pagamentoRepository.findByUserIdAndMetodo(1L, MetodoPagamento.PIX))
                .thenReturn(List.of(new PagamentoModel()));

        var lista = pagamentoService.listarPorMetodo(MetodoPagamento.PIX);

        assertFalse(lista.isEmpty());
    }
}