# Planejamento temporario

Status: somente planejamento; nenhuma etapa de implementacao autorizada.

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

- [ ] 1. Adaptar motor Java: retornar resultado estruturado e historico, aceitar E/D, distinguir aceitacao/rejeicao/limite e limitar passos. Manter fita expansivel e classes existentes. Revisar com testes unitarios de aceitacao, rejeicao, entrada vazia, movimentos nas duas extremidades, loop e limite exato.
- [ ] 2. Integrar HTTP: endpoint com validacao dos cinco campos, estados e duplicidades; instancia independente por requisicao; mensagens de erro claras. Retirar execucao automatica do exemplo no main e disponibilizar o exemplo na pagina. Testar contrato HTTP, dados invalidos e independencia entre execucoes.
- [ ] 3. Criar pagina e conectar ao endpoint: campos, tabela editavel, simulacao, historico, erro destacado, persistencia e restauracao. Exibir dados como texto, sem interpretar HTML. Revisar fluxo completo no navegador e ajustar apenas problemas encontrados.
- [ ] 4. Revisar entrega: executar testes existentes e novos, conferir exemplo inicial, alteracoes de transicoes, recarga, restauracao, rejeicao e limite. Documentar apenas o necessario para iniciar localmente e abrir a pagina.

Execucao prevista: iniciar o Spring Boot pelo Gradle Wrapper (bootRun) com Java 21 e abrir http://localhost:8080. Sem servidor separado para frontend.

Controle: antes de cada etapa, ler este arquivo e analisar o codigo atual. Iniciar apenas a etapa expressamente autorizada. Marcar [x] somente apos implementar, revisar e validar; registrar testes e pendencias de forma breve. Nao avancar automaticamente. Validacoes bloqueadas devem ficar registradas e impedem marcar a etapa como concluida.
