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
