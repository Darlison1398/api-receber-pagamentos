package com.auth.demo.e2e.controller;

import com.auth.demo.enums.MetodoPagamento;
import com.auth.demo.enums.StatusPagamento;
import com.auth.demo.model.PagamentoModel;
import com.auth.demo.model.UserModel;
import com.auth.demo.repository.PagamentoRepository;
import com.auth.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserModel usuario;

    @BeforeEach
    void setup() {
        pagamentoRepository.deleteAll();

        usuario = new UserModel();
        usuario.setId(1L);

        when(userService.getUsuarioLogado()).thenReturn(usuario);
    }

    // =========================
    // ✅ CRIAR PAGAMENTO
    // =========================

    @Test
    void deveCriarPagamentoComSucesso() throws Exception {
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.TEN);
        pagamento.setMetodo(MetodoPagamento.PIX);

        mockMvc.perform(post("/pagamentos/criar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagamento)))
                .andExpect(status().isCreated());
    }

    @Test
    void deveRetornarErro_QuandoValorInvalido() throws Exception {
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.ZERO);
        pagamento.setMetodo(MetodoPagamento.PIX);

        mockMvc.perform(post("/pagamentos/criar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagamento)))
                .andExpect(status().is5xxServerError());
    }

    // =========================
    // ✅ LISTAR PAGAMENTOS
    // =========================

    @Test
    void deveListarPagamentos() throws Exception {
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.TEN);
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setUser(usuario);

        pagamentoRepository.save(pagamento);

        mockMvc.perform(get("/pagamentos"))
                .andExpect(status().isOk());
    }

    // =========================
    // 🔍 BUSCAR POR ID
    // =========================

    @Test
    void deveBuscarPagamentoPorId() throws Exception {
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.TEN);
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setUser(usuario);

        pagamento = pagamentoRepository.save(pagamento);

        mockMvc.perform(get("/pagamentos/" + pagamento.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornarErro_QuandoPagamentoNaoExiste() throws Exception {
        mockMvc.perform(get("/pagamentos/999"))
                .andExpect(status().isNotFound());
    }

    // =========================
    // 🔄 CONFIRMAR PAGAMENTO
    // =========================

    @Test
    void deveConfirmarPagamento() throws Exception {
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.TEN);
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setUser(usuario);
        pagamento.setStatus(StatusPagamento.PENDENTE);

        pagamento = pagamentoRepository.save(pagamento);

        mockMvc.perform(put("/pagamentos/" + pagamento.getId() + "/confirmar"))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornarErro_QuandoConfirmarPagamentoJaConfirmado() throws Exception {
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.TEN);
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setUser(usuario);
        pagamento.setStatus(StatusPagamento.CONFIRMADO);

        pagamento = pagamentoRepository.save(pagamento);

        mockMvc.perform(put("/pagamentos/" + pagamento.getId() + "/confirmar"))
                .andExpect(status().isBadRequest());
    }

    // =========================
    // ❌ CANCELAR PAGAMENTO
    // =========================

    @Test
    void deveCancelarPagamento() throws Exception {
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.TEN);
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setUser(usuario);
        pagamento.setStatus(StatusPagamento.PENDENTE);

        pagamento = pagamentoRepository.save(pagamento);

        mockMvc.perform(put("/pagamentos/" + pagamento.getId() + "/cancelar"))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornarErro_QuandoCancelarPagamentoJaConfirmado() throws Exception {
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.TEN);
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setUser(usuario);
        pagamento.setStatus(StatusPagamento.CONFIRMADO);

        pagamento = pagamentoRepository.save(pagamento);

        mockMvc.perform(put("/pagamentos/" + pagamento.getId() + "/cancelar"))
                .andExpect(status().isBadRequest());
    }

    // =========================
    // 🔍 FILTROS
    // =========================

    @Test
    void deveFiltrarPorStatus() throws Exception {
        mockMvc.perform(get("/pagamentos/status/PENDENTE"))
                .andExpect(status().isOk());
    }

    @Test
    void deveFiltrarPorMetodo() throws Exception {
        mockMvc.perform(get("/pagamentos/metodo/PIX"))
                .andExpect(status().isOk());
    }
}