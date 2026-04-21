# Reponses a la grille - Projet `conexion 3A27`

## Hypothese de travail

Ce document est redige a partir du projet `conexion 3A27`, qui est une application Java desktop de gestion commerciale developpee avec JavaFX, JDBC et MySQL. Le projet permet de consulter un catalogue de produits, constituer un panier, saisir les informations d'une commande, valider le paiement et administrer les commandes et paiements.

## 1. Fonctionnement general du programme

Le programme suit un parcours utilisateur simple et logique :

1. Au lancement, la classe `MainApp` initialise la base de donnees MySQL via `MyConexion.initDatabase()`.
2. Ensuite, l'application ouvre l'ecran principal `main-view.fxml`, pilote par `MainController`.
3. L'utilisateur consulte le catalogue des produits disponibles.
4. Il selectionne un produit, saisit une quantite et l'ajoute au panier.
5. Le panier est gere temporairement en memoire par la classe `OrderSession`.
6. Depuis le panier, l'utilisateur peut modifier les quantites, supprimer des lignes ou vider le panier.
7. Il passe ensuite a l'ecran commande pour saisir ses informations personnelles : nom, prenom, telephone et adresse.
8. Ces informations sont enregistrees dans la table `commande` avec le statut `EN_ATTENTE`.
9. Sur l'ecran de paiement, la commande est finalisee par `CheckoutService`, qui :
   - met a jour ou cree la commande,
   - enregistre les lignes de commande,
   - enregistre le paiement,
   - change le statut a `PAYEE`,
   - valide la transaction SQL,
   - vide la session panier.
10. Un tableau de bord administrateur permet ensuite d'afficher, trier, filtrer, modifier et supprimer les commandes et les paiements.

## 2. Architecture generale

L'architecture suit une separation par couches :

- **Couche presentation**
  - Contient les controleurs JavaFX et les fichiers FXML.
  - Exemples : `MainController`, `LigneCommandeController`, `CommandeController`, `PaymentController`, `AdminDashboardController`.
  - Role : gerer l'interface graphique, les actions utilisateur et la navigation entre les ecrans.

- **Couche metier / services**
  - Contient les classes qui portent la logique fonctionnelle et les operations CRUD.
  - Exemples : `ServiceCommande`, `ServiceLigneCommande`, `ServicePayment`, `CheckoutService`, `CatalogueProduitService`, `OrderSession`.
  - Role : traiter les donnees, appliquer les regles metier et communiquer avec la base.

- **Couche modele / entites**
  - Contient les objets representant les donnees du domaine.
  - Exemples : `Commande`, `LigneCommande`, `Payment`, `Produit`, `CartItem`.
  - Role : encapsuler les attributs et servir d'objets de transfert entre les couches.

- **Couche utilitaire / technique**
  - Exemples : `MyConexion`, `SceneNavigator`, `AlertUtils`.
  - Role : factoriser les fonctions techniques communes : connexion JDBC, navigation entre scenes, affichage des alertes.

Cette architecture est simple, lisible et adaptee a un projet academique JavaFX.

## 3. Explication des classes principales

### `MainApp`

Classe de demarrage de l'application. Elle lance JavaFX, initialise la base de donnees et charge la fenetre principale. Elle securise aussi le demarrage : si MySQL n'est pas disponible, une alerte s'affiche puis l'application se ferme.

### `MainController`

Controleur de l'ecran catalogue.

Ses responsabilites sont :

- afficher la liste des produits,
- permettre la selection d'un produit,
- verifier la quantite demandee,
- ajouter le produit au panier,
- naviguer vers le panier ou vers le tableau de bord administrateur.

### `LigneCommandeController`

Controleur de l'ecran panier.

Ses responsabilites sont :

- afficher toutes les lignes du panier,
- modifier la quantite d'une ligne,
- supprimer une ligne,
- vider tout le panier,
- calculer et afficher le nombre total d'articles et le prix total,
- passer a l'etape commande.

### `CommandeController`

Controleur de l'ecran informations commande.

Ses responsabilites sont :

- charger le brouillon de commande depuis `OrderSession`,
- afficher un resume du panier,
- lire et valider les informations client,
- creer ou mettre a jour une commande en base,
- conserver le brouillon pour l'etape suivante,
- rediriger vers le paiement.

### `PaymentController`

Controleur de l'ecran paiement.

Ses responsabilites sont :

- afficher le recapitulatif de la commande,
- verifier qu'il existe une commande brouillon et un panier non vide,
- lancer la finalisation du processus via `CheckoutService`,
- afficher le succes ou l'erreur.

### `AdminDashboardController`

Controleur du tableau de bord administrateur.

Ses responsabilites sont :

- afficher la liste des commandes,
- filtrer par statut,
- rechercher une commande,
- trier par ID, montant ou date,
- modifier le statut d'une commande,
- supprimer une commande,
- afficher la liste des paiements,
- consulter les details d'une commande payee,
- supprimer un paiement.

### `EditCommandeStatusController`

Fenetre secondaire qui permet de modifier rapidement le statut d'une commande (`EN_ATTENTE`, `VALIDEE`, `PAYEE`, `ANNULEE`).

### `OrderSession`

Classe tres importante pour la logique metier. C'est un **singleton** qui conserve temporairement :

- les articles du panier,
- le brouillon de commande.

Elle joue le role de memoire de session tant que l'utilisateur navigue entre les ecrans.

### `CheckoutService`

Classe centrale pour la validation finale. Elle orchestre toute la transaction metier :

- insertion ou mise a jour de la commande,
- suppression des anciennes lignes,
- insertion des nouvelles lignes de commande,
- insertion du paiement,
- validation SQL (`commit`) ou annulation (`rollback`) en cas d'erreur.

### `ServiceCommande`, `ServiceLigneCommande`, `ServicePayment`

Ces classes implementent la couche d'acces aux donnees. Elles utilisent JDBC avec `PreparedStatement` pour faire les operations CRUD sur les tables :

- `commande`
- `lignecommande`
- `payment`

### `CatalogueProduitService`

Service simple qui retourne une liste statique de produits. Ici, les produits ne viennent pas de la base de donnees mais sont definis en dur dans le code. C'est un choix de simplification du projet.

### `MyConexion`

Classe technique chargee de :

- ouvrir la connexion MySQL,
- creer la base `projetpi` si elle n'existe pas,
- creer les tables necessaires,
- verifier l'existence des colonnes manquantes.

Elle centralise donc toute l'initialisation de l'infrastructure de donnees.

## 4. Explication des entites

### `Produit`

Represente un produit du catalogue avec :

- `id`
- `nom`
- `prix`
- `stock`
- `description`

### `CartItem`

Represente un article du panier. Cette classe est differente de `LigneCommande` car elle est utilisee avant l'enregistrement en base. Elle contient :

- l'identifiant du produit,
- le nom du produit,
- la quantite,
- le prix unitaire,
- un calcul dynamique du sous-total.

### `Commande`

Represente une commande client. Elle contient :

- l'identifiant,
- la date de commande,
- le total,
- l'identifiant client,
- le statut,
- le nom,
- le prenom,
- le telephone,
- l'adresse.

### `LigneCommande`

Represente une ligne detaillee rattachee a une commande. Elle contient :

- l'id de la ligne,
- l'id de la commande,
- l'id du produit,
- la quantite,
- le prix unitaire.

### `Payment`

Represente un paiement lie a une commande. Elle contient :

- l'id du paiement,
- l'id de la commande,
- le montant,
- la date de paiement.

## 5. Explication des methodes importantes

### Dans `MainController`

- `initialize()` : configure les colonnes du tableau et charge les produits.
- `addSelectedProduct()` : ajoute le produit selectionne au panier apres validation de la quantite.
- `goToCart()` : ouvre l'ecran panier.
- `clearCart()` : vide la session panier.
- `openAdminDashboard()` : ouvre le tableau de bord administrateur.

### Dans `LigneCommandeController`

- `refresh()` : recharge la table du panier et les totaux.
- `updateQuantite()` : modifie la quantite d'une ligne selectionnee.
- `removeLigne()` : supprime une ligne du panier.
- `clearCart()` : vide tout le panier.
- `goToCommande()` : passe a l'ecran informations commande.

### Dans `CommandeController`

- `loadSessionData()` : charge les informations du brouillon et le resume du panier.
- `readForm()` : lit le formulaire, valide les champs et construit un objet `Commande`.
- `continueToPayment()` : sauvegarde la commande en base puis passe au paiement.
- `updateFirstItemQuantity()` : permet d'ajuster rapidement la quantite du premier article affiche.
- `refreshCartPreview()` : met a jour l'aperçu du panier.

### Dans `PaymentController`

- `loadDraftSummary()` : affiche les informations de la commande avant validation.
- `confirmPayment()` : appelle `CheckoutService.checkout()` pour finaliser la commande et le paiement.

### Dans `OrderSession`

- `addProduct()` : ajoute un produit au panier ou incremente la quantite si le produit existe deja.
- `updateQuantity()` : met a jour une quantite.
- `removeProduct()` : retire un produit.
- `clearCart()` : vide le panier et supprime le brouillon.
- `getCartTotal()` : calcule le montant total.
- `getTotalItems()` : calcule le nombre total d'articles.
- `resetAfterCheckout()` : reinitialise la session apres paiement.

### Dans `CheckoutService`

- `checkout(OrderSession session, Date paymentDate)` :
  - recupere la commande brouillon,
  - demarre une transaction SQL,
  - cree ou met a jour la commande,
  - supprime les anciennes lignes,
  - ajoute les nouvelles lignes,
  - cree le paiement,
  - fait un `commit`,
  - annule avec `rollback` si une erreur survient.

## 6. Logique metier

La logique metier principale est la suivante :

### Gestion du catalogue

Le catalogue affiche une liste de produits avec leur stock. L'utilisateur ne peut pas commander plus que le stock disponible. Cela introduit une premiere regle metier de validation.

### Gestion du panier

Le panier est gere en memoire par `OrderSession`. Tant que le paiement n'est pas confirme, les informations restent temporaires. Cela permet une navigation fluide entre plusieurs ecrans sans ecrire chaque modification en base de donnees.

### Creation d'une commande

Une commande n'est pas directement payee. Lors de la saisie des informations client, une commande brouillon est enregistree avec le statut `EN_ATTENTE`. Cela permet de separer l'etape de collecte des informations de l'etape de paiement.

### Finalisation du paiement

Lors de la validation du paiement :

- le total est recalcule depuis le panier,
- le statut de la commande devient `PAYEE`,
- les lignes de commande sont enregistrees,
- le paiement est insere,
- toutes les operations sont executees dans une transaction unique.

Le choix de la transaction est important car il garantit la coherence des donnees. Par exemple, on evite le cas ou la commande serait enregistree sans paiement, ou inversement.

### Administration

Le tableau de bord permet de suivre les commandes et paiements deja enregistres. Il permet aussi la mise a jour du statut et la suppression. Cela couvre la partie gestion back-office du projet.

## 7. Choix techniques

### Java 17

Le projet utilise Java 17, une version moderne et stable du langage.

### JavaFX

JavaFX a ete choisi pour construire une interface graphique desktop avec :

- des vues en FXML,
- des controleurs Java separes,
- une bonne separation entre presentation et logique.

### MySQL + JDBC

Le stockage persistant est gere avec MySQL. L'acces a la base se fait en JDBC natif. Ce choix est pedagogique, car il montre clairement :

- l'ouverture de connexion,
- les requetes SQL,
- les `PreparedStatement`,
- la gestion des transactions.

### Pattern Singleton

Le singleton est utilise dans :

- `MyConexion` pour partager une seule connexion principale,
- `OrderSession` pour partager l'etat du panier dans toute l'application.

### Interfaces de service

Les interfaces `IServiceCommande`, `IServiceLigneCommande` et `IServicePayment` permettent de definir un contrat clair pour les operations CRUD. Cela ameliore la lisibilite et facilite l'evolution du code.

### Tests unitaires

Le projet contient des tests JUnit pour :

- `ServiceCommande`
- `ServiceLigneCommande`
- `ServicePayment`

Ces tests verifient l'ajout, la modification, la lecture et la suppression. Cela montre une volonte de fiabiliser la couche service.

## 8. Points forts du projet

- Architecture claire par couches.
- Navigation simple entre les ecrans.
- Bonne separation entre interface, logique metier et acces aux donnees.
- Gestion transactionnelle dans `CheckoutService`.
- Tableau de bord administrateur assez complet.
- Presence de tests sur les services.
- Initialisation automatique de la base de donnees au demarrage.

## 9. Limites et pistes d'amelioration

Voici les principales limites que l'etudiant peut expliquer honnêtement :

- Les produits sont statiques et non stockes en base.
- Le `clientId` est encore un placeholder et il n'existe pas de vraie entite `Client`.
- Les tables ne definissent pas explicitement de cles etrangeres SQL.
- La gestion du stock n'est pas decrementee apres paiement.
- La connexion MySQL est configuree en local avec l'utilisateur `root`, ce qui n'est pas ideal pour la production.
- Certaines validations metier pourraient etre enrichies.

Ameliorations possibles :

- ajouter une table `produit`,
- ajouter une table `client`,
- gerer les cles etrangeres et contraintes d'integrite,
- decrementer automatiquement le stock apres paiement,
- securiser la configuration de connexion via un fichier externe,
- ajouter plus de tests, notamment sur `CheckoutService` et `OrderSession`.

## 10. Conclusion orale possible

Ce projet est une application de gestion commerciale developpee en JavaFX avec persistance MySQL. Son objectif principal est de simuler un processus complet de commande : consultation d'un catalogue, ajout au panier, saisie des informations client, paiement puis suivi administratif. L'architecture est organisee en couches avec des controleurs pour l'IHM, des services pour la logique metier et des entites pour les donnees. Le point technique le plus important est `CheckoutService`, qui garantit la coherence entre commande, lignes de commande et paiement grace a une transaction SQL. Le projet est donc coherent, fonctionnel et bien structure pour un contexte academique.

