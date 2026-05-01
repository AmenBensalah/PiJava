package edu.ProjetPI.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private String url = System.getenv().getOrDefault(
            "PIJAVA_DB_URL",
            "jdbc:mysql://localhost:3306/esportify?serverTimezone=UTC"
    );
    private String login = System.getenv().getOrDefault("PIJAVA_DB_USER", "root");
    private String pwd = System.getenv().getOrDefault("PIJAVA_DB_PASSWORD", "");

    private Connection cnx;
    public static MyConnection instance;

    private MyConnection(){
        try {
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("Connexion établie!");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public Connection getCnx() {
        return cnx;
    }

    public static MyConnection getInstance(){
        if(instance == null){
            instance = new MyConnection();
        }
        return instance;
    }
}
