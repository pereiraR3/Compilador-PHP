package compilador.semantico;

import java.util.ArrayList;
import java.util.List;

public class Simbolo {

    public final String nome;
    public final TipoSimbolo tipo;
    public int endereco;
    public List<String> parametros;
    public int numVariaveisLocais;
    public int enderecoInicio;
    public String escopo;

    /**
     * Cria um simbolo para uma das tres possibilidades:
     * 1. Variavel: informe endereco e escopo, parametros = null
     * 2. Parametro: informe endereco e escopo (nome da funcao), parametros = null
     * 3. Funcao: informe parametros e escopo, endereco = -1
     */
    public Simbolo(String nome, TipoSimbolo tipo, int endereco, List<String> parametros, String escopo) {
        this.nome = nome;
        this.tipo = tipo;
        this.endereco = endereco;
        this.parametros = parametros == null ? new ArrayList<>() : new ArrayList<>(parametros);
        this.numVariaveisLocais = 0;
        this.enderecoInicio = -1;
        this.escopo = escopo;
    }
}
