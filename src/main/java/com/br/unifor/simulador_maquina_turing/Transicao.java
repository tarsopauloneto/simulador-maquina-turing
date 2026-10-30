package com.br.unifor.simulador_maquina_turing;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class Transicao {
    private String estadoAtual;
    private char simboloLido;
    private String novoEstado;
    private char simboloEscrito;
    private char movimento;

    public Transicao(String estadoAtual, char simboloLido, String novoEstado, char simboloEscrito, char movimento) {
        this.estadoAtual = estadoAtual;
        this.simboloLido = simboloLido;
        this.novoEstado = novoEstado;
        this.simboloEscrito = simboloEscrito;
        this.movimento = movimento;
    }

    public String getEstadoAtual() {
        return estadoAtual;
    }

    public char getSimboloLido() {
        return simboloLido;
    }

    public String getNovoEstado() {
        return novoEstado;
    }

    public char getSimboloEscrito() {
        return simboloEscrito;
    }

    public char getMovimento() {
        return movimento;
    }
}
