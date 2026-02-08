package compilador.arvore_sintatica_abstrata;

import java.util.List;

public class ComandoIf extends Comando {
    
    public final Condicao condicao;
    public final List<Comando> blocoIf;
    public final List<Comando> blocoElse;

    public ComandoIf(Condicao condicao, List<Comando> blocoIf, List<Comando> blocoElse) {
        this.condicao = condicao;
        this.blocoIf = blocoIf;
        this.blocoElse = blocoElse;
    }
}
