package compilador.arvore_sintatica_abstrata;

import java.util.List;

public class Corpo extends NoArvoreSintaticaAbstrata {
    
    public final List<Declaracao> declaracoes;
    public final List<Comando> comandos;

    public Corpo(List<Declaracao> declaracoes, List<Comando> comandos) {
        this.declaracoes = declaracoes;
        this.comandos = comandos;
    }
}
