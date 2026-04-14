package com.auth.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.auth.demo.dto.PagamentoResponseDTO;
import com.auth.demo.enums.MetodoPagamento;
import com.auth.demo.enums.StatusPagamento;
import com.auth.demo.model.PagamentoModel;
import com.auth.demo.model.UserModel;
import com.auth.demo.repository.PagamentoRepository;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final UserService userService;

    public PagamentoService(PagamentoRepository pagamentoRepository, UserService userService) {
        this.pagamentoRepository = pagamentoRepository;
        this.userService = userService;
    }

    public PagamentoModel criarPagamento(PagamentoModel pagamento) {

        UserModel usuarioLogado = userService.getUsuarioLogado();

        // 🔒 Segurança: ignora qualquer user vindo do request
        pagamento.setUser(usuarioLogado);

        // 🔹 Validações
        if (pagamento.getValor() == null || pagamento.getValor().doubleValue() <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }

        if (pagamento.getMetodo() == null) {
            throw new IllegalArgumentException("Método de pagamento é obrigatório");
        }

        return pagamentoRepository.save(pagamento);
    }

    // 🔹 Listar apenas pagamentos do usuário logado
    public List<PagamentoResponseDTO> listarMeusPagamentos() {
        UserModel usuarioLogado = userService.getUsuarioLogado();

        List<PagamentoModel> pagamentos =
            pagamentoRepository.findByUserId(usuarioLogado.getId());

        return pagamentos.stream()
                .map(PagamentoResponseDTO::new)
                .toList();
    }

    // 🔹 Buscar por ID com proteção
    public PagamentoModel buscarPorId(Long id) {
        UserModel usuarioLogado = userService.getUsuarioLogado();
        PagamentoModel pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Pagamento não encontrado"));

        // 🔒 BLOQUEIO DE ACESSO
        if (!pagamento.getUser().getId().equals(usuarioLogado.getId())) {
            throw new IllegalStateException("Acesso negado");
        }
        return pagamento;
    }
    
    // 🔹 Confirmar pagamento
    public PagamentoModel confirmarPagamento(Long id) {
        PagamentoModel pagamento = buscarPorId(id);
        pagamento.confirmar();
        return pagamentoRepository.save(pagamento);
    }

    // 🔹 Cancelar pagamento
    public PagamentoModel cancelarPagamento(Long id) {
        PagamentoModel pagamento = buscarPorId(id);
        pagamento.cancelar();
        return pagamentoRepository.save(pagamento);
    }

    // 🔹 Filtros seguros
    public List<PagamentoResponseDTO> listarPorStatus(StatusPagamento status) {
        UserModel usuarioLogado = userService.getUsuarioLogado();
        return pagamentoRepository
            .findByUserIdAndStatus(usuarioLogado.getId(), status)
            .stream()
            .map(PagamentoResponseDTO::new)
            .toList();
    }

    public List<PagamentoResponseDTO> listarPorMetodo(MetodoPagamento metodo) {
        UserModel usuarioLogado = userService.getUsuarioLogado();
        return pagamentoRepository
            .findByUserIdAndMetodo(usuarioLogado.getId(), metodo)
            .stream()
            .map(PagamentoResponseDTO::new)
            .toList();
    }
}
