package com.br.unifor.simulador_maquina_turing;

import java.util.List;

public record ResultadoSimulacao(Status status, String motivo, String estadoFinal,
                                 String fitaFinal, int passos, List<PassoSimulacao> historico) {
    public ResultadoSimulacao {
        historico = List.copyOf(historico);
    }

    public enum Status {
        ACEITA, REJEITADA, LIMITE_ATINGIDO
    }
}
