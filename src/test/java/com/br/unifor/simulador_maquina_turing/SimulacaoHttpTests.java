package com.br.unifor.simulador_maquina_turing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SimulacaoHttpTests {
    @Autowired
    private MockMvc http;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void exemploRetornaContratoCompleto() throws Exception {
        simular(exemplo())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ACEITA"))
                .andExpect(jsonPath("$.motivo").value("Estado de aceitacao atingido."))
                .andExpect(jsonPath("$.estadoFinal").value("q3"))
                .andExpect(jsonPath("$.fitaFinal").value("B00000BB"))
                .andExpect(jsonPath("$.passos").value(8))
                .andExpect(jsonPath("$.historico.length()").value(9))
                .andExpect(jsonPath("$.historico[0].passo").value(0))
                .andExpect(jsonPath("$.historico[0].estado").value("q0"))
                .andExpect(jsonPath("$.historico[0].simboloLido").value("0"))
                .andExpect(jsonPath("$.historico[0].fita").value("B000100B"))
                .andExpect(jsonPath("$.historico[0].cabecote").value(1))
                .andExpect(jsonPath("$.historico[8].estado").value("q3"))
                .andExpect(jsonPath("$.historico[8].cabecote").value(7));
    }

    @Test
    void aceitaEntradaVaziaERejeitaSemTransicoes() throws Exception {
        var dados = exemplo();
        dados.put("entrada", "");
        dados.put("transicoes", List.of());
        simular(dados).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.passos").value(0))
                .andExpect(jsonPath("$.fitaFinal").value("BB"));
        dados.put("estadoAceitacao", "q0");
        simular(dados).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACEITA"))
                .andExpect(jsonPath("$.passos").value(0));
    }

    @Test
    void loopRetornaLimiteDistintoDeRejeicao() throws Exception {
        var dados = exemplo();
        dados.put("entrada", "");
        dados.put("transicoes", List.of(transicao("q0", "B", "q1", "B", "D"),
                transicao("q1", "B", "q0", "B", "E")));
        simular(dados).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIMITE_ATINGIDO"))
                .andExpect(jsonPath("$.motivo").value("ERRO: limite de passos atingido; possivel loop infinito."))
                .andExpect(jsonPath("$.passos").value(1000))
                .andExpect(jsonPath("$.historico.length()").value(1001));
    }

    @ParameterizedTest
    @MethodSource("dadosInvalidos")
    void rejeitaDadosInvalidos(Map<String, Object> dados, String mensagem) throws Exception {
        simular(dados).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.containsString(mensagem)));
    }

    static Stream<Arguments> dadosInvalidos() {
        var casos = new ArrayList<Arguments>();
        for (String campo : List.of("entrada", "estadoInicial", "estadoAceitacao", "transicoes")) {
            var ausente = exemplo();
            ausente.remove(campo);
            casos.add(Arguments.of(ausente, campo.equals("entrada") ? "entrada" : campo));
            var nulo = exemplo();
            nulo.put(campo, null);
            casos.add(Arguments.of(nulo, campo));
        }
        for (String campo : List.of("estadoInicial", "estadoAceitacao")) {
            var dados = exemplo();
            dados.put(campo, "  ");
            casos.add(Arguments.of(dados, campo));
        }
        for (String campo : List.of("estadoAtual", "simboloLido", "novoEstado", "simboloEscrito", "direcao")) {
            for (String valor : new String[]{null, "", campo.contains("Estado") || campo.equals("estadoAtual") ? "  " : "XX"}) {
                var dados = exemplo();
                var transicao = transicao("q0", "0", "q1", "0", "D");
                transicao.put(campo, valor);
                dados.put("transicoes", List.of(transicao));
                casos.add(Arguments.of(dados, "Transicao 1: " + campo));
            }
        }
        for (String direcao : List.of("L", "R", "d")) {
            var dados = exemplo();
            dados.put("transicoes", List.of(transicao("q0", "0", "q1", "0", direcao)));
            casos.add(Arguments.of(dados, "direcao deve ser E ou D"));
        }
        var duplicadas = exemplo();
        duplicadas.put("transicoes", List.of(transicao("q0", "0", "q1", "0", "D"),
                transicao("q0", "0", "q2", "1", "E")));
        casos.add(Arguments.of(duplicadas, "duplicados"));
        var nula = exemplo();
        var lista = new ArrayList<Map<String, Object>>();
        lista.add(null);
        nula.put("transicoes", lista);
        casos.add(Arguments.of(nula, "Transicao 1"));
        return casos.stream();
    }

    @Test
    void rejeitaCorpoAusenteMalformadoOuComTiposInvalidos() throws Exception {
        for (String corpo : List.of("", "null", "{", "[]", "{\"transicoes\":{}}")) {
            http.perform(post("/api/simular").contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.containsString("JSON ausente ou invalido")));
        }
    }

    @Test
    void requisicoesSequenciaisEConcorrentesSaoIndependentes() throws Exception {
        Callable<Boolean> aceita = () -> {
            simular(exemplo()).andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACEITA"))
                    .andExpect(jsonPath("$.passos").value(8));
            return true;
        };
        Callable<Boolean> rejeita = () -> {
            var dados = exemplo();
            dados.put("transicoes", List.of());
            simular(dados).andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJEITADA"))
                    .andExpect(jsonPath("$.estadoFinal").value("q0"))
                    .andExpect(jsonPath("$.fitaFinal").value("B000100B"))
                    .andExpect(jsonPath("$.passos").value(0));
            return true;
        };
        aceita.call();
        rejeita.call();
        aceita.call();
        try (var executor = Executors.newFixedThreadPool(4)) {
            for (var resultado : executor.invokeAll(List.of(aceita, rejeita, aceita, rejeita))) {
                assertTrue(resultado.get());
            }
        }
    }

    @Test
    void paginaDisponibilizaExemploInicial() throws Exception {
        http.perform(get("/index.html")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("000100")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<td>q2</td><td>0</td><td>q3</td><td>B</td><td>D</td>")));
    }

    private org.springframework.test.web.servlet.ResultActions simular(Map<String, Object> dados) throws Exception {
        return http.perform(post("/api/simular").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dados)));
    }

    private static Map<String, Object> exemplo() {
        var dados = new LinkedHashMap<String, Object>();
        dados.put("entrada", "000100");
        dados.put("estadoInicial", "q0");
        dados.put("estadoAceitacao", "q3");
        dados.put("transicoes", List.of(transicao("q0", "0", "q0", "0", "D"),
                transicao("q0", "1", "q1", "0", "D"), transicao("q1", "0", "q1", "0", "D"),
                transicao("q1", "B", "q2", "B", "E"), transicao("q2", "0", "q3", "B", "D")));
        return dados;
    }

    private static Map<String, Object> transicao(String atual, String lido, String novo, String escrito, String direcao) {
        return new LinkedHashMap<>(Map.of("estadoAtual", atual, "simboloLido", lido,
                "novoEstado", novo, "simboloEscrito", escrito, "direcao", direcao));
    }
}
