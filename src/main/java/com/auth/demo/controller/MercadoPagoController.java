package com.auth.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.demo.model.OAuthTokenResponse;
import com.auth.demo.model.UserModel;
import com.auth.demo.repository.UserRepository;
import com.auth.demo.service.MercadoPagoService;
import com.auth.demo.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoController {

    private final UserService userService;
    private final MercadoPagoService mercadoPagoService;
    private final UserRepository userRepository;

    @Value("${mercadopago.client.id}")
    private String clientId;

    @GetMapping("/conectar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> conectar() {

        UserModel usuario = userService.getUsuarioLogado();
        String url = "https://auth.mercadopago.com.br/authorization" +
            "?client_id=" + clientId +
            "&response_type=code" +
            "&platform_id=mp" +
            "&state=" + usuario.getId(); // 🔥 AQUI

        return ResponseEntity.ok(url);

    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<String> callback(
        @RequestParam("code") String code,
        @RequestParam("state") String state) {

    try {
        Long userId = Long.valueOf(state);

        UserModel usuario = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        OAuthTokenResponse token = mercadoPagoService.gerarToken(code);

        usuario.setMpAccessToken(token.getAccessToken());
        usuario.setMpRefreshToken(token.getRefreshToken());
        usuario.setMpUserId(token.getUserId());

        userRepository.save(usuario);

        return ResponseEntity.ok("Conta conectada com sucesso!");

    } catch (Exception e) {
        return ResponseEntity.internalServerError()
            .body("Erro ao conectar conta: " + e.getMessage());
    }
}
}
