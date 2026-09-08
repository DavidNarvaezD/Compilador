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
    
    }
}