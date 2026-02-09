package compilador.MaqHipo;

import compilador.Debug;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MaquinaHipoteticaMain {
    
    public static void main(String[] args) {
        String caminhoPadrao = Path.of("saidas", "codigo.objeto.txt").toString();

        // Processa argumentos
        List<String> argumentos = new ArrayList<>();
        boolean debugMaquinaHipotetica = false;

        for (String arg : args) {
            if (arg.equals("-debug") || arg.equals("--debug") || arg.equals("-debug-maquina-hipotetica")) {
                debugMaquinaHipotetica = true;
            } else {
                argumentos.add(arg);
            }
        }

        // Configura debug
        if (debugMaquinaHipotetica) {
            Debug.ENABLED = true;
            Debug.MAQUINA_HIPOTETICA = true;
        }

        String caminho = !argumentos.isEmpty() ? argumentos.get(0) : caminhoPadrao;

        if (!Files.exists(Path.of(caminho))) {
            System.out.println("Erro: Arquivo nao encontrado: " + caminho);
            System.out.println("Uso: java -cp out compilador.MaqHipo.MaquinaHipoteticaMain [opcoes] [caminho_do_arquivo]");
            System.out.println("\nOpcoes:");
            System.out.println("  -debug    Ativa debug da maquina hipotetica");
            System.exit(1);
        }

        MaquinaHipotetica vm = new MaquinaHipotetica();
        try {
            vm.carregarArquivo(caminho);
            vm.executar();
        } catch (ErrosMaquinaHipotetica e) {
            System.out.println("Erro de execucao: " + e.getMensagem());
            System.exit(1);
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
            System.exit(1);
        }
    }
}
