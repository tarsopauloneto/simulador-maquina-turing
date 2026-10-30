package com.br.unifor.simulador_maquina_turing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimuladorMaquinaTuringApplication {

	public static void main(String[] args) {

        SpringApplication.run(SimuladorMaquinaTuringApplication.class, args);

        MaquinaTuring mt = new MaquinaTuring("q0","q3",new Fita("000100"));

        mt.adicionarTransicao(new Transicao("q0",'0',"q0",'0','D'));
        mt.adicionarTransicao(new Transicao("q0",'1',"q1",'0','D'));

        mt.adicionarTransicao(new Transicao("q1",'0',"q1",'0','D'));
        mt.adicionarTransicao(new Transicao("q1",'B',"q2",'B','E'));

        mt.adicionarTransicao(new Transicao("q2",'0',"q3",'B','D'));

        System.out.println(mt.executar());
	}

}
