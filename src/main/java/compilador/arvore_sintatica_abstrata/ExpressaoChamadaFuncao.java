package compilador.arvore_sintatica_abstrata;

import java.util.List;

public class ExpressaoChamadaFuncao extends Expressao {
    
    public final String nomeFuncao;
    public final List<Expressao> argumentos;

    public ExpressaoChamadaFuncao(String nomeFuncao, List<Expressao> argumentos) {
        this.nomeFuncao = nomeFuncao;
        this.argumentos = argumentos;
    }
}
