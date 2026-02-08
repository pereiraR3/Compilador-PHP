package compilador.semantico;

/**
 * Excecao de analise semantica com mensagem detalhada
 */
public class ErroSemantico extends Exception {

    private final String mensagem;

    /**
     * Cria um erro semantico com a mensagem informada
     */
    public ErroSemantico(String mensagem) {
        super("Erro Semantico: " + mensagem);
        this.mensagem = mensagem;
    }

    /**
     * Retorna a mensagem do erro semantico
     */
    public String getMensagem() {
        return mensagem;
    }
}

/**
 * Excecao especifica para falhas na tabela de simbolos
 */
class ErroTabelaSimbolos extends Exception {

    /**
     * Cria um erro de tabela de simbolos com a mensagem informada
     */
    public ErroTabelaSimbolos(String mensagem) {
        super(mensagem);
    }
}
