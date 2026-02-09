package compilador.semantico;

import java.util.HashMap;
import java.util.Map;

public class TabelaSimbolos {
    
    private final Map<String, Simbolo> simbolos;
    private final TabelaSimbolos pai;
    public final String nomeEscopo;
    public int proximoEndereco;

    public TabelaSimbolos(TabelaSimbolos pai, String nomeEscopo) {
        this.simbolos = new HashMap<>();
        this.pai = pai;
        this.nomeEscopo = nomeEscopo;
        this.proximoEndereco = 0;
    }
    public void adicionar(Simbolo simbolo) throws ErroTabelaSimbolos {
        if (simbolos.containsKey(simbolo.nome)) {
            throw new ErroTabelaSimbolos("Simbolo '" + simbolo.nome + "' ja declarado neste escopo");
        }
        simbolos.put(simbolo.nome, simbolo);
    }

    public Simbolo buscar(String nome) {
        Simbolo simbolo = simbolos.get(nome);
        if (simbolo != null) {
            return simbolo;
        }
        if (pai != null) {
            return pai.buscar(nome);
        }
        return null;
    }

    public Simbolo buscarLocal(String nome) {
        return simbolos.get(nome);
    }

    public int alocarEndereco() {
        int endereco = proximoEndereco;
        proximoEndereco += 1;
        return endereco;
    }
    public Map<String, Simbolo> getTodosSimbolos() {
        return new HashMap<>(simbolos);
    }
}
