import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class VentanaPrincipal extends JFrame {

    private GestorAFN gestor;

    private JPanel panelIzquierdo; 

    public VentanaPrincipal() {

        gestor = new GestorAFN();

        setTitle("Compilador");
        setSize(800, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout()); 

        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new FlowLayout(FlowLayout.LEFT)); 
        panelSuperior.setBackground(new Color(45, 45, 48)); 

        JButton btnThompson = new JButton("Thompson");
        panelSuperior.add(btnThompson);

        add(panelSuperior, BorderLayout.NORTH); 


        panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new GridLayout(7, 1, 5, 5)); 
        panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        panelIzquierdo.setVisible(false);

        JButton btnCrearBasico = new JButton("Crear Básico");
        JButton btnUnir = new JButton("Unir AFN");
        JButton btnConcatenar = new JButton("Concatenar");
        JButton btnCerraduraPos = new JButton("Cerradura Positiva");
        JButton btnCerraduraKleen = new JButton("Cerradura de Kleen");
        JButton btnOpcional = new JButton("Opcional");
        JButton btnVer = new JButton("Ver AFN");

        panelIzquierdo.add(btnCrearBasico);
        panelIzquierdo.add(btnUnir);
        panelIzquierdo.add(btnConcatenar);
        panelIzquierdo.add(btnCerraduraPos);
        panelIzquierdo.add(btnCerraduraKleen);
        panelIzquierdo.add(btnOpcional);
        panelIzquierdo.add(btnVer);

        add(panelIzquierdo, BorderLayout.WEST);


        JPanel panelCentral = new JPanel();
        panelCentral.setBackground(Color.LIGHT_GRAY); 
        add(panelCentral, BorderLayout.CENTER);


        //EVENTOS 
        btnThompson.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean estaVisible = panelIzquierdo.isVisible();
                panelIzquierdo.setVisible(!estaVisible);
            }
        });

        btnCrearBasico.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //simbolo inferior
                    String infStr = JOptionPane.showInputDialog(null, "Ingresa el símbolo inferior (ej. a):");
                    if (infStr == null || infStr.isEmpty()) return; 

                    //simbolo superior
                    String supStr = JOptionPane.showInputDialog(null, "Ingresa el símbolo superior (ej. z):");
                    if (supStr == null || supStr.isEmpty()) return;

                    //ID
                    String idStr = JOptionPane.showInputDialog(null, "Asigna un ID numérico a este AFN (ej. 1):");
                    if (idStr == null || idStr.isEmpty()) return;

                    char inf = infStr.charAt(0);
                    char sup = supStr.charAt(0);
                    int id = Integer.parseInt(idStr);

                    //Validacion
                    if (gestor.existeAFN(id)) {
                        JOptionPane.showMessageDialog(null, "Error: El ID " + id + " ya está en uso.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        //Crear el autómata y guardarlo
                        AFN nuevoAfn = new AFN();
                        nuevoAfn = nuevoAfn.crearBasico(inf, sup);
                        
                        gestor.agregarAFN(id, nuevoAfn);

                        //Exito
                        JOptionPane.showMessageDialog(null, "¡AFN creado exitosamente con el ID: " + id + "!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "El ID debe ser un número entero válido.", "Error de entrada", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // Cerradura positiva
        btnCerraduraPos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String idStr = JOptionPane.showInputDialog(null, "Ingresa el ID del AFN para aplicar Cerradura Positiva (+):");
                    if (idStr == null || idStr.isEmpty()) return;

                    int id = Integer.parseInt(idStr);

                    if (gestor.existeAFN(id)) {
                        AFN afn = gestor.obtenerAFN(id);
                        afn.cerraduraPos();
                        JOptionPane.showMessageDialog(null, "Cerradura Positiva aplicada al ID: " + id, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Error: El ID no existe.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "El ID debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Cerradura de kleen
        btnCerraduraKleen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String idStr = JOptionPane.showInputDialog(null, "Ingresa el ID del AFN para aplicar Cerradura de Kleene (*):");
                    if (idStr == null || idStr.isEmpty()) return;

                    int id = Integer.parseInt(idStr);

                    if (gestor.existeAFN(id)) {
                        AFN afn = gestor.obtenerAFN(id);
                        afn.cerraduraKleen();
                        JOptionPane.showMessageDialog(null, "Cerradura de Kleene aplicada al ID: " + id, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Error: El ID no existe.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "El ID debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Opcional
        btnOpcional.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String idStr = JOptionPane.showInputDialog(null, "Ingresa el ID del AFN para aplicar Opcional (?):");
                    if (idStr == null || idStr.isEmpty()) return;

                    int id = Integer.parseInt(idStr);

                    if (gestor.existeAFN(id)) {
                        AFN afn = gestor.obtenerAFN(id);
                        afn.opcional(); // Llama al método que agregamos anteriormente
                        JOptionPane.showMessageDialog(null, "Operación Opcional aplicada al ID: " + id, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Error: El ID no existe.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "El ID debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Unir AFN
        btnUnir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String id1Str = JOptionPane.showInputDialog(null, "Ingresa el ID del primer AFN:");
                    if (id1Str == null || id1Str.isEmpty()) return;

                    String id2Str = JOptionPane.showInputDialog(null, "Ingresa el ID del segundo AFN:");
                    if (id2Str == null || id2Str.isEmpty()) return;

                    int id1 = Integer.parseInt(id1Str);
                    int id2 = Integer.parseInt(id2Str);

                    // Validar que ambos existan
                    if (gestor.existeAFN(id1) && gestor.existeAFN(id2)) {
                        AFN afn1 = gestor.obtenerAFN(id1);
                        AFN afn2 = gestor.obtenerAFN(id2);

                        // Union
                        afn1.unirAFN(afn2);
                        
                        JOptionPane.showMessageDialog(null, "Unión exitosa. El resultado está en el ID: " + id1, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Error: Uno o ambos IDs no existen.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Los IDs deben ser números.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        //Concatenar
        btnConcatenar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String id1Str = JOptionPane.showInputDialog(null, "Ingresa el ID del primer AFN:");
                    if (id1Str == null || id1Str.isEmpty()) return;

                    String id2Str = JOptionPane.showInputDialog(null, "Ingresa el ID del segundo AFN:");
                    if (id2Str == null || id2Str.isEmpty()) return;

                    int id1 = Integer.parseInt(id1Str);
                    int id2 = Integer.parseInt(id2Str);

                    if (gestor.existeAFN(id1) && gestor.existeAFN(id2)) {
                        AFN afn1 = gestor.obtenerAFN(id1);
                        AFN afn2 = gestor.obtenerAFN(id2);

                        afn1.concatenar(afn2);
                        
                        JOptionPane.showMessageDialog(null, "Concatenación exitosa. El resultado está en el ID: " + id1, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Error: Uno o ambos IDs no existen.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Los IDs deben ser números.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Ver AFN
        btnVer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String idStr = JOptionPane.showInputDialog(null, "Ingresa el ID del AFN que deseas ver:");
                    if (idStr == null || idStr.isEmpty()) return;

                    int id = Integer.parseInt(idStr);

                    if (gestor.existeAFN(id)) {
                        // Busca automata
                        AFN afn = gestor.obtenerAFN(id);
                        
    
                        StringBuilder info = new StringBuilder();
                        info.append("--- DATOS DEL AFN (ID: ").append(id).append(") ---\n\n");
                        
                        // Alfabeto
                        info.append("Alfabeto: ").append(afn.alfabeto).append("\n");
                        
                        // Estado Inicial
                        info.append("Estado Inicial: ").append(afn.edoIni.idEdo).append("\n");
                        
                        // Estados de Aceptación
                        info.append("Estado(s) de Aceptación: ");
                        for (Estado edo : afn.edosAceptacion) {
                            info.append(edo.idEdo).append(" ");
                        }
                        info.append("\n");
                        
                        // Total de estados
                        info.append("Total de estados: ").append(afn.edosAFN.size()).append("\n");
                        
                        // Muestra
                        JOptionPane.showMessageDialog(null, info.toString(), "Información del AFN", JOptionPane.INFORMATION_MESSAGE);
                        
                    } else {
                        JOptionPane.showMessageDialog(null, "Error: El ID no existe.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "El ID debe ser un número entero.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
            
    }
}