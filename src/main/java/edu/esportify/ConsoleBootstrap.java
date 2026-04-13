package edu.esportify;

import edu.esportify.services.CandidatureService;
import edu.esportify.services.EquipeService;
import edu.esportify.services.RecrutementService;
import edu.esportify.tools.MyConnection;

public class ConsoleBootstrap {
    public static void main(String[] args) {
        EquipeService equipeService = new EquipeService();
        RecrutementService recrutementService = new RecrutementService();
        CandidatureService candidatureService = new CandidatureService();

        System.out.println("Connexion et module Equipes prets");
        System.out.println("Equipes: " + equipeService.getData().size());
        System.out.println("Recrutements: " + recrutementService.getData().size());
        System.out.println("Candidatures: " + candidatureService.getData().size());
        System.out.println("Singleton MySQL: " + MyConnection.getInstance().hashCode());
    }
}
