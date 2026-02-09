package compilador.semantico;

public class ErroSemantico extends Exception {

    private final String mensagem;

    public ErroSemantico(String mensagem) {
        super("Erro Semantico: " + mensagem);
        this.mensagem = mensagem;
    }

    public String getMensagem() {
        return mensagem;
    }
}

class ErroTabelaSimbolos extends Exception {

    public ErroTabelaSimbolos(String mensagem) {
        super(mensagem);
    }
}
