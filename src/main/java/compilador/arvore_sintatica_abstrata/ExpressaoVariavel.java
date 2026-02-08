package compilador.arvore_sintatica_abstrata;

public class ExpressaoVariavel extends Expressao {
    
    public final String nome;

    public ExpressaoVariavel(String nome) {
        this.nome = nome;
    }
}
