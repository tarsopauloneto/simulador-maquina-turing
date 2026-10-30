package com.br.unifor.simulador_maquina_turing;

import java.util.ArrayList;
import java.util.List;

public class Fita {
    private List<Character> celulas = new ArrayList<>();
    private int cabecote =0;
    private final char BRANCO = 'B';

    public Fita(String entrada){
        celulas.add(BRANCO);

        for (char c: entrada.toCharArray()){
            celulas.add(c);
        }
        celulas.add(BRANCO);
        cabecote=1;
    }

    public char ler(){
        return celulas.get(cabecote);
    }

    public void escrever(char simbolo){
        celulas.set(cabecote, simbolo);
    }

    public void mover(char direcao){
        if (direcao=='R'){
            cabecote ++;
            if (cabecote>=celulas.size()){
                celulas.add(BRANCO);
            }
        } else if (direcao == 'L') {
            cabecote --;
            if (cabecote<0){
                celulas.add(0,BRANCO);
                cabecote =0;
            }
        }
    }

    public String conteudo(){
        StringBuilder sb = new StringBuilder();
        for (Character c: celulas){
            sb.append(c);

        }
        return sb.toString();
    }

    public int getCabecote(){
        return  cabecote;
    }
}
