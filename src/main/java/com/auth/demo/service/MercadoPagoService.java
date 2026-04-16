package com.auth.demo.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.auth.demo.model.OAuthTokenResponse;
import com.auth.demo.model.UserModel;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.resources.payment.Payment;

@Service
public class MercadoPagoService {

    @Value("${mercadopago.client.id}")
    private String clientId;

    @Value("${mercadopago.client.secret}")
    private String clientSecret;

    public OAuthTokenResponse gerarToken(String code) {

        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> body = new HashMap<>();
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        body.put("redirect_uri", "http://localhost:8080/oauth/callback");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<OAuthTokenResponse> response =
            restTemplate.postForEntity(
                "https://api.mercadopago.com/oauth/token",
                request,
                OAuthTokenResponse.class
            );

        return response.getBody();
    }

    public Payment buscarPagamento(UserModel usuario, Long paymentId) throws Exception {
        MercadoPagoConfig.setAccessToken(usuario.getMpAccessToken());
        PaymentClient client = new PaymentClient();
        return client.get(paymentId);
    }
    
    public Payment criarPix(UserModel usuario, BigDecimal valor, String descricao) throws Exception {

        MercadoPagoConfig.setAccessToken(usuario.getMpAccessToken());

        PaymentCreateRequest request = PaymentCreateRequest.builder()
            .transactionAmount(valor)
            .description(descricao)
            .paymentMethodId("pix")
            .payer(
                PaymentPayerRequest.builder()
                    .email("cliente@email.com")
                    .build()
            )
            .build();

        PaymentClient client = new PaymentClient();
        return client.create(request);
    }


}
