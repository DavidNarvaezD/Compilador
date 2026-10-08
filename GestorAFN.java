import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GestorAFN {
   
    private HashMap<Integer, AFN> mapaAFN;

    public GestorAFN() {
        this.mapaAFN = new HashMap<>();
    }

    public void agregarAFN(int id, AFN automata) {
        mapaAFN.put(id, automata);
    }

    public AFN obtenerAFN(int id) {
        return mapaAFN.get(id);
    }

    public boolean existeAFN(int id) {
        return mapaAFN.containsKey(id);
    }

    /** Devuelve los IDs de todos los AFN registrados, ordenados ascendentemente. */
    public List<Integer> obtenerIds() {
        List<Integer> ids = new ArrayList<>(mapaAFN.keySet());
        Collections.sort(ids);
        return ids;
    }
}