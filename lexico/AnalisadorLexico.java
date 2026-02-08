package compilador.lexico;

import compilador.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AnalisadorLexico lexico que converte codigo-fonte em uma sequencia de tokens
 */
public class AnalisadorLexico {

    private static final String[][] PADROES = {
        {"/\\*[\\s\\S]*?\\*/", "COMENTARIO"},
        {"//[^\\n]*", "COMENTARIOLINHA"},
        {"<\\?php", "PHPABRE"},
        {"\\?>", "PHPFECHA"},
        {"==", "IGUAL"},
        {"!=", "DIFERENTE"},
        {"&&", "AND"},
        {"\\|\\|", "OR"},
        {"!", "NOT"},
        {">=", "MAIORIGUAL"},
        {"<=", "MENORIGUAL"},
        {">", "MAIOR"},
        {"<", "MENOR"},
        {"\\+", "MAIS"},
        {"-", "MENOS"},
        {"\\*", "MULT"},
        {"/", "DIV"},
        {"=", "ATRIBUICAO"},
        {";", "PONTOVIRGULA"},
        {",", "VIRGULA"},
        {"\\.", "PONTO"},
        {"\\(", "ABREPAREN"},
        {"\\)", "FECHAPAREN"},
        {"\\{", "ABRECHAVE"},
        {"\\}", "FECHACHAVE"},
        {"\\$[a-zA-Z_][a-zA-Z0-9_]*", "VARIAVEL"},
        {"[a-zA-Z_][a-zA-Z0-9_]*", "IDENT"},
        {"\\d+(\\.\\d+)?", "NUMERO"},
        {"\\s+", "ESPACOEMBRANCO"}
    };

    private static final Map<String, TipoToken> PALAVRAS_RESERVADAS = new HashMap<>();

    static {
        PALAVRAS_RESERVADAS.put("echo", TipoToken.ECHO);
        PALAVRAS_RESERVADAS.put("if", TipoToken.IF);
        PALAVRAS_RESERVADAS.put("else", TipoToken.ELSE);
        PALAVRAS_RESERVADAS.put("while", TipoToken.WHILE);
        PALAVRAS_RESERVADAS.put("function", TipoToken.FUNCTION);
        PALAVRAS_RESERVADAS.put("floatval", TipoToken.FLOATVAL);
        PALAVRAS_RESERVADAS.put("readline", TipoToken.READLINE);
        PALAVRAS_RESERVADAS.put("PHP_EOL", TipoToken.PHP_EOL);
    }

    private final String codigoFonte;
    private int posicao;
    private int linha;
    private int coluna;
    private final Pattern regexMestre;
    private final List<String> gruposOrdenados;

    /**
     * Cria um scanner para o codigo-fonte informado
     */
    public AnalisadorLexico(String codigoFonte) {
        this.codigoFonte = codigoFonte;
        this.posicao = 0;
        this.linha = 1;
        this.coluna = 1;
        this.gruposOrdenados = new ArrayList<>();
        this.regexMestre = compilarRegex();
    }

    /**
     * Compila a regex master com grupos nomeados para cada token
     */
    private Pattern compilarRegex() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < PADROES.length; i++) {
            String padrao = PADROES[i][0];
            String nome = PADROES[i][1];
            if (i > 0) {
                builder.append("|");
            }
            builder.append("(?<").append(nome).append(">")
                .append(padrao)
                .append(")");
            gruposOrdenados.add(nome);
        }
        return Pattern.compile(builder.toString(), Pattern.DOTALL);
    }

    /**
     * Atualiza linha/coluna a partir de um trecho consumido
     */
    private void atualizarPosicao(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            char ch = texto.charAt(i);
            if (ch == '\n') {
                linha += 1;
                coluna = 1;
            } else {
                coluna += 1;
            }
        }
    }

    /**
     * Converte um tipo textual e valor para um Token do enum TipoToken
     */
    private Token criarToken(String tipoStr, String valor, int linhaAtual, int colunaAtual) throws ErroLexico {
        if ("ESPACOEMBRANCO".equals(tipoStr) || "COMENTARIO".equals(tipoStr) || "COMENTARIOLINHA".equals(tipoStr)) {
            Debug.lexico("Ignorando: " + tipoStr + " na linha " + linhaAtual);
            return null;
        }

        if ("IDENT".equals(tipoStr)) {
            TipoToken reservado = PALAVRAS_RESERVADAS.get(valor);
            if (reservado != null) {
                return new Token(reservado, valor, linhaAtual, colunaAtual);
            }
            return new Token(TipoToken.IDENT, valor, linhaAtual, colunaAtual);
        }

        if ("NUMERO".equals(tipoStr)) {
            double numero = Double.parseDouble(valor);
            return new Token(TipoToken.NUMERO, numero, linhaAtual, colunaAtual);
        }

        switch (tipoStr) {
            case "PHPABRE":
                return new Token(TipoToken.PHP_ABRE, valor, linhaAtual, colunaAtual);
            case "PHPFECHA":
                return new Token(TipoToken.PHP_FECHA, valor, linhaAtual, colunaAtual);
            case "IGUAL":
                return new Token(TipoToken.IGUAL, valor, linhaAtual, colunaAtual);
            case "DIFERENTE":
                return new Token(TipoToken.DIFERENTE, valor, linhaAtual, colunaAtual);
            case "MAIORIGUAL":
                return new Token(TipoToken.MAIOR_IGUAL, valor, linhaAtual, colunaAtual);
            case "MENORIGUAL":
                return new Token(TipoToken.MENOR_IGUAL, valor, linhaAtual, colunaAtual);
            case "MAIOR":
                return new Token(TipoToken.MAIOR, valor, linhaAtual, colunaAtual);
            case "MENOR":
                return new Token(TipoToken.MENOR, valor, linhaAtual, colunaAtual);
            case "MAIS":
                return new Token(TipoToken.MAIS, valor, linhaAtual, colunaAtual);
            case "MENOS":
                return new Token(TipoToken.MENOS, valor, linhaAtual, colunaAtual);
            case "MULT":
                return new Token(TipoToken.MULT, valor, linhaAtual, colunaAtual);
            case "DIV":
                return new Token(TipoToken.DIV, valor, linhaAtual, colunaAtual);
            case "AND":
                return new Token(TipoToken.AND, valor, linhaAtual, colunaAtual);
            case "OR":
                return new Token(TipoToken.OR, valor, linhaAtual, colunaAtual);
            case "NOT":
                return new Token(TipoToken.NOT, valor, linhaAtual, colunaAtual);
            case "ATRIBUICAO":
                return new Token(TipoToken.ATRIBUICAO, valor, linhaAtual, colunaAtual);
            case "PONTOVIRGULA":
                return new Token(TipoToken.PONTO_VIRGULA, valor, linhaAtual, colunaAtual);
            case "VIRGULA":
                return new Token(TipoToken.VIRGULA, valor, linhaAtual, colunaAtual);
            case "PONTO":
                return new Token(TipoToken.PONTO, valor, linhaAtual, colunaAtual);
            case "ABREPAREN":
                return new Token(TipoToken.ABRE_PAREN, valor, linhaAtual, colunaAtual);
            case "FECHAPAREN":
                return new Token(TipoToken.FECHA_PAREN, valor, linhaAtual, colunaAtual);
            case "ABRECHAVE":
                return new Token(TipoToken.ABRE_CHAVE, valor, linhaAtual, colunaAtual);
            case "FECHACHAVE":
                return new Token(TipoToken.FECHA_CHAVE, valor, linhaAtual, colunaAtual);
            case "VARIAVEL":
                return new Token(TipoToken.VARIAVEL, valor, linhaAtual, colunaAtual);
            default:
                throw new ErroLexico("Tipo de token desconhecido: " + tipoStr, linhaAtual, colunaAtual);
        }
    }

    /**
     * Retorna o proximo token da entrada (ou EOF)
     */
    public Token proximoToken() throws ErroLexico {
        while (posicao < codigoFonte.length()) {
            Matcher casador = regexMestre.matcher(codigoFonte);
            casador.region(posicao, codigoFonte.length());
            if (!casador.lookingAt()) {
                char caractere = codigoFonte.charAt(posicao);
                throw new ErroLexico("Caractere inesperado: '" + caractere + "'", linha, coluna);
            }

            String tipoTexto = null;
            String lexema = null;
            for (String grupoNome : gruposOrdenados) {
                String trecho = casador.group(grupoNome);
                if (trecho != null) {
                    tipoTexto = grupoNome;
                    lexema = trecho;
                    break;
                }
            }

            if (tipoTexto == null) {
                throw new ErroLexico("Token desconhecido", linha, coluna);
            }

            int linhaAtual = linha;
            int colunaAtual = coluna;

            atualizarPosicao(lexema);
            posicao = casador.end();

            Token tokenGerado = criarToken(tipoTexto, lexema, linhaAtual, colunaAtual);
            if (tokenGerado != null) {
                Debug.lexico("Token: " + tokenGerado.getTipo().name() + " = '" + tokenGerado.getValor() + "' [" + tokenGerado.getLinha() + ":" + tokenGerado.getColuna() + "]");
                return tokenGerado;
            }
        }

        Debug.lexico("Token: EOF [" + linha + ":" + coluna + "]");
        return new Token(TipoToken.EOF, null, linha, coluna);
    }

    /**
     * Olha o proximo token sem consumir
     */
    public Token olharProximoToken() throws ErroLexico {
        int pos = posicao;
        int linhaSalva = linha;
        int colunaSalva = coluna;
        Token token = proximoToken();
        posicao = pos;
        linha = linhaSalva;
        coluna = colunaSalva;
        return token;
    }

    /**
     * Tokeniza todo o codigo, incluindo o token EOF ao final
     */
    public List<Token> tokenizar() throws ErroLexico {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            Token token = proximoToken();
            tokens.add(token);
            if (token.getTipo() == TipoToken.EOF) {
                break;
            }
        }
        return tokens;
    }
}
