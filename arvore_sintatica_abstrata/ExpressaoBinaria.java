package compilador.arvore_sintatica_abstrata;

public class ExpressaoBinaria extends Expressao {
    
    public final Expressao esquerda;
    public final String operador;
    public final Expressao direita;

    public ExpressaoBinaria(Expressao esquerda, String operador, Expressao direita) {
        this.esquerda = esquerda;
        this.operador = operador;
        this.direita = direita;
    }
}
