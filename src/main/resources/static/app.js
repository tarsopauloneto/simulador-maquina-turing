const CHAVE = 'maquinaTuring.configuracao';
const PADRAO = {
    entrada: '000100', estadoInicial: 'q0', estadoAceitacao: 'q3',
    transicoes: [
        { estadoAtual: 'q0', simboloLido: '0', novoEstado: 'q0', simboloEscrito: '0', direcao: 'D' },
        { estadoAtual: 'q0', simboloLido: '1', novoEstado: 'q1', simboloEscrito: '0', direcao: 'D' },
        { estadoAtual: 'q1', simboloLido: '0', novoEstado: 'q1', simboloEscrito: '0', direcao: 'D' },
        { estadoAtual: 'q1', simboloLido: 'B', novoEstado: 'q2', simboloEscrito: 'B', direcao: 'E' },
        { estadoAtual: 'q2', simboloLido: '0', novoEstado: 'q3', simboloEscrito: 'B', direcao: 'D' }
    ]
};
const CAMPOS = ['estadoAtual', 'simboloLido', 'novoEstado', 'simboloEscrito', 'direcao'];
const formulario = document.getElementById('configuracao');
const tabela = document.getElementById('transicoes');
const mensagem = document.getElementById('mensagem');
const detalhes = document.getElementById('detalhes');
const historico = document.getElementById('historico');

function configuracaoValida(dados) {
    const estadoValido = valor => typeof valor === 'string' && valor.trim().length > 0;
    const simboloValido = valor => typeof valor === 'string' && valor.length === 1
        && !/[\uD800-\uDFFF]/.test(valor);
    if (!dados || typeof dados.entrada !== 'string' || !estadoValido(dados.estadoInicial)
        || !estadoValido(dados.estadoAceitacao) || !Array.isArray(dados.transicoes)) return false;
    const pares = new Set();
    return dados.transicoes.every(t => {
        if (!t || !estadoValido(t.estadoAtual) || !estadoValido(t.novoEstado)
            || !simboloValido(t.simboloLido) || !simboloValido(t.simboloEscrito)
            || !['E', 'D'].includes(t.direcao)) return false;
        const par = JSON.stringify([t.estadoAtual, t.simboloLido]);
        if (pares.has(par)) return false;
        pares.add(par);
        return true;
    });
}

function avisar(texto) {
    const aviso = document.getElementById('aviso');
    aviso.textContent = texto;
    aviso.hidden = false;
}

function carregar() {
    try {
        const dados = JSON.parse(localStorage.getItem(CHAVE));
        if (configuracaoValida(dados)) return dados;
    } catch {
        avisar('Não foi possível recuperar a configuração salva. O exemplo inicial foi carregado.');
    }
    return PADRAO;
}

function lerConfiguracao() {
    return {
        entrada: document.getElementById('entrada').value,
        estadoInicial: document.getElementById('estadoInicial').value,
        estadoAceitacao: document.getElementById('estadoAceitacao').value,
        transicoes: Array.from(tabela.rows, linha => Object.fromEntries(
            CAMPOS.map(campo => [campo, linha.querySelector(`[name="${campo}"]`).value])))
    };
}

function salvar() {
    try {
        localStorage.setItem(CHAVE, JSON.stringify(lerConfiguracao()));
    } catch {
        avisar('Não foi possível salvar no navegador. As alterações podem ser perdidas ao recarregar.');
    }
}

function adicionarTransicao(transicao) {
    const linha = tabela.insertRow();
    for (const campo of CAMPOS) {
        const controle = document.createElement(campo === 'direcao' ? 'select' : 'input');
        controle.name = campo;
        controle.required = true;
        if (campo === 'direcao') {
            for (const direcao of ['E', 'D']) controle.add(new Option(direcao, direcao));
        } else if (campo === 'simboloLido' || campo === 'simboloEscrito') {
            controle.maxLength = 1;
            controle.size = 1;
        }
        controle.value = transicao[campo];
        linha.insertCell().append(controle);
    }
    const excluir = document.createElement('button');
    excluir.type = 'button';
    excluir.textContent = 'Excluir';
    excluir.addEventListener('click', () => {
        linha.remove();
        atualizarRotulos();
        alterar();
    });
    linha.insertCell().append(excluir);
    atualizarRotulos();
}

function atualizarRotulos() {
    const nomes = ['Estado atual', 'Símbolo lido', 'Novo estado', 'Símbolo escrito', 'Direção', 'Excluir'];
    Array.from(tabela.rows).forEach((linha, indice) => {
        linha.querySelectorAll('input, select, button').forEach((controle, coluna) => {
            controle.setAttribute('aria-label', `${nomes[coluna]} da transição ${indice + 1}`);
        });
    });
}

function preencher(dados) {
    for (const campo of ['entrada', 'estadoInicial', 'estadoAceitacao']) {
        document.getElementById(campo).value = dados[campo];
    }
    tabela.replaceChildren();
    dados.transicoes.forEach(adicionarTransicao);
}

function limparResultado() {
    detalhes.hidden = true;
    historico.replaceChildren();
    mensagem.className = '';
    mensagem.textContent = 'Configure a máquina e clique em Simular.';
}

function alterar() {
    salvar();
    limparResultado();
}

function mostrarResultado(resultado) {
    const status = { ACEITA: 'Cadeia aceita', REJEITADA: 'Cadeia rejeitada', LIMITE_ATINGIDO: 'Erro de simulação' };
    mensagem.textContent = `${status[resultado.status]}. ${resultado.motivo}`;
    mensagem.className = resultado.status === 'LIMITE_ATINGIDO' ? 'erro' : '';
    for (const campo of ['estadoFinal', 'fitaFinal', 'passos']) {
        document.getElementById(campo).textContent = resultado[campo];
    }
    const linhas = document.createDocumentFragment();
    for (const passo of resultado.historico) {
        const linha = document.createElement('tr');
        for (const campo of ['passo', 'estado', 'simboloLido', 'fita', 'cabecote']) {
            const celula = document.createElement('td');
            const texto = document.createElement(campo === 'fita' ? 'code' : 'span');
            texto.textContent = passo[campo];
            celula.append(texto);
            linha.append(celula);
        }
        linhas.append(linha);
    }
    historico.replaceChildren(linhas);
    detalhes.hidden = false;
}

formulario.addEventListener('input', alterar);
formulario.addEventListener('change', alterar);
document.getElementById('adicionar').addEventListener('click', () => {
    adicionarTransicao({ estadoAtual: '', simboloLido: '', novoEstado: '', simboloEscrito: '', direcao: 'D' });
    alterar();
    tabela.lastElementChild.querySelector('input').focus();
});
document.getElementById('restaurar').addEventListener('click', () => {
    if (!window.confirm('Restaurar todo o exemplo inicial? Suas alterações serão substituídas.')) return;
    preencher(PADRAO);
    alterar();
});
formulario.addEventListener('submit', async evento => {
    evento.preventDefault();
    const dados = lerConfiguracao();
    salvar();
    limparResultado();
    const controles = document.getElementById('controles');
    const resultados = document.getElementById('resultados');
    controles.disabled = true;
    resultados.setAttribute('aria-busy', 'true');
    mensagem.textContent = 'Simulando…';
    try {
        const resposta = await fetch('/api/simular', {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dados)
        });
        const resultado = await resposta.json();
        if (!resposta.ok) throw new Error(resultado.erro || 'Não foi possível simular.');
        mostrarResultado(resultado);
    } catch (erro) {
        detalhes.hidden = true;
        mensagem.textContent = `Erro: ${erro.message}. Verifique os dados e se o servidor está disponível.`;
        mensagem.className = 'erro';
    } finally {
        controles.disabled = false;
        resultados.setAttribute('aria-busy', 'false');
    }
});

preencher(carregar());
salvar();
