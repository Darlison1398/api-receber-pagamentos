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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.security.test.context.support.WithMockUser;

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
    // 🔐 SEGURANÇA
    // =========================

    @Test
    void deveRetornar403_QuandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/pagamentos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveNegarAcesso_QuandoRoleErrada() throws Exception {
        mockMvc.perform(get("/pagamentos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void devePermitirAcesso_QuandoRoleCorreta() throws Exception {
        mockMvc.perform(get("/pagamentos"))
                .andExpect(status().isOk());
    }

    // =========================
    // ✅ CRIAR PAGAMENTO
    // =========================

    @Test
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
    void deveRetornarErro_QuandoValorInvalido() throws Exception {
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.ZERO);
        pagamento.setMetodo(MetodoPagamento.PIX);

        mockMvc.perform(post("/pagamentos/criar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagamento)))
                .andExpect(status().isBadRequest());
    }

    // =========================
    // 📄 LISTAR
    // =========================

    @Test
    @WithMockUser(roles = "USER")
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
    // 🔍 BUSCAR
    // =========================

    @Test
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
    void deveRetornar404_QuandoPagamentoNaoExiste() throws Exception {
        mockMvc.perform(get("/pagamentos/999"))
                .andExpect(status().isNotFound());
    }

    // =========================
    // 🔐 ACESSO ENTRE USUÁRIOS
    // =========================

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarAcesso_QuandoPagamentoNaoPertenceAoUsuario() throws Exception {

        UserModel dono = new UserModel();
        dono.setId(1L);

        UserModel outro = new UserModel();
        outro.setId(2L);

        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.TEN);
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setUser(dono);

        pagamento = pagamentoRepository.save(pagamento);

        when(userService.getUsuarioLogado()).thenReturn(outro);

        mockMvc.perform(get("/pagamentos/" + pagamento.getId()))
                .andExpect(status().isNotFound());
    }

    // =========================
    // 🔄 CONFIRMAR
    // =========================

    @Test
    @WithMockUser(roles = "USER")
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
    @WithMockUser(roles = "USER")
    void deveRetornarErro_QuandoConfirmarPagamentoJaConfirmado() throws Exception {
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.TEN); // ✅ obrigatório
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setUser(usuario);
        pagamento.setStatus(StatusPagamento.CONFIRMADO);

        pagamento = pagamentoRepository.save(pagamento);

        mockMvc.perform(put("/pagamentos/" + pagamento.getId() + "/confirmar"))
                .andExpect(status().isBadRequest());
    }

    // =========================
    // ❌ CANCELAR
    // =========================

    @Test
    @WithMockUser(roles = "USER")
    void deveCancelarPagamento() throws Exception {
        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setValor(BigDecimal.TEN); // ✅ obrigatório
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setUser(usuario);
        pagamento.setStatus(StatusPagamento.PENDENTE);

        pagamento = pagamentoRepository.save(pagamento);

        mockMvc.perform(put("/pagamentos/" + pagamento.getId() + "/cancelar"))
                .andExpect(status().isOk());
    }

    // =========================
    // 🔍 FILTROS
    // =========================

    @Test
    @WithMockUser(roles = "USER")
    void deveFiltrarPorStatus() throws Exception {
        mockMvc.perform(get("/pagamentos/status/PENDENTE"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveFiltrarPorMetodo() throws Exception {
        mockMvc.perform(get("/pagamentos/metodo/PIX"))
                .andExpect(status().isOk());
    }
}