package compilador.lexico;

/**
 * Representa um token reconhecido pelo analisador lexico
 */
public class Token {

    private final TipoToken tipo;
    private final Object valor;
    private final int linha;
    private final int coluna;

    /**
     * Cria um token com tipo, valor e posicao no codigo fonte
     */
    public Token(TipoToken tipo, Object valor, int linha, int coluna) {
        this.tipo = tipo;
        this.valor = valor;
        this.linha = linha;
        this.coluna = coluna;
    }

    /**
     * Retorna o tipo do token
     */
    public TipoToken getTipo() {
        return tipo;
    }

    /**
     * Retorna o valor do token
     */
    public Object getValor() {
        return valor;
    }

    /**
     * Retorna a linha do token no codigo fonte
     */
    public int getLinha() {
        return linha;
    }

    /**
     * Retorna a coluna do token no codigo fonte
     */
    public int getColuna() {
        return coluna;
    }

    /**
     * Retorna uma representacao textual do token
     */
    @Override
    public String toString() {
        return "Token(" + tipo.name() + ", " + String.valueOf(valor) + ", " + linha + ":" + coluna + ")";
    }
}
