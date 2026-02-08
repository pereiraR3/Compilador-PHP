package compilador.gerador;

import compilador.Debug;
import compilador.arvore_sintatica_abstrata.*;
import compilador.semantico.Simbolo;
import compilador.semantico.TabelaSimbolos;
import compilador.semantico.TipoSimbolo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeradorCodigo {

    private final TabelaSimbolos tabelaGlobal;
    private TabelaSimbolos tabelaAtual;
    private final List<Instrucao> instrucoes;
    private final Map<String, FuncaoInfo> funcoesInfo;
    private int numVariaveisGlobais;

    private static class FuncaoInfo {
        final int enderecoInicio;

        FuncaoInfo(int enderecoInicio) {
            this.enderecoInicio = enderecoInicio;
        }
    }

    public GeradorCodigo(TabelaSimbolos tabelaGlobal) {
        this.tabelaGlobal = tabelaGlobal;
        this.tabelaAtual = tabelaGlobal;
        this.instrucoes = new ArrayList<>();
        this.funcoesInfo = new HashMap<>();
        this.numVariaveisGlobais = 0;
    }

    /**
     * Gera o codigo objeto a partir da AST e retorna a lista de instrucoes
     */
    public List<Instrucao> gerar(Programa ast) {
        Debug.gerador("=== Iniciando Geracao de Codigo ===");
        instrucoes.clear();
        emitir("INPP", null);
        gerarPrograma(ast);
        emitir("PARA", null);
        Debug.gerador("=== Geracao de Codigo Concluida: " + instrucoes.size() + " instrucoes ===");
        return new ArrayList<>(instrucoes);
    }

    /**
     * Emite uma instrucao sem comentario
     */
    private int emitir(String mnemonico, String argumento) {
        int endereco = instrucoes.size();
        instrucoes.add(new Instrucao(mnemonico, argumento, null));
        Debug.instrucao(endereco, mnemonico, argumento);
        return endereco;
    }

    /**
     * Emite uma instrucao com comentario
     */
    private int emitir(String mnemonico, String argumento, String comentario) {
        int endereco = instrucoes.size();
        instrucoes.add(new Instrucao(mnemonico, argumento, comentario));
        Debug.instrucao(endereco, mnemonico, argumento);
        return endereco;
    }

    /**
     * Retorna o endereco (indice) da proxima instrucao a emitir
     */
    private int enderecoAtual() {
        return instrucoes.size();
    }

    /**
     * Corrige o argumento de um desvio para um novo endereco
     */
    private void corrigirEndereco(int indice, int novoEndereco) {
        instrucoes.get(indice).argumento = String.valueOf(novoEndereco);
    }

    /**
     * Gera codigo para o programa principal
     */
    private void gerarPrograma(Programa programa) {
        numVariaveisGlobais = contarVariaveis(programa.corpo);
        gerarCorpo(programa.corpo, true);
    }

    /**
     * Gera codigo para um corpo (global ou de funcao)
     */
    private int gerarCorpo(Corpo corpo, boolean eGlobal) {
        int numVariaveis = 0;

        if (!eGlobal) {
            for (Declaracao decl : corpo.declaracoes) {
                if (decl instanceof DeclaracaoVariavel) {
                    DeclaracaoVariavel declVar = (DeclaracaoVariavel) decl;
                    if (tabelaAtual.buscarLocal(declVar.nome) == null) {
                        Simbolo simbolo = new Simbolo(declVar.nome, TipoSimbolo.VARIAVEL, declVar.endereco, null, tabelaAtual.nomeEscopo);
                        try {
                            tabelaAtual.adicionar(simbolo);
                        } catch (Exception e) {
                            // Ignora a duplicacao de simbolos
                        }
                        if (declVar.endereco + 1 > tabelaAtual.proximoEndereco) {
                            tabelaAtual.proximoEndereco = declVar.endereco + 1;
                        }
                    }
                }
            }
        }

        for (Declaracao decl : corpo.declaracoes) {
            if (decl instanceof DeclaracaoVariavel) {
                emitir("ALME", "1");
                numVariaveis += 1;
            }
        }

        // Primeiro, gerar todas as funções (com DSVIs que serão corrigidos depois)
        List<Integer> indicesDesvioFuncoes = new ArrayList<>();
        for (Declaracao decl : corpo.declaracoes) {
            if (decl instanceof DeclaracaoFuncao) {
                int indiceDesvio = gerarDeclaracaoFuncaoComDesvio((DeclaracaoFuncao) decl);
                indicesDesvioFuncoes.add(indiceDesvio);
            }
        }

        // Corrigir todos os DSVIs para apontar para depois de todas as funções
        int enderecoAposTodasFuncoes = enderecoAtual();
        for (int indiceDesvio : indicesDesvioFuncoes) {
            corrigirEndereco(indiceDesvio, enderecoAposTodasFuncoes);
        }

        // Gerar declarações de variáveis
        for (Declaracao decl : corpo.declaracoes) {
            if (decl instanceof DeclaracaoVariavel) {
                gerarDeclaracaoVariavel((DeclaracaoVariavel) decl);
            }
        }

        for (Comando cmd : corpo.comandos) {
            gerarComando(cmd);
        }

        return numVariaveis;
    }

    /**
     * Gera inicializacao de variavel, quando houver expressao
     */
    private void gerarDeclaracaoVariavel(DeclaracaoVariavel decl) {
        if (decl.expressaoInicial != null && !ehZeroLiteral(decl.expressaoInicial)) {
            emitirAtribuicao(decl.nome, decl.expressaoInicial);
        }
    }

    /**
     * Gera o corpo da funcao e retorna o indice do DSVI para patch posterior
     */
    private int gerarDeclaracaoFuncaoComDesvio(DeclaracaoFuncao decl) {
        Debug.gerador("--- Gerando funcao: " + decl.nome + " ---");
        int indiceDesvio = emitir("DSVI", "0", "#funcao " + decl.nome);

        int enderecoInicio = enderecoAtual();
        Debug.gerador("Funcao " + decl.nome + " inicia no endereco " + enderecoInicio);
        funcoesInfo.put(decl.nome, new FuncaoInfo(enderecoInicio));

        Simbolo simbolo = tabelaGlobal.buscar(decl.nome);
        if (simbolo != null) {
            simbolo.enderecoInicio = enderecoInicio;
        }

        TabelaSimbolos tabelaFuncao = new TabelaSimbolos(tabelaGlobal, decl.nome);
        for (int i = 0; i < decl.parametros.size(); i++) {
            String parametro = decl.parametros.get(i);
            Simbolo simboloParam = new Simbolo(parametro, TipoSimbolo.PARAMETRO, i, null, decl.nome);
            try {
                tabelaFuncao.adicionar(simboloParam);
            } catch (Exception e) {
                // Ignorar duplicacao de simbolos
            }
        }
        tabelaFuncao.proximoEndereco = decl.parametros.size();

        TabelaSimbolos tabelaAnterior = tabelaAtual;
        tabelaAtual = tabelaFuncao;

        int numLocais = gerarCorpo(decl.corpo, false);
        int numParametros = decl.parametros.size();
        int totalDesalocar = numLocais + numParametros;

        if (totalDesalocar > 0) {
            emitir("DESM", String.valueOf(totalDesalocar));
        }

        emitir("RTPR", null);

        tabelaAtual = tabelaAnterior;

        // Retorna o índice do DSVI para correção posterior
        return indiceDesvio;
    }

    /**
     * Gera codigo para um comando da linguagem
     */
    private void gerarComando(Comando cmd) {
        if (cmd instanceof ComandoEcho) {
            gerarComandoEcho((ComandoEcho) cmd);
        } else if (cmd instanceof ComandoIf) {
            gerarComandoIf((ComandoIf) cmd);
        } else if (cmd instanceof ComandoWhile) {
            gerarComandoWhile((ComandoWhile) cmd);
        } else if (cmd instanceof ComandoAtribuicao) {
            gerarComandoAtribuicao((ComandoAtribuicao) cmd);
        } else if (cmd instanceof ComandoChamadaFuncao) {
            gerarComandoChamadaFuncao((ComandoChamadaFuncao) cmd);
        }
    }

    /**
     * Gera codigo para o comando echo
     */
    private void gerarComandoEcho(ComandoEcho cmd) {
        gerarExpressao(cmd.expressao);
        emitir("IMPR", null);
    }

    /**
     * Gera codigo para o comando if/else
     */
    private void gerarComandoIf(ComandoIf cmd) {
        gerarCondicao(cmd.condicao);
        int indiceDsvf = emitir("DSVF", "0");

        for (Comando c : cmd.blocoIf) {
            gerarComando(c);
        }

        if (cmd.blocoElse != null) {
            int indiceDsvi = emitir("DSVI", "0");
            corrigirEndereco(indiceDsvf, enderecoAtual());

            for (Comando c : cmd.blocoElse) {
                gerarComando(c);
            }

            corrigirEndereco(indiceDsvi, enderecoAtual());
        } else {
            corrigirEndereco(indiceDsvf, enderecoAtual());
        }
    }

    /**
     * Gera codigo para o comando while
     */
    private void gerarComandoWhile(ComandoWhile cmd) {
        int enderecoInicio = enderecoAtual();

        gerarCondicao(cmd.condicao);
        int indiceDsvf = emitir("DSVF", "0");

        for (Comando c : cmd.bloco) {
            gerarComando(c);
        }

        emitir("DSVI", String.valueOf(enderecoInicio));
        corrigirEndereco(indiceDsvf, enderecoAtual());
    }

    /**
     * Gera codigo para atribuicao
     */
    private void gerarComandoAtribuicao(ComandoAtribuicao cmd) {
        emitirAtribuicao(cmd.variavel, cmd.expressao);
    }

    /**
     * Emite o codigo de atribuicao para uma variavel
     */
    private void emitirAtribuicao(String nomeVariavel, Expressao expressao) {
        if (expressao instanceof ExpressaoLeitura) {
            emitir("LEIT", null, "#" + nomeVariavel);
        } else {
            gerarExpressao(expressao);
        }
        int endereco = obterEnderecoVariavel(nomeVariavel);
        emitir("ARMZ", String.valueOf(endereco));
    }

    /**
     * Gera codigo para chamada de funcao em comando
     */
    private void gerarComandoChamadaFuncao(ComandoChamadaFuncao cmd) {
        Debug.gerador("Gerando chamada de funcao: " + cmd.nomeFuncao);
        Simbolo simbolo = tabelaGlobal.buscar(cmd.nomeFuncao);
        if (simbolo == null) {
            Debug.gerador("ERRO: Funcao " + cmd.nomeFuncao + " nao encontrada!");
            return;
        }

        int indicePusher = emitir("PUSHER", "0");

        for (int i = 0; i < cmd.argumentos.size(); i++) {
            Debug.gerador("  Argumento " + i);
            Expressao argumento = cmd.argumentos.get(i);
            if (argumento instanceof ExpressaoVariavel) {
                int endereco = obterEnderecoVariavel(((ExpressaoVariavel) argumento).nome);
                emitir("PARAM", String.valueOf(endereco));
            } else {
                erroArgumentoNaoVariavel(cmd.nomeFuncao);
            }
        }

        int enderecoFuncao = 0;
        FuncaoInfo infoFuncao = funcoesInfo.get(cmd.nomeFuncao);
        if (infoFuncao != null) {
            enderecoFuncao = infoFuncao.enderecoInicio;
        }
        if (enderecoFuncao == 0 && simbolo.enderecoInicio > 0) {
            enderecoFuncao = simbolo.enderecoInicio;
        }
        Debug.gerador("  Endereco da funcao: " + enderecoFuncao);

        emitir("CHPR", String.valueOf(enderecoFuncao));

        int endRetorno = enderecoAtual();
        Debug.gerador("  Endereco de retorno: " + endRetorno);
        corrigirEndereco(indicePusher, endRetorno);
    }

    /**
     * Gera codigo para uma condicao (expressao booleana)
     */
    private void gerarCondicao(Condicao cond) {
        gerarExpressao(cond.expressao);
    }

    /**
     * Gera codigo para uma expressao
     */
    private void gerarExpressao(Expressao expr) {
        if (expr instanceof ExpressaoLeitura) {
            emitir("LEIT", null);
        } else if (expr instanceof ExpressaoBinaria) {
            gerarExpressaoBinaria((ExpressaoBinaria) expr);
        } else if (expr instanceof ExpressaoUnaria) {
            gerarExpressaoUnaria((ExpressaoUnaria) expr);
        } else if (expr instanceof ExpressaoVariavel) {
            int endereco = obterEnderecoVariavel(((ExpressaoVariavel) expr).nome);
            emitir("CRVL", String.valueOf(endereco));
        } else if (expr instanceof ExpressaoNumero) {
            emitir("CRCT", formatarNumero(((ExpressaoNumero) expr).valor));
        } else if (expr instanceof ExpressaoChamadaFuncao) {
            gerarExpressaoChamadaFuncao((ExpressaoChamadaFuncao) expr);
        }
    }

    /**
     * Gera codigo para chamada de funcao usada como expressao
     */
    private void gerarExpressaoChamadaFuncao(ExpressaoChamadaFuncao expr) {
        Simbolo simbolo = tabelaGlobal.buscar(expr.nomeFuncao);
        if (simbolo == null) {
            return;
        }

        int indicePusher = emitir("PUSHER", "0");

        for (int i = 0; i < expr.argumentos.size(); i++) {
            Expressao argumento = expr.argumentos.get(i);
            if (argumento instanceof ExpressaoVariavel) {
                int endereco = obterEnderecoVariavel(((ExpressaoVariavel) argumento).nome);
                emitir("PARAM", String.valueOf(endereco));
            } else {
                erroArgumentoNaoVariavel(expr.nomeFuncao);
            }
        }

        int enderecoFuncao = 0;
        FuncaoInfo infoFuncao = funcoesInfo.get(expr.nomeFuncao);
        if (infoFuncao != null) {
            enderecoFuncao = infoFuncao.enderecoInicio;
        }
        if (enderecoFuncao == 0 && simbolo.enderecoInicio > 0) {
            enderecoFuncao = simbolo.enderecoInicio;
        }

        emitir("CHPR", String.valueOf(enderecoFuncao));

        corrigirEndereco(indicePusher, enderecoAtual());
    }

    /**
     * Gera codigo para uma expressao binaria
     */
    private void gerarExpressaoBinaria(ExpressaoBinaria expr) {
        gerarExpressao(expr.esquerda);
        gerarExpressao(expr.direita);

        switch (expr.operador) {
            case "+":
                emitir("SOMA", null);
                break;
            case "-":
                emitir("SUBT", null);
                break;
            case "*":
                emitir("MULT", null);
                break;
            case "/":
                emitir("DIVI", null);
                break;
            case "==":
                emitir("CPIG", null);
                break;
            case "!=":
                emitir("CDES", null);
                break;
            case ">=":
                emitir("CMAI", null);
                break;
            case "<=":
                emitir("CPMI", null);
                break;
            case ">":
                emitir("CPMA", null);
                break;
            case "<":
                emitir("CPME", null);
                break;
            case "&&":
                emitir("CONJ", null);
                break;
            case "||":
                emitir("DISJ", null);
                break;
            default:
                break;
        }
    }

    /**
     * Gera codigo para uma expressao unaria
     */
    private void gerarExpressaoUnaria(ExpressaoUnaria expr) {
        if ("-".equals(expr.operador)) {
            gerarExpressao(expr.operando);
            emitir("INVE", null);
        } else if ("!".equals(expr.operador)) {
            gerarExpressao(expr.operando);
            emitir("NEGA", null);
        } else {
            gerarExpressao(expr.operando);
        }
    }

    /**
     * Resolve o endereco de uma variavel no escopo atual
     */
    private int obterEnderecoVariavel(String nome) {
        Simbolo simbolo = tabelaAtual.buscar(nome);
        if (simbolo != null) {
            if ("global".equals(simbolo.escopo)) {
                return simbolo.endereco;
            }
            return numVariaveisGlobais + simbolo.endereco;
        }
        throw new IllegalStateException("Variavel nao encontrada na tabela de simbolos: " + nome);
    }

    /**
     * Conta variaveis declaradas em um corpo
     */
    private int contarVariaveis(Corpo corpo) {
        int total = 0;
        for (Declaracao decl : corpo.declaracoes) {
            if (decl instanceof DeclaracaoVariavel) {
                total += 1;
            }
        }
        return total;
    }

    /**
     * Verifica se a expressao e o literal zero
     */
    private boolean ehZeroLiteral(Expressao expr) {
        if (expr instanceof ExpressaoNumero) {
            double valor = ((ExpressaoNumero) expr).valor;
            return Math.abs(valor) < 1e-9;
        }
        return false;
    }

    /**
     * Formata numero para o codigo objeto (remove .0 quando inteiro)
     */
    private String formatarNumero(double valor) {
        if (Math.rint(valor) == valor) {
            return String.valueOf((long) valor);
        }
        return String.valueOf(valor);
    }

    /**
     * Lanca erro quando argumentos nao sao variaveis simples
     */
    private void erroArgumentoNaoVariavel(String nomeFuncao) {
        throw new IllegalStateException("Chamada de funcao '" + nomeFuncao + "' requer argumentos simples (variaveis) no modo Aula13");
    }

    /**
     * Exporta o codigo objeto para um arquivo
     */
    public void exportar(String caminho) throws IOException {
        Path path = Path.of(caminho);
        List<String> linhas = new ArrayList<>();
        for (Instrucao instrucao : instrucoes) {
            linhas.add(instrucao.toString());
        }
        Files.write(path, linhas, StandardCharsets.UTF_8);
    }

}
