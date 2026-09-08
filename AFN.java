import java.util.HashSet;
import java.util.Set;

public class AFN {

    // Representa una transicion "vacia" (epsilon)
    public static final char EPSILON = '\0';

    Set<Character> alfabeto;
    Estado edoIni;
    Set<Estado> edosAceptacion;
    Set<Estado> edosAFN;

    public AFN() {
        this.alfabeto = new HashSet<>();
        this.edosAceptacion = new HashSet<>();
        this.edosAFN = new HashSet<>();
    }

    // 1) Crea el AFN basico para un rango de simbolos [simb1, simb2]
    public AFN crearBasico(char simb1, char simb2) {
        AFN f = new AFN();
        Estado e1, e2;
        Transicion t;

        e1 = new Estado();
        e2 = new Estado();
        t = new Transicion(simb1, simb2, e2);

        e1.transiciones.add(t);
        e2.edoAcept = true;

        f.edoIni = e1;
        f.edosAceptacion.add(e2);
        f.edosAFN.add(e1);
        f.edosAFN.add(e2);

        for (char a = simb1; a <= simb2; a++) {
            f.alfabeto.add(a);
        }

        return f;
    }

    // 2) Concatenacion: this seguido de f2  (this . f2)
    public AFN concatenar(AFN f2) {
        for (Estado e : this.edosAceptacion) {
            for (Transicion t : f2.edoIni.transiciones) {
                e.transiciones.add(t);
            }
            e.edoAcept = false;
        }

        this.edosAFN.addAll(f2.edosAFN);
        this.edosAFN.remove(f2.edoIni);
        this.edosAceptacion.clear();
        this.edosAceptacion.addAll(f2.edosAceptacion);
        this.alfabeto.addAll(f2.alfabeto);

        f2 = null;
        return this;
    }

    // 3) Union: this | f2
    public AFN unirAFN(AFN f2) {
        Estado e1 = new Estado();
        Estado e2 = new Estado();

        e1.transiciones.add(new Transicion(EPSILON, this.edoIni));
        e1.transiciones.add(new Transicion(EPSILON, f2.edoIni));

        for (Estado edo : this.edosAceptacion) {
            edo.transiciones.add(new Transicion(EPSILON, e2));
            edo.edoAcept = false;
        }

        for (Estado edo : f2.edosAceptacion) {
            edo.transiciones.add(new Transicion(EPSILON, e2));
            edo.edoAcept = false;
        }

        e2.edoAcept = true;

        this.edosAFN.addAll(f2.edosAFN);
        this.edosAFN.add(e1);
        this.edosAFN.add(e2);
        this.edosAceptacion.clear();
        this.edosAceptacion.add(e2);
        this.edoIni = e1;
        this.alfabeto.addAll(f2.alfabeto);

        f2 = null;
        return this;
    }

    // 4) Cerradura positiva: this+
    public AFN cerraduraPos() {
        Estado e1 = new Estado();
        Estado e2 = new Estado();
        e2.edoAcept = true;

        e1.transiciones.add(new Transicion(EPSILON, this.edoIni));

        for (Estado e : this.edosAceptacion) {
            e.transiciones.add(new Transicion(EPSILON, e2));
            e.transiciones.add(new Transicion(EPSILON, this.edoIni));
            e.edoAcept = false;
        }

        this.edosAFN.add(e1);
        this.edosAFN.add(e2);
        this.edoIni = e1;
        this.edosAceptacion.clear();
        this.edosAceptacion.add(e2);

        return this;
    }

    // 5) Cerradura de Kleene: this*
    public AFN cerraduraKleen() {
        this.cerraduraPos();

        for (Estado e : this.edosAceptacion) {
            this.edoIni.transiciones.add(new Transicion(EPSILON, e));
        }

        return this;
    }

    public AFN opcional() {
    Estado e1 = new Estado();
    Estado e2 = new Estado();
    e2.edoAcept = true;
    e1.transiciones.add(new Transicion(EPSILON, this.edoIni));

    // Transiciones de los antiguos estados de aceptación al nuevo estado final
    for (Estado e : this.edosAceptacion) {
        e.transiciones.add(new Transicion(EPSILON, e2));
        e.edoAcept = false;
    }

    // Transición directa 
    e1.transiciones.add(new Transicion(EPSILON, e2));

    // Actualización de los conjuntos del autómata actual
    this.edosAFN.add(e1);
    this.edosAFN.add(e2);
    this.edoIni = e1;
    this.edosAceptacion.clear();
    this.edosAceptacion.add(e2);

    return this;
}
}