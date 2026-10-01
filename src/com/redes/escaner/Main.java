package com.redes.escaner;

import javax.swing.SwingUtilities;

public class Main {

    public Main() {
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            EscanerRedFrame frame =
                    new EscanerRedFrame();

            frame.setVisible(true);
        });
    }
}
