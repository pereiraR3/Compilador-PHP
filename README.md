# Compilador-PHP

Compilador da linguagem PHP, com etapas de analise lexico, sintatico, semantico, geracao de codigo objeto e execucao na Maquina Hipotetica.

**Requisitos**
- Java 17
- `bash` para `run_interativo.sh`

**Estrutura de Pastas**
- `src/main/java/compilador/lexico` analisador lexico e definicoes de tokens
- `src/main/java/compilador/sintatico` analisador sintatico e erros sintaticos
- `src/main/java/compilador/semantico` analisador semantico e tabela de simbolos
- `src/main/java/compilador/gerador` gerador de codigo objeto
- `src/main/java/compilador/arvore_sintatica_abstrata` nos da AST
- `src/main/java/compilador/MaqHipo` Maquina Hipotetica e executavel
- `src/main/java/compilador` classes auxiliares e `Main`
- `dados` programas de exemplo em `.php`
- `saidas` arquivos gerados (tokens, AST, tabela de simbolos, codigo objeto, erros)
- `out` classes compiladas (`.class`)
- `run_interativo.sh` script interativo para compilar e executar

**Compilacao do Projeto**
```bash
javac -d out $(find src/main/java -name "*.java")
```

**Execucao do Compilador (todas as etapas)**
O `compilador.Main` executa as etapas em ordem: lexico -> sintatico -> semantico -> geracao de codigo objeto.
```bash
java -cp out compilador.Main dados/teste_01.php
```
Opcionalmente, informe o nome do arquivo de saida (o arquivo final sempre fica em `saidas/`).
```bash
java -cp out compilador.Main dados/teste_01.php codigo.objeto.txt
```

**Execucao da Maquina Hipotetica (MaqHipo)**
Para executar o codigo objeto gerado:
```bash
java -cp out compilador.MaqHipo.MaquinaHipoteticaMain saidas/codigo.objeto.txt
```
Se nenhum caminho for informado, o padrao e `saidas/codigo.objeto.txt`.

**Execucao Interativa (compilar + opcionalmente executar MaqHipo)**
```bash
./run_interativo.sh
```

**Detalhamento das Etapas**
Cada etapa gera saidas em `saidas/`.

1. **Analise Lexica**
Gera `saidas/tokens.txt`. Erros sao salvos em `saidas/erros_lexicos.txt`.

1. **Analise Sintatica**
Gera `saidas/ast.txt`. Erros sao salvos em `saidas/erros_sintaticos.txt`.

1. **Analise Semantica**
Gera `saidas/tabela_simbolos.txt`. Erros sao salvos em `saidas/erros_semanticos.txt`.

1. **Geracao de Codigo Objeto**
Gera `saidas/codigo.objeto.txt` (ou o nome informado).

**Opcoes de Debug**
Ative logs detalhados por etapa usando:
```bash
java -cp out compilador.Main -debug dados/teste_01.php
```
Opcoes disponiveis:
- `-debug` (todas as etapas)
- `-debug-lexico`
- `-debug-sintatico`
- `-debug-semantico`
- `-debug-gerador`
- `-debug-maquina-hipotetica`

A MaqHipo tambem aceita debug:
```bash
java -cp out compilador.MaqHipo.MaquinaHipoteticaMain -debug saidas/codigo.objeto.txt
```

**Saidas Geradas**
- `saidas/tokens.txt` lista de tokens
- `saidas/ast.txt` arvore sintatica abstrata
- `saidas/tabela_simbolos.txt` tabela de simbolos por escopo
- `saidas/codigo.objeto.txt` codigo objeto para a MaqHipo
- `saidas/erros_lexicos.txt` erros lexico
- `saidas/erros_sintaticos.txt` erros sintaticos
- `saidas/erros_semanticos.txt` erros semanticos
