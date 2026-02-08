package compilador.arvore_sintatica_abstrata;

public class ExpressaoNumero extends Expressao {
    
    public final double valor;

    public ExpressaoNumero(double valor) {
        this.valor = valor;
    }
}
