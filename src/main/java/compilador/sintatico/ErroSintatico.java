package compilador.sintatico;

import compilador.lexico.Token;

public class ErroSintatico extends Exception {

    public ErroSintatico(String mensagem, Token token) {
        super("Erro Sintatico na linha " + token.getLinha() + ", coluna " + token.getColuna() + ": "
            + mensagem + " (encontrado: " + token.getTipo().name() + " '" + String.valueOf(token.getValor()) + "')");
    }

}
