package edu.PROJETPI.services;

import edu.PROJETPI.entites.Produit;

import java.util.List;

public class CatalogueProduitService {
    public List<Produit> readAll() {
        return List.of(
                new Produit(1, "Clavier Mecanique", 189.90, 15, "Switchs tactiles pour jeu et bureautique"),
                new Produit(2, "Souris RGB", 89.50, 22, "Capteur haute precision et 6 boutons"),
                new Produit(3, "Casque Gaming", 249.00, 10, "Audio surround et micro antibruit"),
                new Produit(4, "Tapis XXL", 39.90, 30, "Surface large pour setup complet"),
                new Produit(5, "Ecran 27 pouces", 899.00, 7, "QHD 165Hz pour experience fluide")
        );
    }
}
