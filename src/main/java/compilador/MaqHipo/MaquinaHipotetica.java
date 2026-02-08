package compilador.MaqHipo;

import compilador.Debug;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Maquina hipotetica baseada em pilha que executa codigo objeto
 */
public class MaquinaHipotetica {
    
    private final List<Double> pilha = new ArrayList<>();
    private final List<Instrucao> instrucoes = new ArrayList<>();
    private int contadorPrograma = 0;
    private final List<Integer> pilhaRetorno = new ArrayList<>();
    private boolean executando = false;
    private final BufferedReader leitor = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

    /**
     * Converte uma linha de texto em uma instrucao
     */
    private Instrucao lerLinhaInstrucao(String linha) {
        String limpa = linha.trim();
        if (limpa.isEmpty()) {
            return null;
        }
        String[] partes = limpa.split("\\s+", 2);
        String mnemonico = partes[0];
        Double argumento = null;
        String comentario = null;
        if (partes.length > 1) {
            String argLimpo = partes[1];
            int hash = argLimpo.indexOf('#');
            if (hash >= 0) {
                comentario = argLimpo.substring(hash + 1).trim();
                argLimpo = argLimpo.substring(0, hash).trim();
            }
            if (!argLimpo.isEmpty()) {
                String[] argPartes = argLimpo.split("\\s+");
                try {
                    argumento = Double.parseDouble(argPartes[0]);
                } catch (NumberFormatException e) {
                    argumento = null;
                }
            }
        }
        return new Instrucao(mnemonico, argumento, comentario);
    }

    /**
     * Carrega instrucoes a partir de um arquivo de codigo objeto
     */
    public void carregarArquivo(String caminho) throws IOException {
        instrucoes.clear();
        List<String> linhas = Files.readAllLines(Path.of(caminho), StandardCharsets.UTF_8);
        for (String linha : linhas) {
            Instrucao instrucao = lerLinhaInstrucao(linha);
            if (instrucao != null) {
                instrucoes.add(instrucao);
            }
        }
    }

    /**
     * Carrega instrucoes a partir de um texto contendo o codigo objeto
     */
    public void carregarInstrucoes(String texto) {
        instrucoes.clear();
        String[] linhas = texto.trim().split("\n");
        for (String linha : linhas) {
            Instrucao instrucao = lerLinhaInstrucao(linha);
            if (instrucao != null) {
                instrucoes.add(instrucao);
            }
        }
    }

    /**
     * Executa o programa carregado na memoria da MaqHipo
     */
    public void executar() throws ErrosMaquinaHipotetica, IOException {
        Debug.maquinaHipotetica("=== Iniciando Execucao da Maquina Hipotetica ===");
        Debug.maquinaHipotetica("Total de instrucoes: " + instrucoes.size());
        contadorPrograma = 0;
        pilha.clear();
        executando = true;
        pilhaRetorno.clear();

        while (executando && contadorPrograma < instrucoes.size()) {
            Instrucao instrucao = instrucoes.get(contadorPrograma);
            Debug.maquinaHipoteticaEstado(contadorPrograma, instrucao.mnemonico, instrucao.argumento, pilha);
            executarInstrucao(instrucao);
        }
        Debug.maquinaHipotetica("=== Execucao da Maquina Hipotetica Concluida ===");
    }

    /**
     * Resolve enderecos negativos (parametros) para endereco positivo
     */
    private int resolverEndereco(Double valor) {
        int end = valor != null ? valor.intValue() : 0;
        if (end < 0) {
            return -end - 1;
        }
        return end;
    }

    /**
     * Instrucao INPP: inicializa a execucao
     */
    private void execInpp() {
        contadorPrograma += 1;
    }

    /**
     * Instrucao PARA: encerra a execucao
     */
    private void execPara() {
        executando = false;
    }

    /**
     * Instrucao ALME: reserva espaco na pilha
     */
    private void execAlme(Double arg) {
        int n = arg != null ? arg.intValue() : 0;
        for (int i = 0; i < n; i++) {
            pilha.add(0.0);
        }
        contadorPrograma += 1;
    }

    /**
     * Instrucao DESM: desaloca espaco da pilha
     */
    private void execDesm(Double arg) {
        int n = arg != null ? arg.intValue() : 0;
        for (int i = 0; i < n; i++) {
            if (!pilha.isEmpty()) {
                pilha.remove(pilha.size() - 1);
            }
        }
        contadorPrograma += 1;
    }

    /**
     * Instrucao ARMZ: armazena valor no endereco indicado
     */
    private void execArmz(Double arg) {
        int end = resolverEndereco(arg);
        double valor = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        while (end >= pilha.size()) {
            pilha.add(0.0);
        }
        pilha.set(end, valor);
        contadorPrograma += 1;
    }

    /**
     * Instrucao CRVL: carrega valor do endereco indicado
     */
    private void execCrvl(Double arg) {
        int end = resolverEndereco(arg);
        double valor = end < pilha.size() ? pilha.get(end) : 0.0;
        pilha.add(valor);
        contadorPrograma += 1;
    }

    /**
     * Instrucao CRCT: carrega constante
     */
    private void execCrct(Double arg) {
        double valor = arg != null ? arg : 0.0;
        pilha.add(valor);
        contadorPrograma += 1;
    }

    /**
     * Instrucao SOMA: soma os dois valores do topo da pilha
     */
    private void execSoma() {
        double op2 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        double op1 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        pilha.add(op1 + op2);
        contadorPrograma += 1;
    }

    /**
     * Instrucao SUBT: subtrai os dois valores do topo da pilha
     */
    private void execSubt() {
        double op2 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        double op1 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        pilha.add(op1 - op2);
        contadorPrograma += 1;
    }

    /**
     * Instrucao MULT: multiplica os dois valores do topo da pilha
     */
    private void execMult() {
        double op2 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        double op1 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        pilha.add(op1 * op2);
        contadorPrograma += 1;
    }

    /**
     * Instrucao DIVI: divide os dois valores do topo da pilha
     */
    private void execDivi() throws ErrosMaquinaHipotetica {
        double op2 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        double op1 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        if (op2 == 0) {
            throw new ErrosMaquinaHipotetica("Divisao por zero");
        }
        pilha.add(op1 / op2);
        contadorPrograma += 1;
    }

    /**
     * Instrucao DSVI: desvio incondicional
     */
    private void execDsvi(Double arg) {
        contadorPrograma = arg != null ? arg.intValue() : 0;
    }

    /**
     * Instrucao DSVF: desvio se falso
     */
    private void execDsvf(Double arg) {
        double valor = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        if (valor == 0) {
            contadorPrograma = arg != null ? arg.intValue() : 0;
        } else {
            contadorPrograma += 1;
        }
    }

    /**
     * Instrucao LEIT: le um valor do usuario e empilha
     */
    private void execLeit(String comentario) throws IOException {
        String alvo = comentario;
        if (alvo != null && !alvo.isBlank()) {
            System.out.print("[Entrada " + alvo + "] ");
        } else {
            System.out.print("[Entrada] ");
        }
        System.out.flush();
        String entrada = leitor.readLine();
        if (entrada == null) {
            throw new EOFException("EOF");
        }
        double valor;
        try {
            valor = Double.parseDouble(entrada);
        } catch (NumberFormatException e) {
            valor = 0.0;
        }
        pilha.add(valor);
        contadorPrograma += 1;
    }

    /**
     * Instrucao IMPR: imprime o valor no topo da pilha
     */
    private void execImpr() {
        double valor = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        String saida;
        if (valor == Math.rint(valor)) {
            saida = String.valueOf((long) valor);
        } else {
            saida = String.valueOf(valor);
        }
        System.out.println("[Saida] " + saida);
        contadorPrograma += 1;
    }

    /**
     * Instrucao INVE: inverte o sinal do valor no topo da pilha
     */
    private void execInve() {
        double valor = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        pilha.add(-valor);
        contadorPrograma += 1;
    }

    /**
     * Instrucao CONJ: conjuncao logica (AND)
     */
    private void execConj() {
        double op2 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        double op1 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        pilha.add((op1 == 1.0 && op2 == 1.0) ? 1.0 : 0.0);
        contadorPrograma += 1;
    }

    /**
     * Instrucao DISJ: disjuncao logica (OR)
     */
    private void execDisj() {
        double op2 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        double op1 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        pilha.add((op1 == 1.0 || op2 == 1.0) ? 1.0 : 0.0);
        contadorPrograma += 1;
    }

    /**
     * Instrucao NEGA: negacao logica
     */
    private void execNega() {
        double valor = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
        pilha.add(1.0 - valor);
        contadorPrograma += 1;
    }

    /**
     * Decodifica e executa uma instrucao da MaqHipo
     */
    private void executarInstrucao(Instrucao instrucao) throws ErrosMaquinaHipotetica, IOException {
        String mnem = instrucao.mnemonico;
        Double arg = instrucao.argumento;

        switch (mnem) {
            case "INPP":
                execInpp();
                break;
            case "PARA":
                execPara();
                break;
            case "ALME": {
                execAlme(arg);
                break;
            }
            case "DESM": {
                execDesm(arg);
                break;
            }
            case "ARMZ": {
                execArmz(arg);
                break;
            }
            case "CRVL": {
                execCrvl(arg);
                break;
            }
            case "CRCT": {
                execCrct(arg);
                break;
            }
            case "SOMA": {
                execSoma();
                break;
            }
            case "SUBT": {
                execSubt();
                break;
            }
            case "MULT": {
                execMult();
                break;
            }
            case "DIVI": {
                execDivi();
                break;
            }
            case "DSVI":
                execDsvi(arg);
                break;
            case "DSVF": {
                execDsvf(arg);
                break;
            }
            case "LEIT": {
                execLeit(instrucao.comentario);
                break;
            }
            case "IMPR": {
                execImpr();
                break;
            }
            case "INVE": {
                execInve();
                break;
            }
            case "CONJ": {
                execConj();
                break;
            }
            case "DISJ": {
                execDisj();
                break;
            }
            case "NEGA": {
                execNega();
                break;
            }
            case "CPIG": {
                double op2 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
                double op1 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
                pilha.add(op1 == op2 ? 1.0 : 0.0);
                contadorPrograma += 1;
                break;
            }
            case "CDES": {
                double op2 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
                double op1 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
                pilha.add(op1 != op2 ? 1.0 : 0.0);
                contadorPrograma += 1;
                break;
            }
            case "CMAI": {
                double op2 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
                double op1 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
                pilha.add(op1 >= op2 ? 1.0 : 0.0);
                contadorPrograma += 1;
                break;
            }
            case "CPMI": {
                double op2 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
                double op1 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
                pilha.add(op1 <= op2 ? 1.0 : 0.0);
                contadorPrograma += 1;
                break;
            }
            case "CPMA": {
                double op2 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
                double op1 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
                pilha.add(op1 > op2 ? 1.0 : 0.0);
                contadorPrograma += 1;
                break;
            }
            case "CPME": {
                double op2 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
                double op1 = pilha.isEmpty() ? 0.0 : pilha.remove(pilha.size() - 1);
                pilha.add(op1 < op2 ? 1.0 : 0.0);
                contadorPrograma += 1;
                break;
            }
            case "PUSHER": {
                int end = arg != null ? arg.intValue() : 0;
                pilhaRetorno.add(end);
                contadorPrograma += 1;
                break;
            }
            case "PARAM": {
                int indice = arg != null ? arg.intValue() : 0;
                double valor = indice < pilha.size() ? pilha.get(indice) : 0.0;
                pilha.add(valor);
                contadorPrograma += 1;
                break;
            }
            case "CHPR": {
                int endereco = arg != null ? arg.intValue() : 0;
                contadorPrograma = endereco;
                break;
            }
            case "RTPR": {
                Debug.maquinaHipotetica("  RTPR: Retornando de funcao");
                if (!pilhaRetorno.isEmpty()) {
                    int endRetorno = pilhaRetorno.remove(pilhaRetorno.size() - 1);
                    Debug.maquinaHipotetica("    Endereco de retorno: " + endRetorno);
                    contadorPrograma = endRetorno;
                } else {
                    Debug.maquinaHipotetica("    AVISO: Pilha vazia, sem endereco de retorno!");
                    contadorPrograma += 1;
                }
                break;
            }
            default:
                throw new ErrosMaquinaHipotetica("Instrucao desconhecida: " + mnem);
        }
    }

    /**
     * Retorna uma string com o estado atual da execucao
     */
    public String debugEstado() {
        return "PC: " + contadorPrograma + ", Pilha: " + pilha;
    }
}

class ErrosMaquinaHipotetica extends Exception {
    private final String mensagem;

    /**
     * Cria um erro de execucao da maquina hipotetica
     */
    public ErrosMaquinaHipotetica(String mensagem) {
        super("Erro Maquina Hipotetica: " + mensagem);
        this.mensagem = mensagem;
    }

    /**
     * Retorna a mensagem base do erro
     */
    public String getMensagem() {
        return mensagem;
    }
}
