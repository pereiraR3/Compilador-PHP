package compilador.arvore_sintatica_abstrata;

import java.util.List;

public class ComandoWhile extends Comando {
    
    public final Condicao condicao;
    public final List<Comando> bloco;

    public ComandoWhile(Condicao condicao, List<Comando> bloco) {
        this.condicao = condicao;
        this.bloco = bloco;
    }
}
