package compilador.arvore_sintatica_abstrata;

public class Condicao extends NoArvoreSintaticaAbstrata {
    
    public final Expressao expressao;

    public Condicao(Expressao expressao) {
        this.expressao = expressao;
    }
}
