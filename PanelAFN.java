import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * Panel que dibuja gráficamente un AFN (Autómata Finito No determinista)
 * usando Graphics2D, con un estilo similar al de un diagrama de Thompson
 * hecho a mano: círculos simples para los estados, líneas rectas para las
 * transiciones "normales" (nivel -> nivel+1), y arcos curvos para las
 * transiciones especiales (regresos de cerradura positiva/Kleene, saltos
 * de la cerradura de Kleene/opcional).
 */
public class PanelAFN extends JPanel {

    private AFN afn;
    private Map<Estado, Point> posiciones;
    private Map<Estado, Integer> niveles;

    private static final int RADIO = 20;
    private static final int ESPACIO_X = 130;
    private static final int ESPACIO_Y = 90;
    private static final int MARGEN = 60;
    // Espacio extra reservado arriba/abajo de la fila de nodos para que
    // los arcos curvos (ε de regreso o de salto) no se corten en el borde
    private static final int ESPACIO_ARCOS = 110;

    public PanelAFN(AFN afn) {
        this.afn = afn;
        setBackground(Color.WHITE);
        calcularLayout();
    }

    /**
     * Calcula la posición (x,y) de cada estado usando BFS a partir de edoIni.
     * El "nivel" (profundidad BFS) determina la coordenada X.
     * Dentro de un mismo nivel, los estados se reparten verticalmente.
     */
    private void calcularLayout() {
        posiciones = new HashMap<>();
        niveles = new HashMap<>();
        List<List<Estado>> porNivel = new ArrayList<>();

        if (afn.edoIni == null) {
            setPreferredSize(new Dimension(400, 300));
            return;
        }

        Queue<Estado> cola = new LinkedList<>();
        cola.add(afn.edoIni);
        niveles.put(afn.edoIni, 0);
        porNivel.add(new ArrayList<>());
        porNivel.get(0).add(afn.edoIni);

        while (!cola.isEmpty()) {
            Estado actual = cola.poll();
            int nivelActual = niveles.get(actual);

            for (Transicion t : actual.transiciones) {
                Estado destino = t.edoDestino;
                if (!niveles.containsKey(destino)) {
                    int nuevoNivel = nivelActual + 1;
                    niveles.put(destino, nuevoNivel);
                    while (porNivel.size() <= nuevoNivel) {
                        porNivel.add(new ArrayList<>());
                    }
                    porNivel.get(nuevoNivel).add(destino);
                    cola.add(destino);
                }
            }
        }

        // Por seguridad: cualquier estado de edosAFN no alcanzado por BFS
        int nivelExtra = porNivel.size();
        boolean hayExtra = false;
        for (Estado e : afn.edosAFN) {
            if (!niveles.containsKey(e)) {
                if (!hayExtra) {
                    porNivel.add(new ArrayList<>());
                    hayExtra = true;
                }
                porNivel.get(nivelExtra).add(e);
                niveles.put(e, nivelExtra);
            }
        }

        // Forzar a que los estados de aceptación siempre queden en la última
        // columna (a la derecha), sin importar si el BFS los alcanzó antes
        // por un camino "más corto" (esto pasa en uniones/cerraduras, donde
        // un ε puede llevar directo al estado final saltándose estados
        // intermedios que en realidad deberían dibujarse antes).
        int maxNivelNoAceptacion = -1;
        for (Estado e : afn.edosAFN) {
            if (!afn.edosAceptacion.contains(e)) {
                maxNivelNoAceptacion = Math.max(maxNivelNoAceptacion, niveles.get(e));
            }
        }
        int nivelFinal = maxNivelNoAceptacion + 1;

        for (Estado e : afn.edosAceptacion) {
            Integer nivelActual = niveles.get(e);
            if (nivelActual != null && nivelActual == nivelFinal) continue;
            if (nivelActual != null) {
                porNivel.get(nivelActual).remove(e);
            }
            while (porNivel.size() <= nivelFinal) {
                porNivel.add(new ArrayList<>());
            }
            porNivel.get(nivelFinal).add(e);
            niveles.put(e, nivelFinal);
        }

        int maxPorNivel = 1;
        for (List<Estado> lista : porNivel) {
            maxPorNivel = Math.max(maxPorNivel, lista.size());
        }

        // Predecesores "hacia adelante": solo cuenta una transición
        // origen->destino si destino queda en un nivel posterior al de origen
        // (mismo criterio que usa paintComponent para decidir línea recta vs.
        // arco). Las transiciones de regreso (cerradura +/*) se ignoran aquí,
        // porque esas se dibujan como arcos y no deben mover la altura base.
        Map<Estado, List<Estado>> predecesoresFwd = new HashMap<>();
        for (Estado origen : afn.edosAFN) {
            Integer nivelO = niveles.get(origen);
            if (nivelO == null) continue;
            for (Transicion t : origen.transiciones) {
                Estado destino = t.edoDestino;
                Integer nivelD = niveles.get(destino);
                if (nivelD != null && nivelD > nivelO) {
                    predecesoresFwd.computeIfAbsent(destino, k -> new ArrayList<>()).add(origen);
                }
            }
        }

        // Altura de cada estado, en una sola pasada de izquierda a derecha:
        // cada estado se posiciona en el promedio de sus predecesores. La
        // clave para que el estado inicial (y cualquier nodo donde confluyen
        // varias ramas) quede bien centrado SIN necesidad de iterar es cómo
        // se resuelven los encimamientos: cuando dos o más estados de un
        // mismo nivel comparten la misma altura ideal (p. ej. las dos ramas
        // de una unión, que parten del mismo estado), se reparten de forma
        // SIMÉTRICA alrededor de esa altura ideal (mitad arriba, mitad abajo)
        // en vez de empujar solo hacia abajo. Así, el promedio de las ramas
        // coincide exactamente con la altura del estado del que provienen,
        // y no hace falta ningún ajuste posterior "hacia atrás".
        Map<Estado, Double> altura = new HashMap<>();
        List<Estado> nivel0 = porNivel.get(0);
        int n0 = nivel0.size();
        for (int i = 0; i < n0; i++) {
            altura.put(nivel0.get(i), (i - (n0 - 1) / 2.0) * ESPACIO_Y);
        }

        for (int nivel = 1; nivel < porNivel.size(); nivel++) {
            asignarAltura(porNivel.get(nivel), predecesoresFwd, altura);
        }

        // Se normaliza para que la altura mínima sea 0 (el reparto simétrico
        // deja valores negativos por encima del primer estado).
        double minAltura = Double.MAX_VALUE;
        double maxAltura = -Double.MAX_VALUE;
        for (double v : altura.values()) {
            minAltura = Math.min(minAltura, v);
            maxAltura = Math.max(maxAltura, v);
        }
        if (minAltura == Double.MAX_VALUE) {
            minAltura = 0;
            maxAltura = 0;
        }

        for (int nivel = 0; nivel < porNivel.size(); nivel++) {
            for (Estado e : porNivel.get(nivel)) {
                int x = MARGEN + 40 + nivel * ESPACIO_X;
                int y = ESPACIO_ARCOS + (int) Math.round(altura.get(e) - minAltura) + ESPACIO_Y / 2;
                posiciones.put(e, new Point(x, y));
            }
        }

        // Se reserva espacio extra arriba y abajo para los arcos curvos
        int ancho = MARGEN * 2 + 40 + porNivel.size() * ESPACIO_X;
        int alturaContenido = (int) Math.round(maxAltura - minAltura) + ESPACIO_Y;
        int alto = ESPACIO_ARCOS * 2 + Math.max(alturaContenido, maxPorNivel * ESPACIO_Y);
        setPreferredSize(new Dimension(Math.max(ancho, 400), Math.max(alto, 300)));
    }

    /**
     * Calcula y asigna la altura de cada estado de un nivel, a partir del
     * promedio de sus predecesores ya posicionados (niveles anteriores).
     * Si varios estados del nivel quedan encimados (misma altura ideal, o
     * muy cerca) se reparten de forma simétrica alrededor de esa altura,
     * en vez de empujarlos solo hacia abajo — así el promedio del grupo ya
     * resuelto sigue siendo igual al promedio de las alturas ideales, y el
     * estado (o estados) del que provienen queda automáticamente centrado
     * respecto a ellos sin necesidad de ningún ajuste posterior.
     */
    private void asignarAltura(List<Estado> lista, Map<Estado, List<Estado>> predecesoresFwd,
                                Map<Estado, Double> altura) {
        List<Double> ideal = new ArrayList<>();
        for (Estado e : lista) {
            List<Estado> preds = predecesoresFwd.getOrDefault(e, Collections.emptyList());
            double suma = 0;
            int cuenta = 0;
            for (Estado p : preds) {
                Double alturaP = altura.get(p);
                if (alturaP != null) {
                    suma += alturaP;
                    cuenta++;
                }
            }
            ideal.add(cuenta > 0 ? suma / cuenta : 0.0);
        }

        List<Integer> orden = new ArrayList<>();
        for (int i = 0; i < lista.size(); i++) orden.add(i);
        orden.sort((a, b) -> Double.compare(ideal.get(a), ideal.get(b)));

        // Primero se resuelven los encimamientos empujando hacia abajo
        // (como antes) para obtener la separación mínima entre estados...
        List<Double> posMin = new ArrayList<>();
        double previa = Double.NEGATIVE_INFINITY;
        for (int idx : orden) {
            double y = ideal.get(idx);
            if (previa != Double.NEGATIVE_INFINITY && y < previa + ESPACIO_Y) {
                y = previa + ESPACIO_Y;
            }
            posMin.add(y);
            previa = y;
        }

        // ...y luego se desplaza el grupo completo para que su propio
        // promedio vuelva a coincidir con el promedio de las alturas ideales
        // originales (reparto simétrico en vez de "todo empujado hacia
        // abajo").
        double promedioIdeal = ideal.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double promedioPosMin = posMin.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double desplazamiento = promedioIdeal - promedioPosMin;

        for (int i = 0; i < orden.size(); i++) {
            altura.put(lista.get(orden.get(i)), posMin.get(i) + desplazamiento);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));

        if (afn.edoIni == null) {
            g2.drawString("AFN vacío", 20, 30);
            return;
        }

        // Agrupar transiciones por par (origen -> destino) para no encimar
        // flechas cuando hay varias transiciones entre los mismos dos estados.
        Map<String, List<String>> etiquetasPorPar = new LinkedHashMap<>();
        Map<String, Estado[]> paresEstados = new HashMap<>();

        for (Estado origen : afn.edosAFN) {
            for (Transicion t : origen.transiciones) {
                String etiqueta = etiquetaTransicion(t);
                String clave = origen.idEdo + "->" + t.edoDestino.idEdo;
                etiquetasPorPar.computeIfAbsent(clave, k -> new ArrayList<>()).add(etiqueta);
                paresEstados.put(clave, new Estado[]{origen, t.edoDestino});
            }
        }

        // 1) Dibujar transiciones primero (quedan detrás de los círculos)
        for (String clave : etiquetasPorPar.keySet()) {
            Estado[] par = paresEstados.get(clave);
            Estado origen = par[0];
            Estado destino = par[1];
            String etiqueta = String.join(", ", etiquetasPorPar.get(clave));

            int nivelOrigen = niveles.getOrDefault(origen, 0);
            int nivelDestino = niveles.getOrDefault(destino, 0);

            if (origen == destino) {
                // Loop verdadero (mismo estado) - poco común, pero por si acaso
                dibujarAutotransicion(g2, posiciones.get(origen), etiqueta);
            } else if (nivelDestino > nivelOrigen) {
                // Cualquier avance hacia adelante (al siguiente nivel o
                // saltando varios, como el ε directo al estado de aceptación
                // forzado a la última columna): siempre línea recta
                dibujarFlecha(g2, posiciones.get(origen), posiciones.get(destino), etiqueta);
            } else {
                // Solo retrocede (regresa a un nivel igual o anterior): es un
                // verdadero "regreso" de cerradura positiva/Kleene, se dibuja
                // como arco curvo arriba
                dibujarArco(g2, posiciones.get(origen), posiciones.get(destino), etiqueta, true);
            }
        }

        // 2) Flecha de entrada al estado inicial (convención estándar)
        Point pIni = posiciones.get(afn.edoIni);
        if (pIni != null) {
            g2.setColor(Color.BLACK);
            int xIni = pIni.x - RADIO - 30;
            g2.drawLine(xIni, pIni.y, pIni.x - RADIO, pIni.y);
            dibujarPuntaFlecha(g2, new Point(xIni, pIni.y), new Point(pIni.x - RADIO, pIni.y));
        }

        // 3) Dibujar los estados (círculos) encima de las flechas
        for (Estado e : afn.edosAFN) {
            Point p = posiciones.get(e);
            if (p == null) continue;

            g2.setColor(Color.WHITE);
            g2.fillOval(p.x - RADIO, p.y - RADIO, RADIO * 2, RADIO * 2);
            g2.setColor(Color.BLACK);
            g2.drawOval(p.x - RADIO, p.y - RADIO, RADIO * 2, RADIO * 2);

            // Doble círculo si es estado de aceptación
            if (e.edoAcept) {
                g2.drawOval(p.x - RADIO + 4, p.y - RADIO + 4, RADIO * 2 - 8, RADIO * 2 - 8);
            }

            String texto = String.valueOf(e.idEdo);
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(texto);
            g2.drawString(texto, p.x - textW / 2, p.y + fm.getAscent() / 2 - 2);
        }
    }

    /** Convierte una Transicion en su etiqueta visual: "ε", "a" o "a-z" */
    private String etiquetaTransicion(Transicion t) {
        if (t.simbInf == AFN.EPSILON) {
            return "\u03B5"; // ε
        } else if (t.simbInf == t.simbSup) {
            return String.valueOf(t.simbInf);
        } else {
            return t.simbInf + "-" + t.simbSup;
        }
    }

    /** Línea recta entre dos estados (transición "normal" nivel -> nivel+1) */
    private void dibujarFlecha(Graphics2D g2, Point origen, Point destino, String etiqueta) {
        if (origen == null || destino == null) return;

        double dx = destino.x - origen.x;
        double dy = destino.y - origen.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) return;

        double ux = dx / dist;
        double uy = dy / dist;

        Point p1 = new Point((int) (origen.x + ux * RADIO), (int) (origen.y + uy * RADIO));
        Point p2 = new Point((int) (destino.x - ux * RADIO), (int) (destino.y - uy * RADIO));

        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        dibujarPuntaFlecha(g2, p1, p2);

        int mx = (p1.x + p2.x) / 2;
        int my = (p1.y + p2.y) / 2;
        g2.setColor(Color.BLUE);
        g2.drawString(etiqueta, mx + 4, my - 4);
    }

    /**
     * Arco curvo entre dos estados, usado para las transiciones "especiales"
     * (regresos o saltos de nivel), tal como se ven en los diagramas de
     * cerradura positiva, Kleene y opcional.
     *
     * @param arriba true = el arco se curva hacia arriba (regreso),
     *               false = el arco se curva hacia abajo (salto hacia adelante)
     */
    private void dibujarArco(Graphics2D g2, Point origen, Point destino, String etiqueta, boolean arriba) {
        if (origen == null || destino == null) return;

        double dx = destino.x - origen.x;
        double dy = destino.y - origen.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) return;

        // Punto de control: se calcula ANTES de fijar los puntos de anclaje,
        // porque el arco debe salir/entrar a cada círculo apuntando hacia
        // este punto (no en línea recta hacia el otro estado), o de lo
        // contrario el arco cruza otras transiciones cerca del nodo.
        double mx = (origen.x + destino.x) / 2.0;
        double my = (origen.y + destino.y) / 2.0;

        // Curvatura: proporcional a la distancia total entre los estados,
        // con un mínimo más generoso para que el arco siempre se separe
        // claramente de las líneas rectas vecinas.
        double curvatura = Math.min(110, Math.max(50, dist * 0.45));
        double cx = mx;
        double cy = arriba ? my - curvatura : my + curvatura;

        // Punto de salida en el círculo de origen: dirección hacia el punto de control
        double dxo = cx - origen.x;
        double dyo = cy - origen.y;
        double disto = Math.sqrt(dxo * dxo + dyo * dyo);
        Point p1 = new Point((int) (origen.x + dxo / disto * RADIO), (int) (origen.y + dyo / disto * RADIO));

        // Punto de entrada en el círculo de destino: dirección hacia el punto de control
        double dxd = cx - destino.x;
        double dyd = cy - destino.y;
        double distd = Math.sqrt(dxd * dxd + dyd * dyd);
        Point p2 = new Point((int) (destino.x + dxd / distd * RADIO), (int) (destino.y + dyd / distd * RADIO));

        QuadCurve2D curva = new QuadCurve2D.Double(p1.x, p1.y, cx, cy, p2.x, p2.y);

        g2.setColor(Color.DARK_GRAY);
        g2.draw(curva);

        // Dirección aproximada de la curva justo antes de llegar al destino
        // (tangente de la cuadrática en t=1 es proporcional a destino-control),
        // para orientar bien la punta de flecha
        Point puntoControl = new Point((int) cx, (int) cy);
        dibujarPuntaFlecha(g2, puntoControl, p2);

        g2.setColor(Color.BLUE);
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(etiqueta);
        int textY = arriba ? (int) cy - 6 : (int) cy + fm.getAscent() + 4;
        g2.drawString(etiqueta, (int) cx - textW / 2, textY);
    }

    /** Loop verdadero: un estado con transición hacia sí mismo */
    private void dibujarAutotransicion(Graphics2D g2, Point p, String etiqueta) {
        if (p == null) return;
        int loopSize = 34;
        java.awt.geom.Arc2D arc = new java.awt.geom.Arc2D.Double(
                p.x - loopSize / 2.0, p.y - RADIO - loopSize, loopSize, loopSize, 0, 360, java.awt.geom.Arc2D.OPEN);
        g2.setColor(Color.DARK_GRAY);
        g2.draw(arc);
        g2.setColor(Color.BLUE);
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(etiqueta);
        g2.drawString(etiqueta, p.x - textW / 2, p.y - RADIO - loopSize - 4);
    }

    private void dibujarPuntaFlecha(Graphics2D g2, Point desde, Point hasta) {
        double angulo = Math.atan2(hasta.y - desde.y, hasta.x - desde.x);
        int longitudPunta = 10;
        double anguloPunta = Math.PI / 7;

        int x1 = (int) (hasta.x - longitudPunta * Math.cos(angulo - anguloPunta));
        int y1 = (int) (hasta.y - longitudPunta * Math.sin(angulo - anguloPunta));
        int x2 = (int) (hasta.x - longitudPunta * Math.cos(angulo + anguloPunta));
        int y2 = (int) (hasta.y - longitudPunta * Math.sin(angulo + anguloPunta));

        Polygon punta = new Polygon();
        punta.addPoint(hasta.x, hasta.y);
        punta.addPoint(x1, y1);
        punta.addPoint(x2, y2);

        g2.setColor(Color.DARK_GRAY);
        g2.fillPolygon(punta);
    }
}