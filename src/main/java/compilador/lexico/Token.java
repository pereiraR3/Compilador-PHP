package compilador.lexico;

public class Token {

    private final TipoToken tipo;
    private final Object valor;
    private final int linha;
    private final int coluna;

    public Token(TipoToken tipo, Object valor, int linha, int coluna) {
        this.tipo = tipo;
        this.valor = valor;
        this.linha = linha;
        this.coluna = coluna;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public Object getValor() {
        return valor;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    @Override
    public String toString() {
        return "Token(" + tipo.name() + ", " + String.valueOf(valor) + ", " + linha + ":" + coluna + ")";
    }
}
