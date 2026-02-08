package compilador.arvore_sintatica_abstrata;

public class ExpressaoUnaria extends Expressao {
    
    public final String operador;
    public final Expressao operando;

    public ExpressaoUnaria(String operador, Expressao operando) {
        this.operador = operador;
        this.operando = operando;
    }
}
