# Planejamento temporario

Status: etapas 1, 2 e 3 implementadas, revisadas e validadas. Etapa 4 autorizada e em revisao; validacao final pendente.

Objetivo: pagina local simples para configurar uma maquina de Turing e simular uma cadeia usando o codigo Java da aula.

Base: Java 21, Gradle e Spring Boot existentes; classes Fita, Transicao e MaquinaTuring. Hoje a execucao usa exemplo fixo no main e imprime no console. Ha somente teste de carregamento do contexto.

Arquitetura: HTML, CSS minimo e JavaScript puro em src/main/resources/static, servidos pelo proprio Spring Boot. Um endpoint POST /api/simular recebe entrada, estados e transicoes e devolve resultado e passos. Reutilizar o motor Java; nao duplicar a simulacao no navegador. Sem banco, frameworks de frontend, deploy ou camadas desnecessarias.

Regras funcionais:
- Campos globais: cadeia (inclusive vazia), estado inicial e um estado de aceitacao. Cada transicao tem seus cinco campos: estado atual, simbolo lido, novo estado, simbolo escrito e direcao.
- B representa branco; E/D representam esquerda/direita. Adaptar o Java e o exemplo que hoje usam L/R, mantendo coerencia em todo o fluxo.
- Tabela de transicoes com adicionar, editar e excluir. Simbolos devem ter um caractere; estados devem ser preenchidos; rejeitar duplicidade de estado atual + simbolo lido para manter determinismo.
- Padrao inicial: entrada 000100, inicial q0, aceitacao q3 e transicoes (q0,0,q0,0,D), (q0,1,q1,0,D), (q1,0,q1,0,D), (q1,B,q2,B,E), (q2,0,q3,B,D).
- Botao Simular executa tudo, sem animacao. Resultados: aceita/rejeitada, motivo de parada, estado final, fita final, quantidade de passos e historico com estado, simbolo lido, fita e posicao do cabecote, incluindo configuracao final.
- Aceitar ao atingir o estado de aceitacao; rejeitar ao parar sem transicao fora desse estado. Nao exigir consumo completo da entrada para aceitar.
- Limite fixo inicial de 1000 transicoes por execucao, verificado no Java. Atingir aceitacao na ultima transicao permitida ainda e sucesso. Se precisar continuar, interromper e mostrar ERRO: limite de passos atingido; possivel loop infinito. Destacar visualmente a mensagem e nao classificar como rejeicao.
- Persistir entrada, estados e transicoes no localStorage do navegador ao alterar. Restaurar ao recarregar; dados ausentes ou invalidos usam o padrao. Resultados nao precisam persistir.
- Botao Restaurar padrao solicita confirmacao, recupera todo o exemplo inicial, atualiza localStorage e limpa resultados anteriores.
- Interface em portugues, com tres areas: entrada/configuracao, transicoes e resultados. Aparencia de pagina antiga: controles nativos, tabela, bordas simples, fundo claro e pouca estilizacao. Sem grafos, contas, importacao/exportacao ou recursos extras.

Etapas (autorizar uma por vez):

- [x] 1. Adaptar motor Java: retornar resultado estruturado e historico, aceitar E/D, distinguir aceitacao/rejeicao/limite e limitar passos. Manter fita expansivel e classes existentes. Revisar com testes unitarios de aceitacao, rejeicao, entrada vazia, movimentos nas duas extremidades, loop e limite exato.
- [x] 2. Integrar HTTP: endpoint com validacao dos cinco campos, estados e duplicidades; instancia independente por requisicao; mensagens de erro claras. Retirar execucao automatica do exemplo no main e disponibilizar o exemplo na pagina. Testar contrato HTTP, dados invalidos e independencia entre execucoes.
- [x] 3. Criar pagina e conectar ao endpoint: campos, tabela editavel, simulacao, historico, erro destacado, persistencia e restauracao. Exibir dados como texto, sem interpretar HTML. Revisar fluxo completo no navegador e ajustar apenas problemas encontrados.
- [ ] 4. Revisar entrega: executar testes existentes e novos, conferir exemplo inicial, alteracoes de transicoes, recarga, restauracao, rejeicao e limite. Documentar apenas o necessario para iniciar localmente e abrir a pagina.

Validacao da etapa 1:
- Motor e exemplo adaptados para E/D; resultado com status, motivo, estado/fita finais, total de transicoes e historico imutavel, incluindo configuracoes inicial e final. Cabecote usa indice zero na fita de cada configuracao.
- 13 testes JUnit aprovados: 12 novos testes do motor e o teste existente de contexto Spring. Cobrem exemplo (8 passos), rejeicao, entrada vazia, aceitacao imediata e sem consumir toda a entrada, expansao nas extremidades, direcao invalida, loop, aceitacao/rejeicao no limite exato e independencia entre maquinas.
- Compilacao com javac do Java 21 e execucao direta pelo JUnit Platform, usando dependencias existentes e arquivos auxiliares temporarios. Mockito carregado como agente nessa execucao.
- Limitacao do ambiente: gradlew test nao executou por acesso negado ao cache; tentativa com diretorio temporario tambem bloqueada na conexao ao daemon. Validacao substituida pela execucao direta acima, sem alterar configuracao ou dependencias do projeto. Execucao pelo Gradle permanece para conferencia em ambiente sem essas restricoes.
- Na conclusao da etapa 1, endpoint, validacao HTTP e pagina permaneciam pendentes. Exemplo ainda executava no main, conforme retirada prevista na etapa 2.

Validacao da etapa 2:
- POST /api/simular recebe entrada, estadoInicial, estadoAceitacao e transicoes (estadoAtual, simboloLido, novoEstado, simboloEscrito, direcao). Retorna o ResultadoSimulacao existente, incluindo historico inicial e final. Cada requisicao cria sua propria Fita e MaquinaTuring.
- Validacao manual no servidor, sem dependencias novas: campos obrigatorios, estados nao vazios, simbolos de um caractere compativel com o motor, E/D e determinismo. Entrada vazia e lista vazia sao validas. Dados invalidos e JSON ausente/malformado retornam HTTP 400 com campo erro. Simulacoes validas retornam HTTP 200; LIMITE_ATINGIDO continua distinto de REJEITADA.
- Main apenas inicia o Spring Boot. Pagina estatica inicial disponibiliza o exemplo completo; controles e integracao visual continuam na etapa 3.
- 49 testes JUnit aprovados: 13 existentes e 36 novos casos HTTP com contexto Spring e MockMvc. Cobrem contrato JSON, exemplo, entrada vazia, rejeicao, limite, campos invalidos, duplicidade, JSON invalido, independencia sequencial/concorrente e pagina estatica.
- gradlew test --no-daemon bloqueado por acesso negado ao lock do cache. Validacao substituida por compilacao javac Java 21 e execucao direta de toda a suite pelo JUnit Platform, usando dependencias locais e Mockito como agente. Sem alteracoes no build, caches ou dependencias; conferencia pelo Gradle permanece pendente em ambiente sem essa restricao.

Validacao da etapa 3:
- Pagina com campos globais, tabela editavel, adicionar/excluir transicoes, POST /api/simular, resultado e historico completo. Dados exibidos com textContent/value, sem interpretar HTML. Erros HTTP/conexao e limite destacados; limite nao e rejeicao.
- Configuracao salva no localStorage a cada alteracao; recarga recupera dados validos, ou usa o exemplo se ausentes/invalidos. Restauracao pede confirmacao, salva o exemplo e limpa resultados. Falha de armazenamento gera aviso. Edicoes limpam resultados antigos; controles ficam desabilitados durante a requisicao.
- node --check aprovado. Compilacao javac Java 21 e 49 testes JUnit aprovados via JUnit Platform com dependencias locais e Mockito como agente. Teste da pagina ajustado para conferir HTML e entrega de app.js/style.css. Sem dependencias novas ou alteracoes no motor, build e caches.
- Bloqueio inicial: Tomcat temporario nao iniciou por Permission denied: connect no loopback. Validacao retomada com servidor iniciado pelo usuario fora do sandbox. Gradle nao foi reexecutado pelo agente; bloqueio anterior permanece registrado acima.
- Navegador: exemplo aceito em 8 passos com 9 configuracoes; edicao de cadeia, estado de destino e direcao preservada na recarga; adicionar/excluir transicoes; duplicidade exibida como erro do servidor; rejeicao e entrada vazia; loop interrompido em 1000 passos com 1001 configuracoes e erro visualmente destacado, distinto de rejeicao. Edicoes limpam resultados anteriores.
- Restauracao: automacao da confirmacao interrompida pela ferramenta. Usuario concluiu o teste manual e confirmou persistencia ao recarregar e restauracao pelo botao. Validacao funcional da etapa 3 concluida com essa confirmacao; sem novos testes automatizados executados nesta retomada.
- Etapa 4 autorizada posteriormente; acompanhamento abaixo.
- Limpeza concluida: usuario removeu validacao-temporaria/ pelo terminal local; ausencia da pasta conferida pelo agente.

Revisao da etapa 4:
- Implementacao e testes existentes revisados; sem necessidade identificada de alterar o motor ou a interface. README.md adicionado com Java 21, bootRun, endereco local, uso basico e comando/relatorio de testes.
- Dois testes HTTP adicionados: alteracao dos campos de uma transicao com efeito no resultado, exclusao e nova execucao do exemplo sem contaminacao; encaminhamento de / para index.html.
- Usuario identificou falha de compilacao no set da lista do novo teste. Tipo explicitado como ArrayList<Object> para permitir substituir elementos copiados de List<?> sem cast nao verificado. Correcao validada na execucao Gradle abaixo.
- node --check src/main/resources/static/app.js e git diff --check executados e aprovados.
- Usuario executou .\gradlew.bat test e informou BUILD SUCCESSFUL. Relatorios XML conferidos pelo agente: 51 testes (12 motor, 38 HTTP, 1 contexto), sem falhas, erros ou ignorados. Aviso de compartilhamento de classes da JVM nao impediu a aprovacao.
- Usuario confirmou servidor iniciado. Nova tentativa de conferencia no navegador bloqueada por permissao salva/politica que nega acesso a http://localhost:8080. Sem tentativa de contorno. Validacao da etapa 3 permanece registrada, mas a nova conferencia desta etapa esta pendente.
- Pendencia: conferencia final de exemplo (aceita em 8 passos), edicao/adicao/exclusao de transicoes, recarga, restauracao, rejeicao e limite destacado. Pode ser concluida manualmente pelo usuario. Etapa permanece [ ] ate concluir a validacao.

Execucao: iniciar o Spring Boot pelo Gradle Wrapper (bootRun) com Java 21 e abrir http://localhost:8080. Instrucoes no README.md. Sem servidor separado para frontend.

Combinado com o usuario: quando necessario, solicitar que ele execute os testes pelo Gradle e inicie o servidor no terminal local. Nao recriar infraestrutura temporaria para contornar os bloqueios do ambiente.

Controle: antes de cada etapa, ler este arquivo e analisar o codigo atual. Iniciar apenas a etapa expressamente autorizada. Marcar [x] somente apos implementar, revisar e validar; registrar testes e pendencias de forma breve. Nao avancar automaticamente. Validacoes bloqueadas devem ficar registradas e impedem marcar a etapa como concluida.
