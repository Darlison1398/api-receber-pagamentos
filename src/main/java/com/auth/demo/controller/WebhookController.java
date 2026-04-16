package com.auth.demo.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.demo.enums.StatusPagamento;
import com.auth.demo.model.PagamentoModel;
import com.auth.demo.model.UserModel;
import com.auth.demo.repository.PagamentoRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final PagamentoRepository pagamentoRepository;

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> receber(@RequestBody Map<String, Object> body) throws Exception {

        if (body.get("data") == null) {
            return ResponseEntity.ok().build();
        }

        Map<String, Object> data = (Map<String, Object>) body.get("data");

        if (data.get("id") == null) {
            return ResponseEntity.ok().build();
        }

        Long paymentId = Long.valueOf(data.get("id").toString());

        // 🔹 Busca pagamento no seu banco
        Optional<PagamentoModel> optionalPagamento =
            pagamentoRepository.findByMpPaymentId(paymentId);

        if (optionalPagamento.isEmpty()) {
            return ResponseEntity.ok().build();
        }

        PagamentoModel pagamento = optionalPagamento.get();

        // 🔹 Pega o usuário dono do pagamento
        UserModel usuario = pagamento.getUser();

        // 🔹 Usa o token do usuário
        MercadoPagoConfig.setAccessToken(usuario.getMpAccessToken());

        // 🔹 Consulta no Mercado Pago
        Payment mpPayment = new PaymentClient().get(paymentId);

        if ("approved".equals(mpPayment.getStatus())
            && pagamento.getStatus() == StatusPagamento.PENDENTE) {

            pagamento.confirmar();
            pagamentoRepository.save(pagamento);
        }

        return ResponseEntity.ok().build();
    }
        
}
