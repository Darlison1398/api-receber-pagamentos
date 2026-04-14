package com.auth.demo.enums;

public enum MetodoPagamento {
    DINHEIRO("Dinheiro"),
    PIX("Pix"),
    CARTAO("Cartão");

    private final String descricao;

    MetodoPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
