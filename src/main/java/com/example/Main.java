package com.example;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Panel de Control de Pedidos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Crear componentes
        JLabel statusLabel = new JLabel("Estado de la conexión: Desconectado");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton listOrdersButton = new JButton("Listar Pedidos");
        JButton showDetailsButton = new JButton("Ver Detalles");
        JButton addSaleButton = new JButton("Agregar Venta");

        // Hacer los botones más pequeños
        listOrdersButton.setPreferredSize(new java.awt.Dimension(100, 30));
        showDetailsButton.setPreferredSize(new java.awt.Dimension(100, 30));
        addSaleButton.setPreferredSize(new java.awt.Dimension(100, 30));

        // Crear tabla para mostrar pedidos
        String[] columnNames = {"Valor Pedido", "Numero Boleta", "Motoboy", "Hora Entrega"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Ninguna celda es editable
            }
        };
        JTable table = new JTable(tableModel);

        // Panel para botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));
        buttonPanel.add(listOrdersButton);
        buttonPanel.add(showDetailsButton);
        buttonPanel.add(addSaleButton);

        // Agregar componentes al panel principal
        panel.add(statusLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(new JScrollPane(table), BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);

        // Conectar a la base de datos al iniciar la aplicación
        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection connection = dbConnection.connect()) {
            statusLabel.setText("Estado de la conexión: Conectado");
            // Ejecuto query simple para probar conectividad
            connection.createStatement().executeQuery("SELECT 1 FROM DUAL");
        } catch (SQLException ex) {
            statusLabel.setText("Estado de la conexión: Error");
            JOptionPane.showMessageDialog(frame, "No se puede conectar con el servidor Oracle.\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Acción del botón Listar Pedidos
        listOrdersButton.addActionListener(e -> {
            // Lógica para listar pedidos
            List<String[]> orders = listOrders();
            tableModel.setRowCount(0); // Limpiar tabla
            for (String[] order : orders) {
                tableModel.addRow(order);
            }
        });

        // Acción del botón Ver Detalles
        showDetailsButton.addActionListener(e -> {
            showOrderDetails(frame);
        });

        // Acción del botón Agregar Venta
        addSaleButton.addActionListener(e -> {
            // Lógica para agregar una nueva venta
            addNewSale();
        });
    }

    private static List<String[]> listOrders() {
        List<String[]> orders = new ArrayList<>();
        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection connection = dbConnection.connect();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement
                        .executeQuery("SELECT p.VALOR_PEDIDO, p.NUMERO_BOLETA, m.NOMBRE_MOTOBOY AS MOTOBOY, pm.HORA_ENTREGA FROM PEDIDO p INNER JOIN PEDIDO_MOTOBOY pm ON p.NUMERO_PEDIDO = pm.NUMERO_PEDIDO INNER JOIN MOTOBOY m ON pm.COD_MOTOBOY = m.COD_MOTOBOY")) {

            while (resultSet.next()) {
                String valorPedido = resultSet.getString("VALOR_PEDIDO");
                String numeroBoleta = resultSet.getString("NUMERO_BOLETA");
                String motoboy = resultSet.getString("MOTOBOY");
                String horaEntrega = resultSet.getString("HORA_ENTREGA");
                orders.add(new String[]{valorPedido, numeroBoleta, motoboy, horaEntrega});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar pedidos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return orders;
    }

    private static void showOrderDetails(JFrame frame) {
        String[] columnNames = {"Código Producto", "Producto", "Valor Venta", "Cantidad Producto"};
        DefaultTableModel detailsTableModel = new DefaultTableModel(columnNames, 0);
        JTable detailsTable = new JTable(detailsTableModel);
        JLabel mostSoldProductLabel = new JLabel();

        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection connection = dbConnection.connect();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT " +
                    "p.COD_PRODUCTOS," +
                    "p.PRODUCTOS," +
                    "p.VALOR_VENTA," +
                    "SUM(dp.CANTIDAD_PRODUCTO) AS Cantidad_vendida " +
                "FROM " +
                    "DETALLE_PEDIDO dp " +
                "INNER JOIN PRODUCTO p ON " +
                    "dp.COD_PRODUCTOS = p.COD_PRODUCTOS " +
                "GROUP BY " +
                    "p.COD_PRODUCTOS," +
                    "p.PRODUCTOS," +
                    "p.VALOR_VENTA " +
                "ORDER BY SUM(dp.CANTIDAD_PRODUCTO) DESC ")) {
            
            boolean firstRow = true;
            while (resultSet.next()) {
                String codProductos = resultSet.getString("COD_PRODUCTOS");
                String productos = resultSet.getString("PRODUCTOS");
                String valorVenta = resultSet.getString("VALOR_VENTA");
                String cantidad = resultSet.getString("Cantidad_vendida");
                detailsTableModel.addRow(new Object[]{codProductos, productos, valorVenta, cantidad});
                
                if (firstRow) {
                    mostSoldProductLabel.setText("Producto más vendido: " + productos + " con " + cantidad + " unidades vendidas.");
                    firstRow = false;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al mostrar detalles del pedido: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.add(new JScrollPane(detailsTable), BorderLayout.CENTER);
        detailsPanel.add(mostSoldProductLabel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(frame, detailsPanel, "Detalles de Pedido", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void addNewSale() {
        // Implementar lógica para agregar una nueva venta
        JOptionPane.showMessageDialog(null, "Nueva venta agregada");
    }
}