package com.br.unifor.simulador_maquina_turing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record RequisicaoSimulacao(String entrada, String estadoInicial, String estadoAceitacao,
                                  List<ConfiguracaoTransicao> transicoes) {

    public void validar() {
        if (entrada == null) {
            throw new IllegalArgumentException("Informe a entrada; uma cadeia vazia e permitida.");
        }
        validarEstado(estadoInicial, "estadoInicial");
        validarEstado(estadoAceitacao, "estadoAceitacao");
        if (transicoes == null) {
            throw new IllegalArgumentException("Informe a lista de transicoes; ela pode estar vazia.");
        }

        Map<String, Set<String>> simbolosPorEstado = new HashMap<>();
        for (int i = 0; i < transicoes.size(); i++) {
            ConfiguracaoTransicao transicao = transicoes.get(i);
            String campo = "Transicao " + (i + 1) + ": ";
            if (transicao == null) {
                throw new IllegalArgumentException(campo + "informe os cinco campos.");
            }
            transicao.validar(campo);
            if (!simbolosPorEstado.computeIfAbsent(transicao.estadoAtual(), chave -> new HashSet<>())
                    .add(transicao.simboloLido())) {
                throw new IllegalArgumentException(campo + "estado atual + simbolo lido duplicados.");
            }
        }
    }

    private static void validarEstado(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " deve ser preenchido.");
        }
    }

    public record ConfiguracaoTransicao(String estadoAtual, String simboloLido, String novoEstado,
                                        String simboloEscrito, String direcao) {
        private void validar(String campo) {
            validarEstado(estadoAtual, campo + "estadoAtual");
            validarEstado(novoEstado, campo + "novoEstado");
            validarSimbolo(simboloLido, campo + "simboloLido");
            validarSimbolo(simboloEscrito, campo + "simboloEscrito");
            if (!"E".equals(direcao) && !"D".equals(direcao)) {
                throw new IllegalArgumentException(campo + "direcao deve ser E ou D.");
            }
        }

        private static void validarSimbolo(String valor, String campo) {
            // O motor da aula representa cada celula por um char Java.
            if (valor == null || valor.length() != 1 || Character.isSurrogate(valor.charAt(0))) {
                throw new IllegalArgumentException(campo + " deve ter um unico caractere; use B para branco.");
            }
        }
    }
}
