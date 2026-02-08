package compilador.MaqHipo;

public class Instrucao {
    
    public final String mnemonico;
    public final Double argumento;
    public final String comentario;

    public Instrucao(String mnemonico, Double argumento, String comentario) {
        this.mnemonico = mnemonico;
        this.argumento = argumento;
        this.comentario = comentario;
    }
}
