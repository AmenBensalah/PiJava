package edu.esportify.tests;

import edu.esportify.entities.Equipe;
import edu.esportify.entities.Recrutement;
import edu.esportify.services.EquipeService;
import edu.esportify.services.RecrutementService;
import edu.esportify.tools.MyConnection;

public class MainClass {

    public static void main(String[] args) {
        EquipeService equipeService = new EquipeService();
        RecrutementService recrutementService = new RecrutementService();

        if (equipeService.getData().isEmpty()) {
            Equipe equipe = new Equipe("Falcons", "Equipe Valorant competitive", "Diamond", "FLC", "EU");
            equipe.setMaxMembers(5);
            equipe.setPrivate(false);
            equipe.setActive(true);
            equipe.setDiscordInviteUrl("https://discord.gg/falcons");
            equipeService.addEntity(equipe);
        }

        Equipe premiereEquipe = equipeService.getData().get(0);
        if (recrutementService.getData().isEmpty()) {
            Recrutement recrutement = new Recrutement(
                    "Duelist recherche",
                    "Nous cherchons un joueur agressif et discipline.",
                    "Ouvert",
                    premiereEquipe.getId()
            );
            recrutementService.addEntity(recrutement);
        }

        System.out.println(equipeService.getData());
        System.out.println(recrutementService.getData());

        MyConnection m1 = MyConnection.getInstance();
        MyConnection m2 = MyConnection.getInstance();
        System.out.println(m1.hashCode() + " - " + m2.hashCode());
    }
}
