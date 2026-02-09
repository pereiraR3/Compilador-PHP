package compilador.gerador;

public class Instrucao {

    public final String mnemonico;
    public String argumento;
    public String comentario;

    public Instrucao(String mnemonico, String argumento, String comentario) {
        this.mnemonico = mnemonico;
        this.argumento = argumento;
        this.comentario = comentario;
    }
    @Override
    public String toString() {
        if (argumento != null) {
            if (comentario != null) {
                return mnemonico + " " + argumento + " " + comentario;
            }
            return mnemonico + " " + argumento;
        }
        if (comentario != null) {
            return mnemonico + " " + comentario;
        }
        return mnemonico;
    }
}
