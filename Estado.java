/*class Estado{
    int     idEdo;
    boolean    edoAcept;
    Conjunto <Transicion> Transiciones;
    static int contadorEdo=0;

    //constructores pendientes   
}*/

import java.util.HashSet;
import java.util.Set;

public class Estado {
    int idEdo;
    boolean edoAcept;
    Set<Transicion> transiciones;

    static int contadorEdo = 0;

    public Estado() {
        this.idEdo = contadorEdo++;
        this.edoAcept = false;
        this.transiciones = new HashSet<>();
    }
}