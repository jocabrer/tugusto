package com.example;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Estado de la Conexión a la BD");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        JLabel statusLabel = new JLabel("Estado de la conexión: Desconectado");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton connectButton = new JButton("Conectar");
        connectButton.addActionListener(e -> {
            DatabaseConnection dbConnection = new DatabaseConnection();
            try (Connection connection = dbConnection.connect()) {
                statusLabel.setText("Estado de la conexión: Conectado");
                // Ejecuto query simple para probar conectividad
                connection.createStatement().executeQuery("SELECT 1 FROM DUAL");
            } catch (SQLException ex) {
                statusLabel.setText("Estado de la conexión: Error");
                JOptionPane.showMessageDialog(frame, "No se puede conectar con el servidor Oracle.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(statusLabel, BorderLayout.CENTER);
        frame.add(connectButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }
}