package compilador;

public class Debug {

    // Ativa todos os debugs
    public static boolean ENABLED = false;

    // Flags individuais por etapa
    public static boolean LEXICO = false;
    public static boolean SINTATICO = false;
    public static boolean SEMANTICO = false;
    public static boolean GERADOR = false;
    public static boolean MAQUINA_HIPOTETICA = false;

    // Nivel de indentacao para prints hierarquicos
    private static int indentLevel = 0;

    public static void ativarTodos() {
        ENABLED = true;
        LEXICO = true;
        SINTATICO = true;
        SEMANTICO = true;
        GERADOR = true;
        MAQUINA_HIPOTETICA = true;
    }

    public static void lexico(String msg) {
        if (ENABLED && LEXICO) {
            System.out.println("[LEXICO] " + msg);
        }
    }

    public static void sintatico(String msg) {
        if (ENABLED && SINTATICO) {
            System.out.println("[SINTATICO] " + indent() + msg);
        }
    }

    public static void entrarRegra(String regra) {
        if (ENABLED && SINTATICO) {
            System.out.println("[SINTATICO] " + indent() + "-> " + regra);
            indentLevel++;
        }
    }

    public static void sairRegra(String regra) {
        if (ENABLED && SINTATICO) {
            indentLevel--;
            System.out.println("[SINTATICO] " + indent() + "<- " + regra);
        }
    }

    public static void semantico(String msg) {
        if (ENABLED && SEMANTICO) {
            System.out.println("[SEMANTICO] " + msg);
        }
    }

    public static void gerador(String msg) {
        if (ENABLED && GERADOR) {
            System.out.println("[GERADOR] " + msg);
        }
    }

    public static void instrucao(int endereco, String mnemonico, String argumento) {
        if (ENABLED && GERADOR) {
            String arg = argumento != null ? " " + argumento : "";
            System.out.println("[GERADOR] " + String.format("%04d: %s%s", endereco, mnemonico, arg));
        }
    }

    public static void maquinaHipotetica(String msg) {
        if (ENABLED && MAQUINA_HIPOTETICA) {
            System.out.println("[MaqHipo] " + msg);
        }
    }

    public static void maquinaHipoteticaEstado(int pc, String mnemonico, Object arg, java.util.List<Double> pilha) {
        if (ENABLED && MAQUINA_HIPOTETICA) {
            String argStr = arg != null ? " " + arg : "";
            System.out.println("[MaqHipo] PC=" + String.format("%04d", pc) + " | " + mnemonico + argStr + " | Pilha: " + formatarPilha(pilha));
        }
    }

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

    private static String indent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

}
