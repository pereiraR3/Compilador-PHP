package compilador.arvore_sintatica_abstrata;

public class ComandoEcho extends Comando {
    
    public final Expressao expressao;

    public ComandoEcho(Expressao expressao) {
        this.expressao = expressao;
    }
}
