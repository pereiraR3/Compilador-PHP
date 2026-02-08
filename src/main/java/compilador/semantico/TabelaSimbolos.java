package compilador.semantico;

import java.util.HashMap;
import java.util.Map;

/*
 * Estrutura de dados para armazenar simbolos por escopo
 */
public class TabelaSimbolos {
    
    private final Map<String, Simbolo> simbolos;
    private final TabelaSimbolos pai;
    public final String nomeEscopo;
    public int proximoEndereco;

    /**
     * Cria uma tabela de simbolos para um escopo
     *
     * Existem 2 possibilidades de uso:
     * 1) Escopo global: informe pai como null e use um nome como "global" para representar o escopo raiz
     * 2) Escopo aninhado: informe um pai existente e um nomeEscopo para representar o novo nivel (ex.: "funcao", "bloco")
     */
    public TabelaSimbolos(TabelaSimbolos pai, String nomeEscopo) {
        this.simbolos = new HashMap<>();
        this.pai = pai;
        this.nomeEscopo = nomeEscopo;
        this.proximoEndereco = 0;
    }

    /**
     * Adiciona um simbolo no escopo atual
     */
    public void adicionar(Simbolo simbolo) throws ErroTabelaSimbolos {
        if (simbolos.containsKey(simbolo.nome)) {
            throw new ErroTabelaSimbolos("Simbolo '" + simbolo.nome + "' ja declarado neste escopo");
        }
        simbolos.put(simbolo.nome, simbolo);
    }

    /**
     * Busca um simbolo no escopo atual e, se necessario, nos escopos pais
     */
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

    /**
     * Busca um simbolo apenas no escopo atual
     */
    public Simbolo buscarLocal(String nome) {
        return simbolos.get(nome);
    }

    /**
     * Reserva e retorna o proximo endereco disponivel no escopo
     */
    public int alocarEndereco() {
        int endereco = proximoEndereco;
        proximoEndereco += 1;
        return endereco;
    }

    /**
     * Retorna uma copia dos simbolos do escopo atual
     */
    public Map<String, Simbolo> getTodosSimbolos() {
        return new HashMap<>(simbolos);
    }
}
