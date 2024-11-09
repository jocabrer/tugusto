package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConnection {
    private String url;
    private Properties props;

    public DatabaseConnection() {
        Dotenv dotenv = Dotenv.load();
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");

        this.url = "jdbc:oracle:thin:@//192.168.159.133:1521/MINERA_PDB";
        this.props = new Properties();
        this.props.setProperty("user", user);
        this.props.setProperty("password", password);
        this.props.setProperty("oracle.net.CONNECT_TIMEOUT", "30000"); // 30 seconds timeout

        try (Connection connection = DriverManager.getConnection(url, props)) {
            System.out.println("Conexión exitosa la base de datos Oracle!");
            // Ejecuto query simple para probar conectividad 
            connection.createStatement().executeQuery("SELECT 1 FROM DUAL");
        } catch (SQLException e) {
            System.out.println("No se puede conectar con el servidor Oracle.");
            // Muestro error en consola
            System.err.println(e.getMessage());
        }
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, props);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }
}