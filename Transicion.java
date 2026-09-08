/*class Transicion{
    char    simbInf;
    char    simbSup;
    Estado  edoDestino;

    //constructores pendientes   
}*/

public class Transicion {
    char simbInf;
    char simbSup;
    Estado edoDestino;

    // Transicion con un solo simbolo (o EPSILON)
    public Transicion(char simb, Estado destino) {
        this.simbInf = simb;
        this.simbSup = simb;
        this.edoDestino = destino;
    }

    // Transicion con rango de simbolos (ej. crearBasico('a','z'))
    public Transicion(char simbInf, char simbSup, Estado destino) {
        this.simbInf = simbInf;
        this.simbSup = simbSup;
        this.edoDestino = destino;
    }
}