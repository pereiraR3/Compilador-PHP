package compilador;

import compilador.arvore_sintatica_abstrata.Programa;
import compilador.gerador.GeradorCodigo;
import compilador.gerador.Instrucao;
import compilador.lexico.ErroLexico;
import compilador.lexico.AnalisadorLexico;
import compilador.lexico.Token;
import compilador.sintatico.AnalisadorSintatico;
import compilador.sintatico.ErroSintatico;
import compilador.semantico.AnalisadorSemantico;
import compilador.semantico.ErroSemantico;
import compilador.semantico.Simbolo;
import compilador.semantico.TabelaSimbolos;
import compilador.semantico.TipoSimbolo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Main {

    private static void limparArquivosErro(Path... caminhos) throws IOException {
        
        for (Path caminho : caminhos) {
            Files.deleteIfExists(caminho);
        }
    }

    private static void salvarErro(Path caminho, String mensagem) {

        try {
            Files.writeString(caminho, mensagem + System.lineSeparator(), StandardCharsets.UTF_8);
            System.out.println("        Erros salvos em: " + caminho);
        } catch (IOException io) {
            System.out.println("Erro ao salvar erros: " + io.getMessage());
        }
    }

    private static void salvarTexto(Path caminho, String conteudo, String rotulo) throws IOException {

        Files.writeString(caminho, conteudo, StandardCharsets.UTF_8);
        System.out.println("        " + rotulo + " salvo em: " + caminho);
    }

    private static String formatarTabelaSimbolos(Map<String, TabelaSimbolos> tabelasPorEscopo) {

        List<String> escopos = new ArrayList<>(tabelasPorEscopo.keySet());
        escopos.sort(String::compareTo);

        StringBuilder construtor = new StringBuilder();
        for (String escopo : escopos) {
            TabelaSimbolos tabela = tabelasPorEscopo.get(escopo);
            Map<String, Simbolo> simbolos = tabela.getTodosSimbolos();
            List<Simbolo> lista = new ArrayList<>(simbolos.values());
            lista.sort(Comparator.comparing(s -> s.nome));

            construtor.append("Tabela de Simbolos (escopo ").append(escopo).append(")")
                .append(System.lineSeparator());
            for (Simbolo simbolo : lista) {
                construtor.append("- nome=").append(simbolo.nome)
                    .append(" tipo=").append(simbolo.tipo)
                    .append(" escopo=").append(simbolo.escopo);
                if (simbolo.tipo == TipoSimbolo.FUNCAO) {
                    construtor.append(" parametros=").append(simbolo.parametros)
                        .append(" num_locais=").append(simbolo.numVariaveisLocais)
                        .append(" endereco_inicio=").append(simbolo.enderecoInicio);
                } else {
                    construtor.append(" endereco=").append(simbolo.endereco);
                }
                construtor.append(System.lineSeparator());
            }
            construtor.append(System.lineSeparator());
        }
        return construtor.toString();
    }

    private static Path resolverSaida(Path saidasDir, String caminhoSaida) {

        if (caminhoSaida == null || caminhoSaida.isBlank()) {
            return saidasDir.resolve("codigo.objeto.txt");
        }
        Path nomeArquivo = Path.of(caminhoSaida).getFileName();
        if (nomeArquivo == null) {
            return saidasDir.resolve("codigo.objeto.txt");
        }
        return saidasDir.resolve(nomeArquivo);
    }

    public static boolean compilar(String caminhoEntrada, String caminhoSaida) {

        System.out.println("Compilando: " + caminhoEntrada);
        String codigoFonte;
        try {
            codigoFonte = Files.readString(Path.of(caminhoEntrada), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Erro ao ler arquivo: " + e.getMessage());
            return false;
        }

        Path saidasDir = Path.of("saidas");
        try {
            Files.createDirectories(saidasDir);
        } catch (IOException e) {
            System.out.println("Erro ao criar pasta de saidas: " + e.getMessage());
            return false;
        }

        Path caminhoTokens = saidasDir.resolve("tokens.txt");
        Path caminhoAst = saidasDir.resolve("ast.txt");
        Path caminhoTabela = saidasDir.resolve("tabela_simbolos.txt");
        Path caminhoErrosLexicos = saidasDir.resolve("erros_lexicos.txt");
        Path caminhoErrosSintaticos = saidasDir.resolve("erros_sintaticos.txt");
        Path caminhoErrosSemanticos = saidasDir.resolve("erros_semanticos.txt");
        Path caminhoCodigoObjeto = resolverSaida(saidasDir, caminhoSaida);
        try {
            limparArquivosErro(caminhoErrosLexicos, caminhoErrosSintaticos, caminhoErrosSemanticos);
        } catch (IOException e) {
            System.out.println("Erro ao limpar arquivos de erro: " + e.getMessage());
            return false;
        }

        System.out.println("=== Etapa 1: Analise Lexica ===");
        try {
            AnalisadorLexico analisadorLexico = new AnalisadorLexico(codigoFonte);
            List<Token> tokensPrevia = analisadorLexico.tokenizar();
            StringBuilder tokensTexto = new StringBuilder();
            for (Token token : tokensPrevia) {
                tokensTexto.append(token).append(System.lineSeparator());
            }
            salvarTexto(caminhoTokens, tokensTexto.toString(), "Tokens");
            System.out.println("Analise Lexica concluida com sucesso.\n");
        } catch (ErroLexico e) {
            String mensagem = "Erro Lexico: " + e.getMessage();
            System.out.println(mensagem);
            salvarErro(caminhoErrosLexicos, mensagem);
            return false;
        } catch (IOException e) {
            System.out.println("Erro ao salvar tokens: " + e.getMessage());
            return false;
        }

        System.out.println("=== Etapa 2: Analise Sintatica ===");
        Programa ast;
        try {
            AnalisadorLexico analisadorLexico = new AnalisadorLexico(codigoFonte);
            AnalisadorSintatico analisadorSintatico = new AnalisadorSintatico(analisadorLexico);
            ast = analisadorSintatico.analisar();
            ImprimaAST impressor = new ImprimaAST("        ");
            String astTexto = impressor.imprimir(ast);
            salvarTexto(caminhoAst, astTexto, "AST");
            System.out.println("Analise Sintatica concluida com sucesso.\n");
        } catch (ErroLexico | ErroSintatico e) {
            String mensagem = "Erro Sintatico: " + e.getMessage();
            System.out.println(mensagem);
            salvarErro(caminhoErrosSintaticos, mensagem);
            return false;
        } catch (IOException e) {
            System.out.println("Erro ao salvar AST: " + e.getMessage());
            return false;
        }

        System.out.println("=== Etapa 3: Analise Semantica ===");
        TabelaSimbolos tabelaSimbolos;
        try {
            AnalisadorSemantico analisador = new AnalisadorSemantico();
            tabelaSimbolos = analisador.analisar(ast);
            String tabelaTexto = formatarTabelaSimbolos(analisador.getTabelasPorEscopo());
            salvarTexto(caminhoTabela, tabelaTexto, "Tabela de simbolos");
            System.out.println("Analise Semantica concluida com sucesso.\n");
        } catch (ErroSemantico e) {
            String mensagem = "Erro Semantico: " + e.getMensagem();
            System.out.println(mensagem);
            salvarErro(caminhoErrosSemanticos, mensagem);
            return false;
        } catch (IOException e) {
            System.out.println("Erro ao salvar tabela de simbolos: " + e.getMessage());
            return false;
        }

        System.out.println("=== Etapa 4: Geracao de Codigo Objeto ===");
        try {
            GeradorCodigo gerador = new GeradorCodigo(tabelaSimbolos);
            gerador.gerar(ast);

            gerador.exportar(caminhoCodigoObjeto.toString());
            System.out.println("Geracao de Codigo Objeto concluida com sucesso.");
            System.out.println("Codigo objeto salvo em: " + caminhoCodigoObjeto);
        } catch (Exception e) {
            System.out.println("Erro na geracao de codigo: " + e.getMessage());
            return false;
        }

        System.out.println("\nCompilacao concluida com sucesso!");
        return true;
    }

    public static void main(String[] args) {

        String caminhoSaida = Path.of("saidas", "codigo.objeto.txt").toString();

        // Processa argumentos
        List<String> argumentos = new ArrayList<>();
        boolean debugTudo = false;
        boolean debugLexico = false;
        boolean debugSintatico = false;
        boolean debugSemantico = false;
        boolean debugGerador = false;
        boolean debugMaquinaHipotetica = false;

        for (String arg : args) {
            if (arg.equals("-debug") || arg.equals("--debug")) {
                debugTudo = true;
            } else if (arg.equals("-debug-lexico")) {
                debugLexico = true;
            } else if (arg.equals("-debug-sintatico")) {
                debugSintatico = true;
            } else if (arg.equals("-debug-semantico")) {
                debugSemantico = true;
            } else if (arg.equals("-debug-gerador")) {
                debugGerador = true;
            } else if (arg.equals("-debug-maquina-hipotetica")) {
                debugMaquinaHipotetica = true;
            } else {
                argumentos.add(arg);
            }
        }

        // Configura debug
        if (debugTudo) {
            Debug.ativarTodos();
        } else {
            Debug.ENABLED = debugLexico || debugSintatico || debugSemantico || debugGerador || debugMaquinaHipotetica;
            Debug.LEXICO = debugLexico;
            Debug.SINTATICO = debugSintatico;
            Debug.SEMANTICO = debugSemantico;
            Debug.GERADOR = debugGerador;
            Debug.MAQUINA_HIPOTETICA = debugMaquinaHipotetica;
        }

        if (argumentos.isEmpty()) {
            System.out.println("Uso: java -cp out compilador.Main [opcoes] <arquivo.php> [arquivo_saida]");
            System.out.println("Observacao: as saidas sao sempre gravadas em 'saidas/'.");
            System.out.println("\nOpcoes de debug:");
            System.out.println("  -debug           Ativa debug de todas as etapas");
            System.out.println("  -debug-lexico    Ativa debug do analisador lexico");
            System.out.println("  -debug-sintatico Ativa debug do analisador sintatico");
            System.out.println("  -debug-semantico Ativa debug do analisador semantico");
            System.out.println("  -debug-gerador   Ativa debug do gerador de codigo");
            System.out.println("  -debug-maquina-hipotetica Ativa debug da maquina hipotetica");
            System.out.println("\nExemplo:");
            System.out.println("  java -cp out compilador.Main dados/correto.php");
            System.out.println("  java -cp out compilador.Main -debug dados/correto.php");
            System.out.println("  java -cp out compilador.Main -debug-sintatico dados/correto.php");
            System.exit(1);
        }

        String caminhoEntrada = argumentos.get(0);

        if (argumentos.size() > 1) {
            caminhoSaida = argumentos.get(1);
        }

        boolean sucesso = compilar(caminhoEntrada, caminhoSaida);
        if (!sucesso) {
            System.exit(1);
        }
    }
}
