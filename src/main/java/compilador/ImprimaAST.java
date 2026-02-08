package compilador;

import compilador.arvore_sintatica_abstrata.*;

import java.util.List;

/**
 * Imprime a arvore sintatica abstrata em formato textual
 */
public class ImprimaAST {
    private final String indentacaoBase;

    /**
     * Cria o impressor com indentacao vazia
     */
    public ImprimaAST() {
        this("");
    }

    /**
     * Cria o impressor com uma indentacao base
     */
    public ImprimaAST(String indentacaoBase) {
        this.indentacaoBase = indentacaoBase == null ? "" : indentacaoBase;
    }

    /**
     * Gera a representacao textual da arvore a partir do programa
     */
    public String imprimir(Programa programa) {
        StringBuilder sb = new StringBuilder();
        imprimirNo(programa, sb, 0);
        return sb.toString();
    }

    /**
     * Imprime recursivamente um no da arvore
     */
    private void imprimirNo(NoArvoreSintaticaAbstrata no, StringBuilder sb, int nivel) {
        if (no == null) {
            linha(sb, nivel, "null");
            return;
        }

        if (no instanceof Programa) {
            Programa programa = (Programa) no;
            linha(sb, nivel, "Programa");
            imprimirNo(programa.corpo, sb, nivel + 1);
            return;
        }

        if (no instanceof Corpo) {
            Corpo corpo = (Corpo) no;
            linha(sb, nivel, "Corpo");
            imprimirLista("Declaracoes", corpo.declaracoes, sb, nivel + 1);
            imprimirLista("Comandos", corpo.comandos, sb, nivel + 1);
            return;
        }

        if (no instanceof DeclaracaoVariavel) {
            DeclaracaoVariavel declaracaoVariavel = (DeclaracaoVariavel) no;
            linha(sb, nivel, "DeclaracaoVariavel nome=" + declaracaoVariavel.nome);
            if (declaracaoVariavel.expressaoInicial != null) {
                linha(sb, nivel + 1, "Inicializacao");
                imprimirNo(declaracaoVariavel.expressaoInicial, sb, nivel + 2);
            }
            return;
        }

        if (no instanceof DeclaracaoFuncao) {
            DeclaracaoFuncao declaracaoFuncao = (DeclaracaoFuncao) no;
            linha(sb, nivel, "DeclaracaoFuncao nome=" + declaracaoFuncao.nome);
            imprimirListaString("Parametros", declaracaoFuncao.parametros, sb, nivel + 1);
            imprimirNo(declaracaoFuncao.corpo, sb, nivel + 1);
            return;
        }

        if (no instanceof ComandoEcho) {
            ComandoEcho comandoEcho = (ComandoEcho) no;
            linha(sb, nivel, "ComandoEcho");
            imprimirNo(comandoEcho.expressao, sb, nivel + 1);
            return;
        }

        if (no instanceof ComandoAtribuicao) {
            ComandoAtribuicao comandoAtribuicao = (ComandoAtribuicao) no;
            linha(sb, nivel, "ComandoAtribuicao variavel=" + comandoAtribuicao.variavel);
            imprimirNo(comandoAtribuicao.expressao, sb, nivel + 1);
            return;
        }

        if (no instanceof ComandoChamadaFuncao) {
            ComandoChamadaFuncao comandoChamadaFuncao = (ComandoChamadaFuncao) no;
            linha(sb, nivel, "ComandoChamadaFuncao nome=" + comandoChamadaFuncao.nomeFuncao);
            imprimirLista("Argumentos", comandoChamadaFuncao.argumentos, sb, nivel + 1);
            return;
        }

        if (no instanceof ComandoIf) {
            ComandoIf comandoIf = (ComandoIf) no;
            linha(sb, nivel, "ComandoIf");
            linha(sb, nivel + 1, "Condicao");
            imprimirNo(comandoIf.condicao, sb, nivel + 2);
            imprimirLista("BlocoIf", comandoIf.blocoIf, sb, nivel + 1);
            imprimirLista("BlocoElse", comandoIf.blocoElse, sb, nivel + 1);
            return;
        }

        if (no instanceof ComandoWhile) {
            ComandoWhile comandoWhile = (ComandoWhile) no;
            linha(sb, nivel, "ComandoWhile");
            linha(sb, nivel + 1, "Condicao");
            imprimirNo(comandoWhile.condicao, sb, nivel + 2);
            imprimirLista("Bloco", comandoWhile.bloco, sb, nivel + 1);
            return;
        }

        if (no instanceof Condicao) {
            Condicao condicao = (Condicao) no;
            linha(sb, nivel, "Condicao");
            imprimirNo(condicao.expressao, sb, nivel + 1);
            return;
        }

        if (no instanceof ExpressaoBinaria) {
            ExpressaoBinaria expressaoBinaria = (ExpressaoBinaria) no;
            linha(sb, nivel, "ExpressaoBinaria op=" + expressaoBinaria.operador);
            imprimirNo(expressaoBinaria.esquerda, sb, nivel + 1);
            imprimirNo(expressaoBinaria.direita, sb, nivel + 1);
            return;
        }

        if (no instanceof ExpressaoUnaria) {
            ExpressaoUnaria expressaoUnaria = (ExpressaoUnaria) no;
            linha(sb, nivel, "ExpressaoUnaria op=" + expressaoUnaria.operador);
            imprimirNo(expressaoUnaria.operando, sb, nivel + 1);
            return;
        }

        if (no instanceof ExpressaoVariavel) {
            ExpressaoVariavel expressaoVariavel = (ExpressaoVariavel) no;
            linha(sb, nivel, "ExpressaoVariavel nome=" + expressaoVariavel.nome);
            return;
        }

        if (no instanceof ExpressaoNumero) {
            ExpressaoNumero expressaoNumero = (ExpressaoNumero) no;
            linha(sb, nivel, "ExpressaoNumero valor=" + expressaoNumero.valor);
            return;
        }

        if (no instanceof ExpressaoLeitura) {
            linha(sb, nivel, "ExpressaoLeitura");
            return;
        }

        if (no instanceof ExpressaoChamadaFuncao) {
            ExpressaoChamadaFuncao expressaoChamadaFuncao = (ExpressaoChamadaFuncao) no;
            linha(sb, nivel, "ExpressaoChamadaFuncao nome=" + expressaoChamadaFuncao.nomeFuncao);
            imprimirLista("Argumentos", expressaoChamadaFuncao.argumentos, sb, nivel + 1);
            return;
        }

        linha(sb, nivel, "NoDesconhecido " + no.getClass().getSimpleName());
    }

    /**
     * Imprime uma lista de nos com um rotulo
     */
    private void imprimirLista(String rotulo, List<? extends NoArvoreSintaticaAbstrata> lista, StringBuilder sb, int nivel) {
        if (lista == null || lista.isEmpty()) {
            linha(sb, nivel, rotulo + " (vazio)");
            return;
        }
        linha(sb, nivel, rotulo);
        for (NoArvoreSintaticaAbstrata item : lista) {
            imprimirNo(item, sb, nivel + 1);
        }
    }

    /**
     * Imprime uma lista de strings com um rotulo
     */
    private void imprimirListaString(String rotulo, List<String> lista, StringBuilder sb, int nivel) {
        if (lista == null || lista.isEmpty()) {
            linha(sb, nivel, rotulo + " (vazio)");
            return;
        }
        linha(sb, nivel, rotulo);
        for (String item : lista) {
            linha(sb, nivel + 1, item);
        }
    }

    /**
     * Adiciona uma linha com indentacao ao texto final
     */
    private void linha(StringBuilder sb, int nivel, String texto) {
        sb.append(indentacaoBase);
        for (int i = 0; i < nivel; i++) {
            sb.append("  ");
        }
        sb.append(texto).append('\n');
    }
}
