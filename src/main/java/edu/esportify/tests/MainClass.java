package edu.esportify.tests;

import edu.esportify.services.FilActualiteService;
import edu.esportify.tools.MyConnection;

public class MainClass {
    public static void main(String[] args) {
        MyConnection.getInstance().initializeDatabase();
        FilActualiteService service = new FilActualiteService();
        System.out.println("Connexion etablie. Nombre d'actualites: " + service.getData().size());
    }
}
