package compilador.arvore_sintatica_abstrata;

public class DeclaracaoVariavel extends Declaracao {
    
    public final String nome;
    public final Expressao expressaoInicial;
    public int endereco = -1;

    public DeclaracaoVariavel(String nome, Expressao expressaoInicial) {
        this.nome = nome;
        this.expressaoInicial = expressaoInicial;
    }
}
