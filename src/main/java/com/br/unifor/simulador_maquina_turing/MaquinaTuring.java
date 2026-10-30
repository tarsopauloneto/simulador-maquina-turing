package com.br.unifor.simulador_maquina_turing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaquinaTuring {

    public static final int LIMITE_PASSOS = 1000;

    private String estadoAtual;
    private final String estadoFinal;
    private final Fita fita;
    private final List<Transicao> transicoes = new ArrayList<>();

    public MaquinaTuring(String estadoAtual, String estadoFinal, Fita fita) {
        this.estadoAtual = estadoAtual;
        this.estadoFinal = estadoFinal;
        this.fita = fita;
    }

    public void adicionarTransicao(Transicao transicao) {
        transicoes.add(transicao);
    }

    public ResultadoSimulacao executar() {
        int passos = 0;
        List<PassoSimulacao> historico = new ArrayList<>();

        while (true) {
            historico.add(new PassoSimulacao(passos, estadoAtual, fita.ler(),
                    fita.conteudo(), fita.getCabecote()));

            if (estadoAtual.equals(estadoFinal)) {
                return resultado(ResultadoSimulacao.Status.ACEITA,
                        "Estado de aceitacao atingido.", passos, historico);
            }

            Optional<Transicao> proxima = buscarTransicao();
            if (proxima.isEmpty()) {
                return resultado(ResultadoSimulacao.Status.REJEITADA,
                        "Maquina parou sem transicao valida fora do estado de aceitacao.", passos, historico);
            }

            // So ha limite atingido se a maquina ainda precisar executar outra transicao.
            if (passos == LIMITE_PASSOS) {
                return resultado(ResultadoSimulacao.Status.LIMITE_ATINGIDO,
                        "ERRO: limite de passos atingido; possivel loop infinito.", passos, historico);
            }

            Transicao transicao = proxima.get();
            fita.escrever(transicao.getSimboloEscrito());
            fita.mover(transicao.getMovimento());
            estadoAtual = transicao.getNovoEstado();
            passos++;
        }
    }

    private Optional<Transicao> buscarTransicao() {
        return transicoes.stream()
                .filter(t -> t.getEstadoAtual().equals(estadoAtual) && t.getSimboloLido() == fita.ler())
                .findFirst();
    }

    private ResultadoSimulacao resultado(ResultadoSimulacao.Status status, String motivo,
                                         int passos, List<PassoSimulacao> historico) {
        return new ResultadoSimulacao(status, motivo, estadoAtual, fita.conteudo(), passos, historico);
    }
}
