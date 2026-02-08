package compilador.lexico;

/**
 * Tipos de tokens reconhecidos pelo analisador lexico
 */
public enum TipoToken {
    PHP_ABRE,
    PHP_FECHA,
    ECHO,
    IF,
    ELSE,
    WHILE,
    FUNCTION,
    FLOATVAL,
    READLINE,
    PHP_EOL,
    IGUAL,
    DIFERENTE,
    MAIOR_IGUAL,
    MENOR_IGUAL,
    MAIOR,
    MENOR,
    MAIS,
    MENOS,
    MULT,
    DIV,
    AND,
    OR,
    NOT,
    ATRIBUICAO,
    PONTO_VIRGULA,
    VIRGULA,
    PONTO,
    ABRE_PAREN,
    FECHA_PAREN,
    ABRE_CHAVE,
    FECHA_CHAVE,
    VARIAVEL,
    IDENT,
    NUMERO,
    EOF,
    ERRO
}
