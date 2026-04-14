package com.auth.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.auth.demo.dto.PagamentoResponseDTO;
import com.auth.demo.enums.MetodoPagamento;
import com.auth.demo.enums.StatusPagamento;
import com.auth.demo.model.PagamentoModel;
import com.auth.demo.service.PagamentoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    // 🔹 Criar pagamento
    @PostMapping("/criar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> criarPagamento(@Valid @RequestBody PagamentoModel pagamento) {
        try {
            pagamentoService.criarPagamento(pagamento);
            return ResponseEntity.status(HttpStatus.CREATED).body("Pagamento registrado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar pagamento: " + e.getMessage());
        }
    }

    // 🔹 Listar meus pagamentos
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> listarMeusPagamentos() {
        try {
            List<PagamentoResponseDTO> lista = pagamentoService.listarMeusPagamentos();
            return ResponseEntity.ok(lista);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar pagamentos: " + e.getMessage());
        }
    }

    // 🔹 Buscar pagamento por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            PagamentoModel pagamento = pagamentoService.buscarPorId(id);
            return ResponseEntity.ok(new PagamentoResponseDTO(pagamento));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erro ao buscar pagamento: " + e.getMessage());
        }
    }

    // 🔹 Confirmar pagamento
    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> confirmar(@PathVariable Long id) {
        try {
            PagamentoModel pagamento = pagamentoService.confirmarPagamento(id);
            return ResponseEntity.ok(new PagamentoResponseDTO(pagamento));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Erro ao confirmar pagamento: " + e.getMessage());
        }
    }

    // 🔹 Cancelar pagamento
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        try {
            PagamentoModel pagamento = pagamentoService.cancelarPagamento(id);
            return ResponseEntity.ok(new PagamentoResponseDTO(pagamento));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Erro ao cancelar pagamento: " + e.getMessage());
        }
    }

    // 🔹 Filtrar por status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> listarPorStatus(@PathVariable StatusPagamento status) {
        try {
            return ResponseEntity.ok(pagamentoService.listarPorStatus(status));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Erro ao filtrar por status: " + e.getMessage());
        }
    }

    // 🔹 Filtrar por método
    @GetMapping("/metodo/{metodo}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> listarPorMetodo(@PathVariable MetodoPagamento metodo) {
        try {
            return ResponseEntity.ok(pagamentoService.listarPorMetodo(metodo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Erro ao filtrar por método: " + e.getMessage());
        }
    }
}