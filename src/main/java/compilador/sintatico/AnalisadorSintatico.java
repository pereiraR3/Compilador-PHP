package compilador.sintatico;

import compilador.Debug;
import compilador.arvore_sintatica_abstrata.*;
import compilador.lexico.ErroLexico;
import compilador.lexico.AnalisadorLexico;
import compilador.lexico.Token;
import compilador.lexico.TipoToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalisadorSintatico {
    
    private static final TipoToken[] TIPOS_COMANDO = new TipoToken[]{
        TipoToken.ECHO,
        TipoToken.IF,
        TipoToken.WHILE,
        TipoToken.VARIAVEL,
        TipoToken.IDENT
    };

    private final AnalisadorLexico analisadorLexico;
    private Token tokenAtual;

    public AnalisadorSintatico(AnalisadorLexico analisadorLexico) throws ErroLexico {
        this.analisadorLexico = analisadorLexico;
        this.tokenAtual = this.analisadorLexico.proximoToken();
    }
    private void erro(String mensagem) throws ErroSintatico {
        throw new ErroSintatico(mensagem, tokenAtual);
    }

    private Token consumir(TipoToken tipoEsperado) throws ErroSintatico, ErroLexico {
        if (tokenAtual.getTipo() == tipoEsperado) {
            Token tokenConsumido = tokenAtual;
            Debug.sintatico("Consumindo: " + tipoEsperado.name() + " = '" + tokenConsumido.getValor() + "'");
            tokenAtual = analisadorLexico.proximoToken();
            return tokenConsumido;
        }
        erro("Esperado " + tipoEsperado.name());
        return null;
    }
    private boolean verificar(TipoToken tipo) {
        return tokenAtual.getTipo() == tipo;
    }
    private boolean verificarVarios(TipoToken... tipos) {
        TipoToken atual = tokenAtual.getTipo();
        for (TipoToken tipo : tipos) {
            if (atual == tipo) {
                return true;
            }
        }
        return false;
    }

    public Programa analisar() throws ErroSintatico, ErroLexico {
        return analisarPrograma();
    }

    private Programa analisarPrograma() throws ErroSintatico, ErroLexico {
        Debug.entrarRegra("<programa>");
        consumir(TipoToken.PHP_ABRE);
        Corpo corpo = analisarCorpo();
        consumir(TipoToken.PHP_FECHA);

        if (!verificar(TipoToken.EOF)) {
            erro("Codigo inesperado apos '?>'");
        }

        Debug.sairRegra("<programa>");
        return new Programa(corpo);
    }

    private Corpo analisarCorpo() throws ErroSintatico, ErroLexico {
        Debug.entrarRegra("<corpo>");
        Corpo corpo = analisarCorpoComDeclaracoes(true);
        Debug.sairRegra("<corpo>");
        return corpo;
    }

    private Corpo analisarCorpoComDeclaracoes(boolean permiteFuncoes) throws ErroSintatico, ErroLexico {
        List<Declaracao> declaracoes = new ArrayList<>();
        List<Comando> comandos = new ArrayList<>();
        Set<String> variaveisDeclaradas = new HashSet<>();

        boolean emDeclaracoes = true;
        while (emDeclaracoes) {
            if (permiteFuncoes && verificar(TipoToken.FUNCTION)) {
                declaracoes.add(analisarDcF());
                continue;
            }
            if (verificar(TipoToken.VARIAVEL)) {
                String nome = (String) tokenAtual.getValor();
                if (variaveisDeclaradas.contains(nome)) {
                    emDeclaracoes = false;
                    continue;
                }
                declaracoes.add(analisarDcV());
                variaveisDeclaradas.add(nome);
                continue;
            }
            emDeclaracoes = false;
        }

        comandos.addAll(analisarComandos());
        return new Corpo(declaracoes, comandos);
    }

    private DeclaracaoVariavel analisarDcV() throws ErroSintatico, ErroLexico {
        Debug.entrarRegra("<dc_v>");
        Token tokenVariavel = consumir(TipoToken.VARIAVEL);
        String nome = (String) tokenVariavel.getValor();
        Debug.sintatico("Declarando variavel: " + nome);
        Expressao expressaoInicial = analisarAtribuicaoOpcional();
        Debug.sairRegra("<dc_v>");
        return new DeclaracaoVariavel(nome, expressaoInicial);
    }

    private Expressao analisarAtribuicaoOpcional() throws ErroSintatico, ErroLexico {
        if (verificar(TipoToken.ATRIBUICAO)) {
            consumir(TipoToken.ATRIBUICAO);
            Expressao expressao = analisarExpressao();
            consumir(TipoToken.PONTO_VIRGULA);
            return expressao;
        }
        consumir(TipoToken.PONTO_VIRGULA);
        return null;
    }

    private DeclaracaoFuncao analisarDcF() throws ErroSintatico, ErroLexico {
        Debug.entrarRegra("<dc_f>");
        consumir(TipoToken.FUNCTION);
        Token tokenNomeFuncao = consumir(TipoToken.IDENT);
        String nome = (String) tokenNomeFuncao.getValor();
        Debug.sintatico("Declarando funcao: " + nome);
        List<String> parametros = analisarParametros();
        Debug.sintatico("Parametros: " + parametros);
        consumir(TipoToken.ABRE_CHAVE);
        Corpo corpo = analisarCorpoF();
        consumir(TipoToken.FECHA_CHAVE);
        Debug.sairRegra("<dc_f>");
        return new DeclaracaoFuncao(nome, parametros, corpo);
    }

    private List<String> analisarParametros() throws ErroSintatico, ErroLexico {
        consumir(TipoToken.ABRE_PAREN);
        List<String> parametros = new ArrayList<>();

        if (verificar(TipoToken.VARIAVEL)) {
            parametros = analisarListaPar();
        }

        consumir(TipoToken.FECHA_PAREN);
        return parametros;
    }

    private List<String> analisarListaPar() throws ErroSintatico, ErroLexico {
        List<String> parametros = new ArrayList<>();
        Token tokenParametro = consumir(TipoToken.VARIAVEL);
        parametros.add((String) tokenParametro.getValor());

        while (verificar(TipoToken.VIRGULA)) {
            consumir(TipoToken.VIRGULA);
            tokenParametro = consumir(TipoToken.VARIAVEL);
            parametros.add((String) tokenParametro.getValor());
        }

        return parametros;
    }

    private Corpo analisarCorpoF() throws ErroSintatico, ErroLexico {
        return analisarCorpoComDeclaracoes(false);
    }

    private List<Comando> analisarComandos() throws ErroSintatico, ErroLexico {
        List<Comando> comandos = new ArrayList<>();
        while (verificarVarios(TIPOS_COMANDO)) {
            comandos.add(analisarComando());
        }

        return comandos;
    }

    private Comando analisarComando() throws ErroSintatico, ErroLexico {
        Debug.entrarRegra("<comando>");
        Comando cmd = null;
        if (verificar(TipoToken.ECHO)) {
            cmd = analisarComandoEcho();
        } else if (verificar(TipoToken.IF)) {
            cmd = analisarComandoIf();
        } else if (verificar(TipoToken.WHILE)) {
            cmd = analisarComandoWhile();
        } else if (verificar(TipoToken.VARIAVEL)) {
            cmd = analisarComandoVariavel();
        } else if (verificar(TipoToken.IDENT)) {
            cmd = analisarComandoIdent();
        } else {
            erro("Esperado comando (echo, if, while ou $var)");
        }
        Debug.sairRegra("<comando>");
        return cmd;
    }

    private ComandoEcho analisarComandoEcho() throws ErroSintatico, ErroLexico {
        consumir(TipoToken.ECHO);
        Expressao expressao = analisarExpressao();
        consumir(TipoToken.PONTO);
        consumir(TipoToken.PHP_EOL);
        consumir(TipoToken.PONTO_VIRGULA);
        return new ComandoEcho(expressao);
    }

    private ComandoIf analisarComandoIf() throws ErroSintatico, ErroLexico {
        consumir(TipoToken.IF);
        consumir(TipoToken.ABRE_PAREN);
        Condicao condicao = analisarCondicao();
        consumir(TipoToken.FECHA_PAREN);
        consumir(TipoToken.ABRE_CHAVE);
        List<Comando> blocoIf = analisarComandos();
        consumir(TipoToken.FECHA_CHAVE);
        List<Comando> blocoElse = analisarPfalsa();
        return new ComandoIf(condicao, blocoIf, blocoElse);
    }

    private List<Comando> analisarPfalsa() throws ErroSintatico, ErroLexico {
        if (verificar(TipoToken.ELSE)) {
            consumir(TipoToken.ELSE);
            consumir(TipoToken.ABRE_CHAVE);
            List<Comando> comandos = analisarComandos();
            consumir(TipoToken.FECHA_CHAVE);
            return comandos;
        }
        return null;
    }

    private ComandoWhile analisarComandoWhile() throws ErroSintatico, ErroLexico {
        consumir(TipoToken.WHILE);
        consumir(TipoToken.ABRE_PAREN);
        Condicao condicao = analisarCondicao();
        consumir(TipoToken.FECHA_PAREN);
        consumir(TipoToken.ABRE_CHAVE);
        List<Comando> bloco = analisarComandos();
        consumir(TipoToken.FECHA_CHAVE);
        return new ComandoWhile(condicao, bloco);
    }

    private Comando analisarComandoVariavel() throws ErroSintatico, ErroLexico {
        Token tokenVariavel = consumir(TipoToken.VARIAVEL);
        String nome = (String) tokenVariavel.getValor();

        if (verificar(TipoToken.ATRIBUICAO)) {
            consumir(TipoToken.ATRIBUICAO);
            Expressao expressao = analisarExpressao();
            consumir(TipoToken.PONTO_VIRGULA);
            return new ComandoAtribuicao(nome, expressao);
        }
        if (verificar(TipoToken.ABRE_PAREN)) {
            List<Expressao> argumentos = analisarListaArg();
            consumir(TipoToken.PONTO_VIRGULA);
            String nomeFuncao = nome.startsWith("$") ? nome.substring(1) : nome;
            return new ComandoChamadaFuncao(nomeFuncao, argumentos);
        }
        erro("Esperado '=' ou '(' apos variavel");
        return null;
    }

    private ComandoChamadaFuncao analisarComandoIdent() throws ErroSintatico, ErroLexico {
        Token tokenIdentificador = consumir(TipoToken.IDENT);
        String nomeFuncao = (String) tokenIdentificador.getValor();
        List<Expressao> argumentos = analisarListaArg();
        consumir(TipoToken.PONTO_VIRGULA);
        return new ComandoChamadaFuncao(nomeFuncao, argumentos);
    }

    private List<Expressao> analisarListaArg() throws ErroSintatico, ErroLexico {
        consumir(TipoToken.ABRE_PAREN);
        List<Expressao> argumentos = new ArrayList<>();

        if (!verificar(TipoToken.FECHA_PAREN)) {
            argumentos = analisarArgumentos();
        }

        consumir(TipoToken.FECHA_PAREN);
        return argumentos;
    }

    private List<Expressao> analisarArgumentos() throws ErroSintatico, ErroLexico {
        List<Expressao> argumentos = new ArrayList<>();
        argumentos.add(analisarExpressao());

        while (verificar(TipoToken.VIRGULA)) {
            consumir(TipoToken.VIRGULA);
            argumentos.add(analisarExpressao());
        }

        return argumentos;
    }

    private Condicao analisarCondicao() throws ErroSintatico, ErroLexico {
        Expressao expressao = analisarDisjuncao();
        return new Condicao(expressao);
    }

    private Expressao analisarDisjuncao() throws ErroSintatico, ErroLexico {
        Expressao esquerda = analisarConjuncao();
        while (verificar(TipoToken.OR)) {
            consumir(TipoToken.OR);
            Expressao direita = analisarConjuncao();
            esquerda = new ExpressaoBinaria(esquerda, "||", direita);
        }
        return esquerda;
    }

    private Expressao analisarConjuncao() throws ErroSintatico, ErroLexico {
        Expressao esquerda = analisarNegacao();
        while (verificar(TipoToken.AND)) {
            consumir(TipoToken.AND);
            Expressao direita = analisarNegacao();
            esquerda = new ExpressaoBinaria(esquerda, "&&", direita);
        }
        return esquerda;
    }

    private Expressao analisarNegacao() throws ErroSintatico, ErroLexico {
        if (verificar(TipoToken.NOT)) {
            consumir(TipoToken.NOT);
            Expressao operando = analisarNegacao();
            return new ExpressaoUnaria("!", operando);
        }
        if (verificar(TipoToken.ABRE_PAREN)) {
            consumir(TipoToken.ABRE_PAREN);
            Expressao expressao = analisarDisjuncao();
            if (!verificar(TipoToken.FECHA_PAREN)) {
                erro("Esperado FECHA_PAREN");
            }
            consumir(TipoToken.FECHA_PAREN);

            // Permite continuar uma expressao aritmetica apos parenteses
            Expressao aritmetica = analisarMaisFatores(expressao);
            aritmetica = analisarOutrosTermos(aritmetica);
            if (verificarRelacional()) {
                return analisarRelacaoComEsquerda(aritmetica);
            }
            return aritmetica;
        }
        return analisarRelacao();
    }

    private Expressao analisarRelacao() throws ErroSintatico, ErroLexico {
        Expressao esquerda = analisarExpressao();
        return analisarRelacaoComEsquerda(esquerda);
    }
    private boolean verificarRelacional() {
        return verificarVarios(
            TipoToken.IGUAL,
            TipoToken.DIFERENTE,
            TipoToken.MAIOR_IGUAL,
            TipoToken.MENOR_IGUAL,
            TipoToken.MAIOR,
            TipoToken.MENOR
        );
    }

    private Expressao analisarRelacaoComEsquerda(Expressao esquerda) throws ErroSintatico, ErroLexico {
        if (verificar(TipoToken.IGUAL)) {
            consumir(TipoToken.IGUAL);
            Expressao direita = analisarExpressao();
            return new ExpressaoBinaria(esquerda, "==", direita);
        }
        if (verificar(TipoToken.DIFERENTE)) {
            consumir(TipoToken.DIFERENTE);
            Expressao direita = analisarExpressao();
            return new ExpressaoBinaria(esquerda, "!=", direita);
        }
        if (verificar(TipoToken.MAIOR_IGUAL)) {
            consumir(TipoToken.MAIOR_IGUAL);
            Expressao direita = analisarExpressao();
            return new ExpressaoBinaria(esquerda, ">=", direita);
        }
        if (verificar(TipoToken.MENOR_IGUAL)) {
            consumir(TipoToken.MENOR_IGUAL);
            Expressao direita = analisarExpressao();
            return new ExpressaoBinaria(esquerda, "<=", direita);
        }
        if (verificar(TipoToken.MAIOR)) {
            consumir(TipoToken.MAIOR);
            Expressao direita = analisarExpressao();
            return new ExpressaoBinaria(esquerda, ">", direita);
        }
        if (verificar(TipoToken.MENOR)) {
            consumir(TipoToken.MENOR);
            Expressao direita = analisarExpressao();
            return new ExpressaoBinaria(esquerda, "<", direita);
        }
        return esquerda;
    }

    private Expressao analisarExpressao() throws ErroSintatico, ErroLexico {
        Debug.entrarRegra("<expressao>");
        Expressao expressao;
        if (verificar(TipoToken.FLOATVAL)) {
            Debug.sintatico("floatval(readline())");
            consumir(TipoToken.FLOATVAL);
            consumir(TipoToken.ABRE_PAREN);
            consumir(TipoToken.READLINE);
            consumir(TipoToken.ABRE_PAREN);
            consumir(TipoToken.FECHA_PAREN);
            consumir(TipoToken.FECHA_PAREN);
            expressao = new ExpressaoLeitura();
        } else {
            Expressao termo = analisarTermo();
            expressao = analisarOutrosTermos(termo);
        }
        Debug.sairRegra("<expressao>");
        return expressao;
    }

    private Expressao analisarOutrosTermos(Expressao esquerda) throws ErroSintatico, ErroLexico {
        while (verificarVarios(TipoToken.MAIS, TipoToken.MENOS)) {
            String operador;
            if (verificar(TipoToken.MAIS)) {
                consumir(TipoToken.MAIS);
                operador = "+";
            } else {
                consumir(TipoToken.MENOS);
                operador = "-";
            }

            Expressao direita = analisarTermo();
            esquerda = new ExpressaoBinaria(esquerda, operador, direita);
        }

        return esquerda;
    }

    private Expressao analisarTermo() throws ErroSintatico, ErroLexico {
        boolean negativo = false;
        if (verificar(TipoToken.MENOS)) {
            consumir(TipoToken.MENOS);
            negativo = true;
        }

        Expressao fator = analisarFator();

        if (negativo) {
            fator = new ExpressaoUnaria("-", fator);
        }

        return analisarMaisFatores(fator);
    }

    private Expressao analisarMaisFatores(Expressao esquerda) throws ErroSintatico, ErroLexico {
        while (verificarVarios(TipoToken.MULT, TipoToken.DIV)) {
            String operador;
            if (verificar(TipoToken.MULT)) {
                consumir(TipoToken.MULT);
                operador = "*";
            } else {
                consumir(TipoToken.DIV);
                operador = "/";
            }

            Expressao direita = analisarFator();
            esquerda = new ExpressaoBinaria(esquerda, operador, direita);
        }

        return esquerda;
    }

    private Expressao analisarFator() throws ErroSintatico, ErroLexico {
        if (verificar(TipoToken.VARIAVEL)) {
            Token token = consumir(TipoToken.VARIAVEL);
            return new ExpressaoVariavel((String) token.getValor());
        }
        if (verificar(TipoToken.NUMERO)) {
            Token token = consumir(TipoToken.NUMERO);
            return new ExpressaoNumero((Double) token.getValor());
        }
        if (verificar(TipoToken.IDENT)) {
            Token token = consumir(TipoToken.IDENT);
            String nomeFuncao = (String) token.getValor();
            List<Expressao> argumentos = analisarListaArg();
            return new ExpressaoChamadaFuncao(nomeFuncao, argumentos);
        }
        if (verificar(TipoToken.ABRE_PAREN)) {
            consumir(TipoToken.ABRE_PAREN);
            Expressao expressao = analisarExpressao();
            consumir(TipoToken.FECHA_PAREN);
            return expressao;
        }
        erro("Esperado variavel, numero, chamada de funcao ou '('");
        return null;
    }
}
