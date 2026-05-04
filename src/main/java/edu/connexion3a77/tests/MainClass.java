package edu.connexion3a77.tests;

import edu.connexion3a77.entities.DemandeParticipation;
import edu.connexion3a77.entities.Tournoi;
import edu.connexion3a77.services.DemandeParticipationService;
import edu.connexion3a77.services.TournoiService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class MainClass {

    public static void main(String[] args) {
        TournoiService ts = new TournoiService();
        DemandeParticipationService dps = new DemandeParticipationService();

        String nomNouveauTournoi = "Winter Legends Cup";
        Tournoi nouveauTournoi = new Tournoi(
                nomNouveauTournoi,
                "Ligue",
                "Valorant",
                Date.valueOf(LocalDate.now().plusDays(7)),
                Date.valueOf(LocalDate.now().plusDays(10)),
                20,
                10000.0
        );

        ts.ajouter(nouveauTournoi);

        Tournoi tournoiYoussef = new Tournoi(
                "youssef",
                "Ligue",
                "Valorant",
                Date.valueOf(LocalDate.now().plusDays(8)),
                Date.valueOf(LocalDate.now().plusDays(12)),
                2,
                5000.0
        );
        ts.ajouter(tournoiYoussef);

        DemandeParticipation demandeTournoi27 = new DemandeParticipation(
                27,
                "Demande pour le tournoi id 27",
                "Pro"
        );
        dps.ajouter(demandeTournoi27);
        
         DemandeParticipation demandeTournoi28 = new DemandeParticipation(
                27,
                "Demande pour le tournoi id 27",
                "medium"
        );
        dps.ajouter(demandeTournoi28);

        DemandeParticipation demandeTournoi29 = new DemandeParticipation(
                27,
                "Demande pour le tournoi id 27",
                ""
        );
        dps.ajouter(demandeTournoi29);
        

        List<Tournoi> tournois = ts.afficher();
        System.out.println("----- TOURNOIS ENREGISTRES -----");
        tournois.forEach(t -> System.out.println(t.getId() + " | " + t.getNomTournoi() + " | " + t.getNomJeu() + " | " + t.getDateDebut() + " -> " + t.getDateFin()));
    }
}
