package com.br.unifor.simulador_maquina_turing;

// A posicao do cabecote e o indice (a partir de zero) na fita desta configuracao.
public record PassoSimulacao(int passo, String estado, char simboloLido, String fita, int cabecote) {
}
