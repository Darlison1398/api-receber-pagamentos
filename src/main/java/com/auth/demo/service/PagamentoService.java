package com.auth.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.auth.demo.dto.PagamentoResponseDTO;
import com.auth.demo.enums.MetodoPagamento;
import com.auth.demo.enums.StatusPagamento;
import com.auth.demo.model.PagamentoModel;
import com.auth.demo.model.UserModel;
import com.auth.demo.repository.PagamentoRepository;
import com.mercadopago.resources.payment.Payment;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final UserService userService;
    private final MercadoPagoService mercadoPagoService;

    public PagamentoService(PagamentoRepository pagamentoRepository, UserService userService, MercadoPagoService mercadoPagoService) {
        this.pagamentoRepository = pagamentoRepository;
        this.userService = userService;
        this.mercadoPagoService = mercadoPagoService;
    }

    public PagamentoModel criarPagamento(PagamentoModel pagamento) {

        UserModel usuarioLogado = userService.getUsuarioLogado();

        pagamento.setUser(usuarioLogado);

        if (pagamento.getValor() == null || pagamento.getValor().doubleValue() <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }

        if (pagamento.getMetodo() == null) {
            throw new IllegalArgumentException("Método de pagamento é obrigatório");
        }

        PagamentoModel salvo = pagamentoRepository.save(pagamento);

        // 🔥 PIX
        if (pagamento.getMetodo() == MetodoPagamento.PIX) {

            if (usuarioLogado.getMpAccessToken() == null) {
                throw new IllegalStateException("Usuário não conectou conta do Mercado Pago");
            }

            try {
                Payment mpPayment = mercadoPagoService.criarPix(
                    usuarioLogado,
                    pagamento.getValor(),
                    "Pagamento ID: " + salvo.getId()
                );

                salvo.setMpPaymentId(mpPayment.getId());

                pagamentoRepository.save(salvo);

            } catch (Exception e) {
                throw new RuntimeException("Erro ao gerar PIX", e);
            }
        }

        return salvo;
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
