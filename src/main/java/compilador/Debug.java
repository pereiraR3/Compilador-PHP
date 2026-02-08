package compilador;

public class Debug {

    // Flag global - ativa todos os debugs
    public static boolean ENABLED = false;

    // Flags individuais por etapa
    public static boolean LEXICO = false;
    public static boolean SINTATICO = false;
    public static boolean SEMANTICO = false;
    public static boolean GERADOR = false;
    public static boolean MAQUINA_HIPOTETICA = false;

    // Nivel de indentacao para prints hierarquicos
    private static int indentLevel = 0;

    /**
     * Ativa todos os debugs
     */
    public static void ativarTodos() {
        ENABLED = true;
        LEXICO = true;
        SINTATICO = true;
        SEMANTICO = true;
        GERADOR = true;
        MAQUINA_HIPOTETICA = true;
    }

    /**
     * Desativa todos os debugs
     */
    public static void desativarTodos() {
        ENABLED = false;
        LEXICO = false;
        SINTATICO = false;
        SEMANTICO = false;
        GERADOR = false;
        MAQUINA_HIPOTETICA = false;
    }

    /**
     * Imprime mensagem de debug para o AnalisadorLexico
     */
    public static void lexico(String msg) {
        if (ENABLED && LEXICO) {
            System.out.println("[LEXICO] " + msg);
        }
    }

    /**
     * Imprime mensagem de debug para o AnalisadorSintatico
     */
    public static void sintatico(String msg) {
        if (ENABLED && SINTATICO) {
            System.out.println("[SINTATICO] " + indent() + msg);
        }
    }

    /**
     * Imprime entrada em regra gramatical
     */
    public static void entrarRegra(String regra) {
        if (ENABLED && SINTATICO) {
            System.out.println("[SINTATICO] " + indent() + "-> " + regra);
            indentLevel++;
        }
    }

    /**
     * Imprime saida de regra gramatical
     */
    public static void sairRegra(String regra) {
        if (ENABLED && SINTATICO) {
            indentLevel--;
            System.out.println("[SINTATICO] " + indent() + "<- " + regra);
        }
    }

    /**
     * Imprime mensagem de debug para o Analisador Semantico
     */
    public static void semantico(String msg) {
        if (ENABLED && SEMANTICO) {
            System.out.println("[SEMANTICO] " + msg);
        }
    }

    /**
     * Imprime mensagem de debug para o Gerador de Codigo
     */
    public static void gerador(String msg) {
        if (ENABLED && GERADOR) {
            System.out.println("[GERADOR] " + msg);
        }
    }

    /**
     * Imprime instrucao gerada
     */
    public static void instrucao(int endereco, String mnemonico, String argumento) {
        if (ENABLED && GERADOR) {
            String arg = argumento != null ? " " + argumento : "";
            System.out.println("[GERADOR] " + String.format("%04d: %s%s", endereco, mnemonico, arg));
        }
    }

    /**
     * Imprime mensagem de debug para a Maquina Hipotetica
     */
    public static void maquinaHipotetica(String msg) {
        if (ENABLED && MAQUINA_HIPOTETICA) {
            System.out.println("[MaqHipo] " + msg);
        }
    }

    /**
     * Imprime estado da Maquina Hipotetica (PC, pilha)
     */
    public static void maquinaHipoteticaEstado(int pc, String mnemonico, Object arg, java.util.List<Double> pilha) {
        if (ENABLED && MAQUINA_HIPOTETICA) {
            String argStr = arg != null ? " " + arg : "";
            System.out.println("[MaqHipo] PC=" + String.format("%04d", pc) + " | " + mnemonico + argStr + " | Pilha: " + formatarPilha(pilha));
        }
    }

    /**
     * Formata a pilha para exibicao
     */
    private static String formatarPilha(java.util.List<Double> pilha) {
        if (pilha == null || pilha.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        int start = Math.max(0, pilha.size() - 10); // Mostra apenas os ultimos 10 elementos
        if (start > 0) {
            sb.append("... ");
        }
        for (int i = start; i < pilha.size(); i++) {
            if (i > start) sb.append(", ");
            double val = pilha.get(i);
            if (val == Math.floor(val)) {
                sb.append((long) val);
            } else {
                sb.append(val);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Retorna string de indentacao
     */
    private static String indent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    /**
     * Reseta indentacao
     */
    public static void resetIndent() {
        indentLevel = 0;
    }
}
