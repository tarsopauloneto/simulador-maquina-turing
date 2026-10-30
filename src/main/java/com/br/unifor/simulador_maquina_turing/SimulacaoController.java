package com.br.unifor.simulador_maquina_turing;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimulacaoController {

    @PostMapping("/api/simular")
    public ResultadoSimulacao simular(@RequestBody RequisicaoSimulacao requisicao) {
        requisicao.validar();
        MaquinaTuring maquina = new MaquinaTuring(requisicao.estadoInicial(),
                requisicao.estadoAceitacao(), new Fita(requisicao.entrada()));
        for (var transicao : requisicao.transicoes()) {
            maquina.adicionarTransicao(new Transicao(transicao.estadoAtual(), transicao.simboloLido().charAt(0),
                    transicao.novoEstado(), transicao.simboloEscrito().charAt(0), transicao.direcao().charAt(0)));
        }
        return maquina.executar();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroRequisicao dadosInvalidos(IllegalArgumentException erro) {
        return new ErroRequisicao(erro.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroRequisicao jsonInvalido() {
        return new ErroRequisicao("Corpo JSON ausente ou invalido. Informe entrada, estadoInicial, estadoAceitacao e transicoes com os tipos corretos.");
    }

    public record ErroRequisicao(String erro) {
    }
}
