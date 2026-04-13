package edu.connexion3a77.tests;

import edu.connexion3a77.services.FilActualiteService;
import edu.connexion3a77.tools.MyConnection;

public class MainClass {
    public static void main(String[] args) {
        MyConnection.getInstance().initializeDatabase();
        FilActualiteService service = new FilActualiteService();
        System.out.println("Connexion etablie. Nombre d'actualites: " + service.getData().size());
    }
}
