package compilador.lexico;

public class ErroLexico extends Exception {
    
    public ErroLexico(String mensagem, int linha, int coluna) {
        super("Erro Lexico na linha " + linha + ", coluna " + coluna + ": " + mensagem);
    }

}
