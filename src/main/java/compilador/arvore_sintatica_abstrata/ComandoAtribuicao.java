package compilador.arvore_sintatica_abstrata;

public class ComandoAtribuicao extends Comando {
    
    public final String variavel;
    public final Expressao expressao;

    public ComandoAtribuicao(String variavel, Expressao expressao) {
        this.variavel = variavel;
        this.expressao = expressao;
    }
}
