# Simulador de máquina de Turing

Requisito: JDK 21. Use o Gradle Wrapper incluído no projeto; não é necessário instalar Gradle separadamente. A primeira execução precisa de internet para baixar as dependências.

No terminal, na pasta do projeto:

```powershell
.\gradlew.bat bootRun
```

Após o servidor iniciar, abra [http://localhost:8080](http://localhost:8080). A página e a API são servidas pelo Spring Boot, sem servidor separado para o frontend. Para encerrar, pressione `Ctrl+C` no terminal.

Configure a cadeia, os estados e as transições e clique em **Simular**. `B` representa branco e `E`/`D` representam esquerda/direita. O exemplo inicial é aceito em 8 passos. O limite é de 1000 transições; se a máquina precisar continuar, a página exibe um erro de possível loop.

As configurações são salvas neste navegador. **Restaurar padrão** recupera o exemplo inicial após confirmação.

Para executar os testes automatizados:

```powershell
.\gradlew.bat test
```

O relatório fica em `build/reports/tests/test/index.html`.
