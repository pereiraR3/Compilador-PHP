package compilador.lexico;

/**
 * Excecao para erros durante a analise lexica
 */
public class ErroLexico extends Exception {
    
    private final String mensagem;
    private final int linha;
    private final int coluna;

    /**
     * Cria um erro lexico com mensagem e posicao (linha/coluna)
     */
    public ErroLexico(String mensagem, int linha, int coluna) {
        super("Erro Lexico na linha " + linha + ", coluna " + coluna + ": " + mensagem);
        this.mensagem = mensagem;
        this.linha = linha;
        this.coluna = coluna;
    }

    /**
     * Retorna a mensagem base do erro
     */
    public String getMensagem() {
        return mensagem;
    }

    /**
     * Retorna a linha onde o erro ocorreu
     */
    public int getLinha() {
        return linha;
    }

    /**
     * Retorna a coluna onde o erro ocorreu
     */
    public int getColuna() {
        return coluna;
    }
}
