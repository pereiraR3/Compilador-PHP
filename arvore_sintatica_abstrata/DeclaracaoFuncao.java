package compilador.arvore_sintatica_abstrata;

import java.util.List;

public class DeclaracaoFuncao extends Declaracao {
    
    public final String nome;
    public final List<String> parametros;
    public final Corpo corpo;
    public int enderecoInicio = -1;

    public DeclaracaoFuncao(String nome, List<String> parametros, Corpo corpo) {
        this.nome = nome;
        this.parametros = parametros;
        this.corpo = corpo;
    }
}
