package compilador.semantico;

import compilador.Debug;
import compilador.arvore_sintatica_abstrata.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executa a analise semantica e gera a tabela de simbolos
 */
public class AnalisadorSemantico {
    
    private TabelaSimbolos tabelaGlobal;
    private TabelaSimbolos tabelaAtual;
    private List<String> erros;
    private Map<String, TabelaSimbolos> tabelasPorEscopo;

    public AnalisadorSemantico() {
        this.tabelaGlobal = null;
        this.tabelaAtual = null;
        this.erros = new ArrayList<>();
        this.tabelasPorEscopo = new HashMap<>();
    }

    /**
     * Analisa o programa e retorna a tabela de simbolos global
     */
    public TabelaSimbolos analisar(Programa ast) throws ErroSemantico {
        Debug.semantico("=== Iniciando Analise Semantica ===");
        tabelaGlobal = new TabelaSimbolos(null, "global");
        tabelaAtual = tabelaGlobal;
        erros = new ArrayList<>();
        tabelasPorEscopo = new HashMap<>();
        tabelasPorEscopo.put("global", tabelaGlobal);

        analisarPrograma(ast);

        if (!erros.isEmpty()) {
            Debug.semantico("Erros encontrados: " + erros.size());
            throw new ErroSemantico(String.join("\n", erros));
        }

        Debug.semantico("=== Analise Semantica Concluida ===");
        return tabelaGlobal;
    }

    /**
     * Retorna uma copia das tabelas de simbolos por escopo
     */
    public Map<String, TabelaSimbolos> getTabelasPorEscopo() {
        return new HashMap<>(tabelasPorEscopo);
    }

    /**
     * Registra uma mensagem de erro semantico
     */
    private void erro(String mensagem) {
        erros.add(mensagem);
    }

    /**
     * Analisa a estrutura principal do programa
     */
    private void analisarPrograma(Programa programa) {
        analisarCorpo(programa.corpo);
    }

    /**
     * Analisa declaracoes e comandos de um corpo
     */
    private void analisarCorpo(Corpo corpo) {
        for (Declaracao decl : corpo.declaracoes) {
            analisarDeclaracao(decl);
        }

        for (Comando cmd : corpo.comandos) {
            analisarComando(cmd);
        }
    }

    /**
     * Encaminha a analise de uma declaracao
     */
    private void analisarDeclaracao(Declaracao decl) {
        if (decl instanceof DeclaracaoVariavel) {
            analisarDeclaracaoVariavel((DeclaracaoVariavel) decl);
        } else if (decl instanceof DeclaracaoFuncao) {
            analisarDeclaracaoFuncao((DeclaracaoFuncao) decl);
        }
    }

    /**
     * Analisa uma declaracao de variavel, incluindo inicializacao se existir
     */
    private void analisarDeclaracaoVariavel(DeclaracaoVariavel decl) {
        String nome = decl.nome;
        Debug.semantico("Analisando declaracao de variavel: " + nome + " (escopo: " + tabelaAtual.nomeEscopo + ")");

        if (tabelaAtual.buscarLocal(nome) != null) {
            erro("Variavel '" + nome + "' ja declarada neste escopo");
            return;
        }

        int endereco = tabelaAtual.alocarEndereco();
        decl.endereco = endereco;
        Debug.semantico("  Endereco alocado: " + endereco);

        Simbolo simbolo = new Simbolo(nome, TipoSimbolo.VARIAVEL, endereco, null, tabelaAtual.nomeEscopo);

        try {
            tabelaAtual.adicionar(simbolo);
            Debug.semantico("  Simbolo adicionado a tabela");
        } catch (ErroTabelaSimbolos e) {
            erro(e.getMessage());
        }

        if (decl.expressaoInicial != null) {
            Debug.semantico("  Analisando expressao inicial");
            analisarExpressao(decl.expressaoInicial);
        }
    }

    /**
     * Analisa uma declaracao de funcao e o seu corpo
     */
    private void analisarDeclaracaoFuncao(DeclaracaoFuncao decl) {
        String nome = decl.nome;
        Debug.semantico("Analisando declaracao de funcao: " + nome);

        if (tabelaGlobal.buscarLocal(nome) != null) {
            erro("Funcao '" + nome + "' ja declarada");
            return;
        }

        Simbolo simboloFuncao = new Simbolo(nome, TipoSimbolo.FUNCAO, -1, decl.parametros, "global");
        Debug.semantico("  Parametros: " + decl.parametros);

        try {
            tabelaGlobal.adicionar(simboloFuncao);
            Debug.semantico("  Funcao adicionada a tabela global");
        } catch (ErroTabelaSimbolos e) {
            erro(e.getMessage());
        }

        TabelaSimbolos tabelaFuncao = new TabelaSimbolos(tabelaGlobal, nome);
        Debug.semantico("  Entrando no escopo da funcao: " + nome);
        tabelasPorEscopo.put(nome, tabelaFuncao);

        for (String param : decl.parametros) {
            int endereco = tabelaFuncao.alocarEndereco();
            Simbolo simboloParam = new Simbolo(param, TipoSimbolo.PARAMETRO, endereco, null, nome);
            Debug.semantico("  Parametro " + param + " -> endereco " + endereco);
            try {
                tabelaFuncao.adicionar(simboloParam);
            } catch (ErroTabelaSimbolos e) {
                erro(e.getMessage());
            }
        }

        TabelaSimbolos tabelaAnterior = tabelaAtual;
        tabelaAtual = tabelaFuncao;

        analisarCorpo(decl.corpo);

        simboloFuncao.numVariaveisLocais = tabelaFuncao.proximoEndereco;
        Debug.semantico("  Variaveis locais da funcao: " + simboloFuncao.numVariaveisLocais);
        Debug.semantico("  Saindo do escopo da funcao: " + nome);

        tabelaAtual = tabelaAnterior;
    }

    /**
     * Encaminha a analise de um comando
     */
    private void analisarComando(Comando cmd) {
        if (cmd instanceof ComandoEcho) {
            analisarComandoEcho((ComandoEcho) cmd);
        } else if (cmd instanceof ComandoIf) {
            analisarComandoIf((ComandoIf) cmd);
        } else if (cmd instanceof ComandoWhile) {
            analisarComandoWhile((ComandoWhile) cmd);
        } else if (cmd instanceof ComandoAtribuicao) {
            analisarComandoAtribuicao((ComandoAtribuicao) cmd);
        } else if (cmd instanceof ComandoChamadaFuncao) {
            analisarComandoChamadaFuncao((ComandoChamadaFuncao) cmd);
        }
    }

    /**
     * Analisa um comando de escrita
     */
    private void analisarComandoEcho(ComandoEcho cmd) {
        analisarExpressao(cmd.expressao);
    }

    /**
     * Analisa um comando condicional
     */
    private void analisarComandoIf(ComandoIf cmd) {
        analisarCondicao(cmd.condicao);
        for (Comando c : cmd.blocoIf) {
            analisarComando(c);
        }
        if (cmd.blocoElse != null) {
            for (Comando c : cmd.blocoElse) {
                analisarComando(c);
            }
        }
    }

    /**
     * Analisa um comando de repeticao
     */
    private void analisarComandoWhile(ComandoWhile cmd) {
        analisarCondicao(cmd.condicao);
        for (Comando c : cmd.bloco) {
            analisarComando(c);
        }
    }

    /**
     * Analisa um comando de atribuicao
     */
    private void analisarComandoAtribuicao(ComandoAtribuicao cmd) {
        verificarVariavelDeclarada(cmd.variavel);
        analisarExpressao(cmd.expressao);
    }

    /**
     * Analisa um comando de chamada de funcao
     */
    private void analisarComandoChamadaFuncao(ComandoChamadaFuncao cmd) {
        validarChamadaFuncao(cmd.nomeFuncao, cmd.argumentos);
    }

    /**
     * Analisa uma condicao
     */
    private void analisarCondicao(Condicao cond) {
        analisarExpressao(cond.expressao);
    }

    /**
     * Analisa uma expressao e suas subexpressoes
     */
    private void analisarExpressao(Expressao expr) {
        if (expr instanceof ExpressaoLeitura) {
            return;
        }
        if (expr instanceof ExpressaoBinaria) {
            ExpressaoBinaria bin = (ExpressaoBinaria) expr;
            analisarExpressao(bin.esquerda);
            analisarExpressao(bin.direita);
        } else if (expr instanceof ExpressaoUnaria) {
            ExpressaoUnaria un = (ExpressaoUnaria) expr;
            analisarExpressao(un.operando);
        } else if (expr instanceof ExpressaoVariavel) {
            verificarVariavelDeclarada(((ExpressaoVariavel) expr).nome);
        } else if (expr instanceof ExpressaoNumero) {
            return;
        } else if (expr instanceof ExpressaoChamadaFuncao) {
            analisarExpressaoChamadaFuncao((ExpressaoChamadaFuncao) expr);
        }
    }

    /**
     * Analisa uma expressao de chamada de funcao
     */
    private void analisarExpressaoChamadaFuncao(ExpressaoChamadaFuncao expr) {
        validarChamadaFuncao(expr.nomeFuncao, expr.argumentos);
    }

    /**
     * Valida a existencia da funcao, sua assinatura e os argumentos
     */
    private void validarChamadaFuncao(String nomeFuncao, List<Expressao> argumentos) {
        Simbolo simbolo = tabelaGlobal.buscar(nomeFuncao);

        if (simbolo == null) {
            erro("Funcao '" + nomeFuncao + "' nao declarada");
            return;
        }

        if (simbolo.tipo != TipoSimbolo.FUNCAO) {
            erro("'" + nomeFuncao + "' nao e uma funcao");
            return;
        }

        int numParams = simbolo.parametros.size();
        int numArgs = argumentos.size();

        if (numArgs != numParams) {
            erro("Funcao '" + nomeFuncao + "' espera " + numParams + " argumento(s), mas recebeu " + numArgs);
        }

        for (Expressao arg : argumentos) {
            analisarExpressao(arg);
        }
    }

    /**
     * Verifica se a variavel foi declarada no escopo atual ou em escopos pais
     */
    private Simbolo verificarVariavelDeclarada(String nome) {
        Simbolo simbolo = tabelaAtual.buscar(nome);

        if (simbolo == null) {
            erro("Variavel '" + nome + "' nao declarada");
            return null;
        }

        if (simbolo.tipo == TipoSimbolo.FUNCAO) {
            erro("'" + nome + "' e uma funcao, nao uma variavel");
            return null;
        }

        return simbolo;
    }

    /**
     * Retorna o endereco de uma variavel ou parametro, se existir
     */
    public int obterEnderecoVariavel(String nome) {
        Simbolo simbolo = tabelaAtual.buscar(nome);
        if (simbolo != null && (simbolo.tipo == TipoSimbolo.VARIAVEL || simbolo.tipo == TipoSimbolo.PARAMETRO)) {
            return simbolo.endereco;
        }
        return -1;
    }

    /**
     * Retorna o simbolo de uma funcao, se existir
     */
    public Simbolo obterInfoFuncao(String nome) {
        Simbolo simbolo = tabelaGlobal.buscar(nome);
        if (simbolo != null && simbolo.tipo == TipoSimbolo.FUNCAO) {
            return simbolo;
        }
        return null;
    }
}
