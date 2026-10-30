package com.br.unifor.simulador_maquina_turing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaquinaTuring {

    private String estadoAtual;
    private String estadoFinal;
    private Fita fita;
    private List<Transicao> transicaos = new ArrayList<>();

    public MaquinaTuring(String estadoAtual, String estadoFinal, Fita fita) {
        this.estadoAtual = estadoAtual;
        this.estadoFinal = estadoFinal;
        this.fita = fita;
    }

    public void adicionarTransicao(Transicao transicao){
        transicaos.add(transicao);
    }

    public void executar(){
        int passo =0;

        while (!estadoAtual.equals(estadoFinal)){
            char simbolo = fita.ler();

            Optional<Transicao> transicaoEntrada = transicaos.stream()
                    .filter(t->t.getEstadoAtual().equals(estadoAtual)
                            && t.getSimboloLido()==simbolo)
                    .findFirst();

            if (transicaoEntrada.isEmpty()){
                System.out.println("Maquina parou sem a transição valida! ");
                break;
            }

            Transicao t = transicaoEntrada.get();

            System.out.println("Passo:  "+passo);
            System.out.println(" Estado atual: " +estadoAtual);
            System.out.println(" Simbolo lido: "+ simbolo);
            System.out.println(" Fita : "+fita.conteudo());
            System.out.println(" cabeçote : "+ fita.getCabecote());
            System.out.println("=========================================");

            fita.escrever(t.getSimboloEscrito());
            fita.mover(t.getMovimento());
            estadoAtual = t.getNovoEstado();

            passo++;
        }

        System.out.println(" Maquina finalizada!");
        System.out.println(" Estado final: "+estadoAtual);
        System.out.println(" FIta final: "+fita.conteudo());
    }
}
