package com.redes.escaner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EscanerRedFrame extends JFrame {

    private JTextField txtIpInicio;
    private JTextField txtIpFin;
    private JTextField txtTimeout;
    private JTextField txtReintentos;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    private JLabel lblEquiposActivos;
    private JLabel lblPorcentaje;

    private JProgressBar barraProgreso;

    private JButton btnIniciar;
    private JButton btnDetener;
    private JButton btnLimpiar;
    private JButton btnGuardar;
    private JButton btnMostrarActivos;

    private SwingWorker<List<Dispositivo>, Dispositivo> worker;

    private boolean mostrarSoloActivos = false;

    private final List<Dispositivo> dispositivos =
            new ArrayList<>();

    public EscanerRedFrame() {
        initComponents();
    }

    private void initComponents() {

        setTitle("Escáner de Red");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(700, 500);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 5));
        panelPrincipal.setBorder(
                new EmptyBorder(5, 5, 5, 5)
        );

        // =====================================================
        // PANEL SUPERIOR
        // =====================================================

        JPanel panelDatos = new JPanel(
                new GridBagLayout()
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // IP inicio
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        panelDatos.add(
                new JLabel("IP de inicio:"),
                gbc
        );

        txtIpInicio = new JTextField("10.160.7.223");

        gbc.gridx = 1;
        gbc.weightx = 1;

        panelDatos.add(txtIpInicio, gbc);

        // IP fin
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;

        panelDatos.add(
                new JLabel("IP de fin:"),
                gbc
        );

        txtIpFin = new JTextField("10.160.7.223");

        gbc.gridx = 1;
        gbc.weightx = 1;

        panelDatos.add(txtIpFin, gbc);

        // Timeout
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;

        panelDatos.add(
                new JLabel("Tiempo de espera (ms):"),
                gbc
        );

        txtTimeout = new JTextField("1000");

        gbc.gridx = 1;
        gbc.weightx = 1;

        panelDatos.add(txtTimeout, gbc);

        // Reintentos
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;

        panelDatos.add(
                new JLabel("Número de reintentos:"),
                gbc
        );

        txtReintentos = new JTextField("0");

        gbc.gridx = 1;
        gbc.weightx = 1;

        panelDatos.add(txtReintentos, gbc);

        panelPrincipal.add(
                panelDatos,
                BorderLayout.NORTH
        );

        // =====================================================
        // TABLA
        // =====================================================

        modeloTabla = new DefaultTableModel(
                new Object[]{
                        "IP",
                        "Nombre equipo",
                        "Activo",
                        "Tiempo (ms)"
                },
                0
        ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        tabla = new JTable(modeloTabla);

        tabla.setRowHeight(22);
        tabla.setAutoCreateRowSorter(true);

        JScrollPane scrollTabla =
                new JScrollPane(tabla);

        panelPrincipal.add(
                scrollTabla,
                BorderLayout.CENTER
        );

        // =====================================================
        // PANEL INFERIOR
        // =====================================================

        JPanel panelInferior = new JPanel(
                new BorderLayout(3, 3)
        );

        // -----------------------------------------------------
        // Contador
        // -----------------------------------------------------

        lblEquiposActivos =
                new JLabel("Equipos activos: 0");

        panelInferior.add(
                lblEquiposActivos,
                BorderLayout.NORTH
        );

        // -----------------------------------------------------
        // Progreso
        // -----------------------------------------------------

        JPanel panelProgreso =
                new JPanel(new BorderLayout());

        barraProgreso =
                new JProgressBar(0, 100);

        barraProgreso.setStringPainted(false);

        panelProgreso.add(
                barraProgreso,
                BorderLayout.CENTER
        );

        lblPorcentaje =
                new JLabel(
                        "0 %",
                        SwingConstants.CENTER
                );

        panelProgreso.add(
                lblPorcentaje,
                BorderLayout.EAST
        );

        panelInferior.add(
                panelProgreso,
                BorderLayout.CENTER
        );

        // -----------------------------------------------------
        // Botones
        // -----------------------------------------------------

        JPanel panelBotones =
                new JPanel(new FlowLayout(
                        FlowLayout.CENTER,
                        5,
                        5
                ));

        btnIniciar =
                new JButton("Iniciar escaneo");

        btnDetener =
                new JButton("Detener escaneo");

        btnLimpiar =
                new JButton("Limpiar");

        btnGuardar =
                new JButton("Guardar resultados");

        btnMostrarActivos =
                new JButton("Mostrar solo activos");

        btnDetener.setEnabled(false);

        panelBotones.add(btnIniciar);
        panelBotones.add(btnDetener);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnMostrarActivos);

        panelInferior.add(
                panelBotones,
                BorderLayout.SOUTH
        );

        panelPrincipal.add(
                panelInferior,
                BorderLayout.SOUTH
        );

        setContentPane(panelPrincipal);

        // =====================================================
        // EVENTOS
        // =====================================================

        btnIniciar.addActionListener(
                e -> iniciarEscaneo()
        );

        btnDetener.addActionListener(
                e -> detenerEscaneo()
        );

        btnLimpiar.addActionListener(
                e -> limpiar()
        );

        btnGuardar.addActionListener(
                e -> guardarResultados()
        );

        btnMostrarActivos.addActionListener(
                e -> mostrarActivos()
        );
    }

    // =========================================================
    // INICIAR ESCANEO
    // =========================================================

    private void iniciarEscaneo() {

        if (worker != null && !worker.isDone()) {
            return;
        }

        String ipInicio = txtIpInicio
                .getText()
                .trim();

        String ipFin = txtIpFin
                .getText()
                .trim();

        int timeoutMs;
        int reintentos;

        try {

            timeoutMs = Integer.parseInt(
                    txtTimeout.getText().trim()
            );

            reintentos = Integer.parseInt(
                    txtReintentos.getText().trim()
            );

        } catch (NumberFormatException e) {

            mostrarError(
                    "El timeout y los reintentos deben ser números."
            );

            return;
        }

        if (!EscanerRedService.esIpValida(ipInicio)) {

            mostrarError(
                    "La IP de inicio no es válida."
            );

            return;
        }

        if (!EscanerRedService.esIpValida(ipFin)) {

            mostrarError(
                    "La IP de fin no es válida."
            );

            return;
        }

        if (timeoutMs <= 0) {

            mostrarError(
                    "El tiempo de espera debe ser mayor que 0."
            );

            return;
        }

        if (reintentos < 0) {

            mostrarError(
                    "El número de reintentos no puede ser negativo."
            );

            return;
        }

        long inicio =
                EscanerRedService.ipToLong(ipInicio);

        long fin =
                EscanerRedService.ipToLong(ipFin);

        if (inicio > fin) {

            mostrarError(
                    "La IP de inicio debe ser menor o igual "
                    + "que la IP de fin."
            );

            return;
        }

        /*
         * Evitamos accidentalmente escanear rangos enormes.
         */
        long cantidad = fin - inicio + 1;

        if (cantidad > 65536) {

            mostrarError(
                    "El rango es demasiado grande. "
                    + "Máximo permitido: 65536 IPs."
            );

            return;
        }

        // -----------------------------------------------------
        // Preparar interfaz
        // -----------------------------------------------------

        dispositivos.clear();

        modeloTabla.setRowCount(0);

        mostrarSoloActivos = false;

        btnMostrarActivos.setText(
                "Mostrar solo activos"
        );

        lblEquiposActivos.setText(
                "Equipos activos: 0"
        );

        barraProgreso.setValue(0);
        lblPorcentaje.setText("0 %");

        btnIniciar.setEnabled(false);
        btnDetener.setEnabled(true);
        btnLimpiar.setEnabled(false);
        btnGuardar.setEnabled(false);

        final int total =
                (int) cantidad;

        // -----------------------------------------------------
        // Worker
        // -----------------------------------------------------

        worker = new SwingWorker<>() {

            private int activos = 0;

            @Override
            protected List<Dispositivo> doInBackground()
                    throws Exception {

                List<Dispositivo> resultados =
                        new ArrayList<>();

                for (int i = 0; i < total; i++) {

                    if (isCancelled()) {
                        break;
                    }

                    long numeroIp =
                            inicio + i;

                    String ip =
                            EscanerRedService.longToIp(
                                    numeroIp
                            );

                    Dispositivo dispositivo =
                            EscanerRedService.escanearIp(
                                    ip,
                                    timeoutMs,
                                    reintentos
                            );

                    resultados.add(dispositivo);

                    if (dispositivo.isActivo()) {
                        activos++;
                    }

                    int progreso =
                            (int) (((i + 1L) * 100)
                                    / total);

                    setProgress(progreso);

                    publish(dispositivo);
                }

                return resultados;
            }

            @Override
            protected void process(
                    List<Dispositivo> chunks) {

                for (Dispositivo d : chunks) {

                    dispositivos.add(d);

                    agregarFila(d);
                }

                lblEquiposActivos.setText(
                        "Equipos activos: " + activos
                );

                int progreso = getProgress();

                barraProgreso.setValue(progreso);

                lblPorcentaje.setText(
                        progreso + " %"
                );
            }

            @Override
            protected void done() {

                try {

                    if (!isCancelled()) {

                        get();

                        barraProgreso.setValue(100);
                        lblPorcentaje.setText("100 %");
                    }

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                } catch (ExecutionException e) {

                    mostrarError(
                            "Error durante el escaneo: "
                            + e.getCause()
                    );

                } finally {

                    btnIniciar.setEnabled(true);
                    btnDetener.setEnabled(false);
                    btnLimpiar.setEnabled(true);
                    btnGuardar.setEnabled(
                            !dispositivos.isEmpty()
                    );

                    worker = null;
                }
            }
        };

        worker.addPropertyChangeListener(evt -> {

            if ("progress".equals(evt.getPropertyName())) {

                int progreso =
                        (Integer) evt.getNewValue();

                barraProgreso.setValue(progreso);

                lblPorcentaje.setText(
                        progreso + " %"
                );
            }
        });

        worker.execute();
    }

    // =========================================================
    // AGREGAR FILA
    // =========================================================

    private void agregarFila(Dispositivo d) {

        if (mostrarSoloActivos
                && !d.isActivo()) {
            return;
        }

        modeloTabla.addRow(
                new Object[]{
                        d.getIp(),
                        d.getNombre(),
                        d.isActivo() ? "Sí" : "No",
                        d.getTiempoMs()
                }
        );
    }

    // =========================================================
    // DETENER
    // =========================================================

    private void detenerEscaneo() {

        if (worker != null
                && !worker.isDone()) {

            worker.cancel(true);

            btnDetener.setEnabled(false);
            btnIniciar.setEnabled(true);
            btnLimpiar.setEnabled(true);

            lblPorcentaje.setText(
                    barraProgreso.getValue() + " %"
            );
        }
    }

    // =========================================================
    // LIMPIAR
    // =========================================================

    private void limpiar() {

        if (worker != null
                && !worker.isDone()) {

            return;
        }

        dispositivos.clear();

        modeloTabla.setRowCount(0);

        lblEquiposActivos.setText(
                "Equipos activos: 0"
        );

        barraProgreso.setValue(0);

        lblPorcentaje.setText("0 %");

        mostrarSoloActivos = false;

        btnMostrarActivos.setText(
                "Mostrar solo activos"
        );

        btnGuardar.setEnabled(false);
    }

    // =========================================================
    // MOSTRAR ACTIVOS
    // =========================================================

    private void mostrarActivos() {

        mostrarSoloActivos =
                !mostrarSoloActivos;

        modeloTabla.setRowCount(0);

        for (Dispositivo d : dispositivos) {

            if (!mostrarSoloActivos
                    || d.isActivo()) {

                agregarFila(d);
            }
        }

        if (mostrarSoloActivos) {

            btnMostrarActivos.setText(
                    "Mostrar todos"
            );

        } else {

            btnMostrarActivos.setText(
                    "Mostrar solo activos"
            );
        }
    }

    // =========================================================
    // GUARDAR
    // =========================================================

    private void guardarResultados() {

        if (dispositivos.isEmpty()) {

            mostrarError(
                    "No hay resultados para guardar."
            );

            return;
        }

        JFileChooser chooser =
                new JFileChooser();

        chooser.setDialogTitle(
                "Guardar resultados"
        );

        chooser.setSelectedFile(
                new java.io.File(
                        "resultados_escaner.csv"
                )
        );

        int resultado =
                chooser.showSaveDialog(this);

        if (resultado
                != JFileChooser.APPROVE_OPTION) {

            return;
        }

        java.io.File archivo =
                chooser.getSelectedFile();

        try (
                java.io.PrintWriter writer =
                        new java.io.PrintWriter(
                                new java.io.OutputStreamWriter(
                                        new java.io.FileOutputStream(
                                                archivo
                                        ),
                                        java.nio.charset.StandardCharsets.UTF_8
                                )
                        )
        ) {

            writer.println(
                    "IP,Nombre equipo,Activo,Tiempo (ms)"
            );

            for (Dispositivo d : dispositivos) {

                String nombre =
                        d.getNombre()
                                .replace("\"", "\"\"");

                writer.println(
                        "\"" + d.getIp() + "\","
                        + "\"" + nombre + "\","
                        + "\"" + d.isActivo() + "\","
                        + d.getTiempoMs()
                );
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Resultados guardados correctamente.",
                    "Guardar resultados",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {

            mostrarError(
                    "No se pudieron guardar los resultados:\n"
                    + e.getMessage()
            );
        }
    }

    // =========================================================
    // ERROR
    // =========================================================

    private void mostrarError(String mensaje) {

        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // =========================================================
    // MAIN OPCIONAL
    // =========================================================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            EscanerRedFrame frame =
                    new EscanerRedFrame();

            frame.setVisible(true);
        });
    }
}
