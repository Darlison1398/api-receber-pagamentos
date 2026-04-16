package com.auth.demo.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auth.demo.enums.MetodoPagamento;
import com.auth.demo.enums.StatusPagamento;
import com.auth.demo.model.PagamentoModel;

@Repository
public interface PagamentoRepository extends JpaRepository<PagamentoModel, Long> {
    List<PagamentoModel> findByUserId(Long userId);
    List<PagamentoModel> findByUserIdAndStatus(Long userId, StatusPagamento status);
    List<PagamentoModel> findByUserIdAndMetodo(Long userId, MetodoPagamento metodo);
    Optional<PagamentoModel> findByMpPaymentId(Long mpPaymentId);
}
