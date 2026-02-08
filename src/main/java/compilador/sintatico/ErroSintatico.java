package compilador.sintatico;

import compilador.lexico.Token;

public class ErroSintatico extends Exception {
    private final String mensagem;
    private final Token token;

    /**
     * Construtor
     */
    public ErroSintatico(String mensagem, Token token) {
        super("Erro Sintatico na linha " + token.getLinha() + ", coluna " + token.getColuna() + ": "
            + mensagem + " (encontrado: " + token.getTipo().name() + " '" + String.valueOf(token.getValor()) + "')");
        this.mensagem = mensagem;
        this.token = token;
    }

    /**
     * Executa a rotina getMensagem
     */
    public String getMensagem() {
        return mensagem;
    }

    /**
     * Executa a rotina getToken
     */
    public Token getToken() {
        return token;
    }
}
