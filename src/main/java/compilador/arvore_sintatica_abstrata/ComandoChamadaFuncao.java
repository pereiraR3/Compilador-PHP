package compilador.arvore_sintatica_abstrata;

import java.util.List;

public class ComandoChamadaFuncao extends Comando {
    
    public final String nomeFuncao;
    public final List<Expressao> argumentos;

    public ComandoChamadaFuncao(String nomeFuncao, List<Expressao> argumentos) {
        this.nomeFuncao = nomeFuncao;
        this.argumentos = argumentos;
    }
}
