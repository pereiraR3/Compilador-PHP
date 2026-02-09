# Compilador-PHP

Compilador de uma linguagem similar a PHP, com etapas de analise lexico, sintatico, semantico, geracao de codigo objeto e execucao na Maquina Hipotetica.

**Requisitos**
- Java 17
- `bash` para `run_interativo.sh`

**Como compilar**
```bash
javac -d out $(find src/main/java -name "*.java")
```

**Como executar**
```bash
java -cp out compilador.Main dados/teste_01.php
```
Opcionalmente, informe o nome do arquivo de saida (o arquivo final sempre fica em `saidas/`):
```bash
java -cp out compilador.Main dados/teste_01.php codigo.objeto.txt
```

**Maquina Hipotetica (MaqHipo)**
```bash
java -cp out compilador.MaqHipo.MaquinaHipoteticaMain saidas/codigo.objeto.txt
```

**Execucao interativa**
```bash
./run_interativo.sh
```

**Saidas**
- `saidas/tokens.txt` tokens reconhecidos
- `saidas/ast.txt` arvore sintatica abstrata
- `saidas/tabela_simbolos.txt` tabela de simbolos
- `saidas/codigo.objeto.txt` codigo objeto
- `saidas/erros_lexicos.txt` erros lexico
- `saidas/erros_sintaticos.txt` erros sintaticos
- `saidas/erros_semanticos.txt` erros semanticos

**Debug**
```bash
java -cp out compilador.Main -debug dados/teste_01.php
```
Opcoes:
- `-debug` (todas as etapas)
- `-debug-lexico`
- `-debug-sintatico`
- `-debug-semantico`
- `-debug-gerador`
- `-debug-maquina-hipotetica`

MaqHipo com debug:
```bash
java -cp out compilador.MaqHipo.MaquinaHipoteticaMain -debug saidas/codigo.objeto.txt
```

**Estrutura**
- `src/main/java/compilador/lexico` analisador lexico e tokens
- `src/main/java/compilador/sintatico` analisador sintatico
- `src/main/java/compilador/semantico` analisador semantico e tabela de simbolos
- `src/main/java/compilador/gerador` gerador de codigo objeto
- `src/main/java/compilador/arvore_sintatica_abstrata` nos da AST
- `src/main/java/compilador/MaqHipo` Maquina Hipotetica
- `src/main/java/compilador` classes auxiliares e `Main`
- `dados` programas de exemplo
- `saidas` arquivos gerados
- `out` classes compiladas
- `run_interativo.sh` script interativo
