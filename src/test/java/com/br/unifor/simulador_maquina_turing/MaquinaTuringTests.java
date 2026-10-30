package com.br.unifor.simulador_maquina_turing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaquinaTuringTests {

    @Test
    void aceitaExemploInicialComHistoricoCompleto() {
        MaquinaTuring maquina = new MaquinaTuring("q0", "q3", new Fita("000100"));
        maquina.adicionarTransicao(new Transicao("q0", '0', "q0", '0', 'D'));
        maquina.adicionarTransicao(new Transicao("q0", '1', "q1", '0', 'D'));
        maquina.adicionarTransicao(new Transicao("q1", '0', "q1", '0', 'D'));
        maquina.adicionarTransicao(new Transicao("q1", 'B', "q2", 'B', 'E'));
        maquina.adicionarTransicao(new Transicao("q2", '0', "q3", 'B', 'D'));

        ResultadoSimulacao resultado = maquina.executar();

        assertEquals(ResultadoSimulacao.Status.ACEITA, resultado.status());
        assertEquals("q3", resultado.estadoFinal());
        assertEquals("B00000BB", resultado.fitaFinal());
        assertEquals(8, resultado.passos());
        assertEquals(9, resultado.historico().size());
        assertEquals(new PassoSimulacao(0, "q0", '0', "B000100B", 1), resultado.historico().getFirst());
        assertEquals(new PassoSimulacao(8, "q3", 'B', "B00000BB", 7), resultado.historico().getLast());
        assertEquals(new PassoSimulacao(4, "q1", '0', "B000000B", 5), resultado.historico().get(4));
        assertThrows(UnsupportedOperationException.class, () -> resultado.historico().clear());
    }

    @Test
    void rejeitaSemTransicaoERegistraConfiguracaoFinal() {
        MaquinaTuring maquina = new MaquinaTuring("q0", "qf", new Fita("01"));
        maquina.adicionarTransicao(new Transicao("q0", '0', "q1", '1', 'D'));

        ResultadoSimulacao resultado = maquina.executar();

        assertEquals(ResultadoSimulacao.Status.REJEITADA, resultado.status());
        assertTrue(resultado.motivo().contains("sem transicao"));
        assertEquals(1, resultado.passos());
        assertEquals("q1", resultado.estadoFinal());
        assertEquals(new PassoSimulacao(1, "q1", '1', "B11B", 2), resultado.historico().getLast());
    }

    @Test
    void entradaVaziaPodeSerAceitaLendoBranco() {
        MaquinaTuring maquina = new MaquinaTuring("q0", "qf", new Fita(""));
        maquina.adicionarTransicao(new Transicao("q0", 'B', "qf", 'B', 'D'));

        ResultadoSimulacao resultado = maquina.executar();

        assertEquals(ResultadoSimulacao.Status.ACEITA, resultado.status());
        assertEquals(1, resultado.passos());
        assertEquals(new PassoSimulacao(0, "q0", 'B', "BB", 1), resultado.historico().getFirst());
        assertEquals(new PassoSimulacao(1, "qf", 'B', "BBB", 2), resultado.historico().getLast());
    }

    @Test
    void entradaVaziaSemTransicaoERejeitadaSemPassos() {
        ResultadoSimulacao resultado = new MaquinaTuring("q0", "qf", new Fita("")).executar();

        assertEquals(ResultadoSimulacao.Status.REJEITADA, resultado.status());
        assertEquals(0, resultado.passos());
        assertEquals(1, resultado.historico().size());
    }

    @Test
    void aceitaEstadoInicialFinalMesmoComTransicaoDisponivel() {
        MaquinaTuring maquina = new MaquinaTuring("qf", "qf", new Fita("01"));
        maquina.adicionarTransicao(new Transicao("qf", '0', "q0", '1', 'D'));

        ResultadoSimulacao resultado = maquina.executar();

        assertEquals(ResultadoSimulacao.Status.ACEITA, resultado.status());
        assertEquals(0, resultado.passos());
        assertEquals("B01B", resultado.fitaFinal());
        assertEquals(1, resultado.historico().size());
    }

    @Test
    void aceitaSemExigirConsumoCompletoDaEntrada() {
        MaquinaTuring maquina = new MaquinaTuring("q0", "qf", new Fita("000"));
        maquina.adicionarTransicao(new Transicao("q0", '0', "qf", '1', 'D'));

        ResultadoSimulacao resultado = maquina.executar();

        assertEquals(ResultadoSimulacao.Status.ACEITA, resultado.status());
        assertEquals(1, resultado.passos());
        assertEquals("B100B", resultado.fitaFinal());
        assertEquals('0', resultado.historico().getLast().simboloLido());
    }

    @Test
    void expandeFitaNasDuasExtremidadesPreservandoCelulas() {
        Fita fita = new Fita("0");
        fita.mover('E');
        fita.escrever('1');
        fita.mover('E');
        assertEquals("B10B", fita.conteudo());
        assertEquals(0, fita.getCabecote());
        assertEquals('B', fita.ler());
        fita.escrever('X');
        for (int i = 0; i < 4; i++) {
            fita.mover('D');
        }
        assertEquals("X10BB", fita.conteudo());
        assertEquals(4, fita.getCabecote());
        assertEquals('B', fita.ler());
    }

    @Test
    void rejeitaMovimentoInvalidoSemMoverFita() {
        Fita fita = new Fita("0");
        assertThrows(IllegalArgumentException.class, () -> fita.mover('R'));
        assertEquals(1, fita.getCabecote());
        assertEquals("B0B", fita.conteudo());
    }

    @Test
    void interrompeLoopComoErroDistintoDeRejeicao() {
        MaquinaTuring maquina = new MaquinaTuring("q0", "qf", new Fita(""));
        maquina.adicionarTransicao(new Transicao("q0", 'B', "q1", 'B', 'D'));
        maquina.adicionarTransicao(new Transicao("q1", 'B', "q0", 'B', 'E'));

        ResultadoSimulacao resultado = maquina.executar();

        assertEquals(ResultadoSimulacao.Status.LIMITE_ATINGIDO, resultado.status());
        assertEquals("ERRO: limite de passos atingido; possivel loop infinito.", resultado.motivo());
        assertEquals(1000, resultado.passos());
        assertEquals(1001, resultado.historico().size());
        assertEquals(new PassoSimulacao(1000, "q0", 'B', "BBB", 1), resultado.historico().getLast());
    }

    @Test
    void aceitaNaUltimaTransicaoPermitida() {
        MaquinaTuring maquina = maquinaComCadeiaDeEstados("q1000");
        maquina.adicionarTransicao(new Transicao("q1000", 'B', "q1001", 'B', 'D'));

        ResultadoSimulacao resultado = maquina.executar();

        assertEquals(ResultadoSimulacao.Status.ACEITA, resultado.status());
        assertEquals(1000, resultado.passos());
        assertEquals("q1000", resultado.estadoFinal());
        assertEquals(1001, resultado.historico().size());
    }

    @Test
    void rejeitaAoPararSemTransicaoExatamenteNoLimite() {
        ResultadoSimulacao resultado = maquinaComCadeiaDeEstados("qf").executar();

        assertEquals(ResultadoSimulacao.Status.REJEITADA, resultado.status());
        assertEquals(1000, resultado.passos());
        assertEquals("q1000", resultado.estadoFinal());
    }

    @Test
    void execucoesDeMaquinasDistintasNaoCompartilhamEstado() {
        MaquinaTuring primeira = new MaquinaTuring("q0", "qf", new Fita("0"));
        MaquinaTuring segunda = new MaquinaTuring("q0", "qf", new Fita("0"));
        primeira.adicionarTransicao(new Transicao("q0", '0', "qf", '1', 'D'));

        ResultadoSimulacao aceita = primeira.executar();
        ResultadoSimulacao rejeitada = segunda.executar();

        assertEquals(ResultadoSimulacao.Status.ACEITA, aceita.status());
        assertEquals("B1B", aceita.fitaFinal());
        assertEquals(ResultadoSimulacao.Status.REJEITADA, rejeitada.status());
        assertEquals("B0B", rejeitada.fitaFinal());
        assertEquals("q0", rejeitada.estadoFinal());
        assertEquals(0, rejeitada.passos());
    }

    private MaquinaTuring maquinaComCadeiaDeEstados(String estadoFinal) {
        MaquinaTuring maquina = new MaquinaTuring("q0", estadoFinal, new Fita(""));
        for (int i = 0; i < 1000; i++) {
            maquina.adicionarTransicao(new Transicao("q" + i, 'B', "q" + (i + 1), 'B', 'D'));
        }
        return maquina;
    }
}
