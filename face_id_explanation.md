# Explication globale des tâches et fonctionnalités du projet

Ce document est conçu pour t'aider à te préparer pour ta validation. Il explique les **principales tâches du projet**, leur **intégration dans les pages JavaFX**, leur **design**, leurs **boutons fonctionnels**, leur **architecture**, ainsi que les parties **CRUD**, **IA**, **API**, **messagerie**, **notifications** et **authentification**.

Le **Face ID** n'est donc qu'une **fonctionnalité parmi l'ensemble du projet**, au même titre que :

* le fil d'actualité
* la gestion des équipes
* les candidatures
* le dashboard manager
* la boutique et les commandes
* les tournois
* la partie admin
* les tâches IA d'aide à la décision

---

## 1. Vue globale du projet

Le projet a été pensé comme une **application complète multi-modules**, avec :

* des pages séparées par fonctionnalité
* des interfaces adaptées selon le rôle utilisateur
* une navigation entre les vues
* des services métier dédiés
* des opérations CRUD
* des traitements IA
* des intégrations API
* un design JavaFX cohérent

Le principe général est toujours le même :

1. **Une vue FXML**
2. **Un controller Java**
3. **Un ou plusieurs services**
4. **Des entités métier**
5. **Une logique de navigation et d'interaction**

## 2. Le Face ID comme une tâche parmi les autres

La reconnaissance faciale fait partie du projet, mais elle ne représente pas tout le travail. Elle constitue surtout la partie **authentification intelligente**.

## 3. Les Fichiers Clés de la Fonctionnalité Face ID

Le système de reconnaissance faciale est principalement géré par deux fichiers Java dans ton projet :

1. **`FaceApiCaptureDialog.java` (Le Contrôleur / L'Interface et l'IA)**
   * **Rôle :** Gère l'interface de capture (Webcam), charge les modèles d'Intelligence Artificielle, détecte le visage, vérifie la qualité de l'image (luminosité, flou) et extrait les "caractéristiques" du visage (le *descriptor*).
   * **Chemin :** `edu/ProjetPI/controllers/FaceApiCaptureDialog.java`

2. **`FaceIdAuthService.java` (Le Service / La Logique Métier)**
   * **Rôle :** Gère la logique d'authentification. Il prend le vecteur du visage extrait, le convertit en JSON pour la base de données (lors de l'inscription), ou le compare avec les visages stockés dans la base de données (lors de la connexion) en utilisant des formules mathématiques (distance euclidienne et similarité cosinus).
   * **Chemin :** `edu/ProjetPI/services/FaceIdAuthService.java`

---

## 4. Comment fonctionne la Prédiction Face ID (Le côté IA)

Pour la prédiction, **tu n'as pas entraîné un modèle de zéro**. Tu as utilisé une approche moderne et optimisée appelée **Transfer Learning** en utilisant des modèles pré-entraînés via **ONNX Runtime** (Open Neural Network Exchange).

### Les modèles exacts utilisés :
1. **Modèle de Détection (YuNet - `face_detection_yunet_2023mar.onnx`) :**
   * Son but est de trouver *où* se trouve le visage dans l'image de la webcam.
   * Il retourne une "bounding box" (le rectangle autour du visage) et **5 repères faciaux (landmarks)** : les deux yeux, le nez, et les deux coins de la bouche.

2. **Modèle de Reconnaissance (SFace - `face_recognition_sface_2021dec.onnx`) :**
   * Une fois le visage détecté et recadré, ce modèle l'analyse et génère un **Vecteur de caractéristiques (Face Descriptor / Embedding)**. C'est une liste de nombres (souvent 128 ou 512 valeurs) qui représente mathématiquement l'identité unique de la personne.

### Pourquoi ONNX Runtime ?
ONNX est un format standard ouvert pour les modèles d'apprentissage automatique. Tu as utilisé la librairie `ai.onnxruntime` en Java pour exécuter ces modèles localement, rapidement et sans avoir besoin d'appeler une API externe (comme AWS ou Google).

---

## 5. Le Processus Étape par Étape du Face ID

Voici exactement ce qui se passe quand un utilisateur utilise le Face ID :

### A. La Capture et le Traitement (Dans `FaceApiCaptureDialog`)
1. **Ouverture de la Webcam :** Le programme utilise la librairie `sarxos.webcam` pour capturer le flux vidéo.
2. **Détection (YuNet) :** Le modèle YuNet scanne l'image pour trouver un visage.
3. **Alignement (Affine Transform) :** En utilisant les 5 repères (yeux, nez, bouche), le programme fait tourner et redimensionne le visage pour qu'il soit parfaitement centré et droit. C'est crucial pour que l'IA puisse le lire correctement.
4. **Vérification de la Qualité :** Le code calcule la **luminosité** et la **netteté (Laplacian variance)** pour s'assurer que la photo n'est ni trop sombre, ni trop floue.
5. **Extraction (SFace) :** Le modèle SFace génère le vecteur mathématique. Pour plus de précision, le système capture **3 images consécutives**, extrait les 3 vecteurs, et fait une **moyenne** pour obtenir un vecteur très stable.

### B. "La création du Dataset" (Inscription)
Si le professeur te demande "Comment as-tu fait ton dataset ?", la réponse est :
**"Nous utilisons une approche de type 'One-Shot Learning'."**
* Tu n'as pas un dossier avec des milliers de photos de tes utilisateurs.
* Lors de l'inscription, tu prends le vecteur mathématique du visage (le tableau de nombres doubles).
* Dans `FaceIdAuthService.java`, la méthode `toJson()` convertit ce tableau de nombres en une chaîne de texte (JSON Array) : exemple `[0.12, -0.45, 0.88, ...]`.
* Ce texte JSON est sauvegardé directement dans l'entité `User` de ta base de données (colonne `faceDescriptorJson`). **Ton "dataset" est donc simplement cette base de données de vecteurs mathématiques.**

### C. L'Authentification (Connexion)
Quand quelqu'un essaie de se connecter :
1. La webcam s'allume et extrait le vecteur du visage de la personne devant l'écran (le *probe*).
2. Le système récupère tous les vecteurs stockés dans la base de données.
3. La méthode `authenticateByDescriptor` dans `FaceIdAuthService` compare le nouveau vecteur avec ceux de la base.
4. **Les Mathématiques utilisées :**
   * **Distance Euclidienne (`euclideanDistance`) :** Mesure la distance physique entre les deux points dans un espace multidimensionnel. Si la distance est **<= 1.128**, c'est un match.
   * **Similarité Cosinus (`cosineSimilarity`) :** Mesure l'angle entre les deux vecteurs. Si la similarité est **>= 0.363**, c'est un match.
5. Si un utilisateur de la base de données dépasse ce seuil de correspondance, il est authentifié et connecté avec succès.

---

## 6. Résumé Face ID pour ta validation
* "Nous n'avons pas réinventé la roue en entraînant un modèle de zéro. Nous avons intégré l'état de l'art avec les modèles **YuNet** pour la détection et **SFace** pour la reconnaissance, en utilisant **ONNX Runtime en Java**."
* "La sécurité et la fiabilité sont assurées par des filtres algorithmiques : nous vérifions la luminosité et la netteté de l'image (variance de Laplace) avant d'accepter un visage."
* "Nous ne stockons pas d'images réelles des utilisateurs dans la base de données pour des raisons de confidentialité (RGPD). Nous stockons uniquement une représentation mathématique irréversible (un vecteur JSON)."
* "La comparaison se fait via le calcul de la Distance Euclidienne et la Similarité Cosinus."

---

## 7. Explication detaillee de la partie Fil d'actualite

Cette partie du projet n'est pas juste un simple mur de publications. En realite, le module **Fil d'actualite** centralise plusieurs sous-fonctionnalites avancees :

1. **Publication et consultation des posts**
2. **CRUD Back Office des posts, commentaires et annonces**
3. **Interactions sociales : likes, sauvegardes, partages**
4. **Notifications dynamiques**
5. **Messenger integre**
6. **Resume et logique IA**
7. **Integration API / streaming / media**

Le fichier principal qui orchestre tout cela est :

* **`FilActualiteController.java`**
  * **Role :** Controle la logique front office et back office du fil, la publication, l'affichage dynamique, le Messenger, les notifications, les filtres, les interactions sociales et certains blocs IA.
  * **Chemin :** `src/main/java/edu/esportify/controllers/FilActualiteController.java`

### Les services utilises dans cette partie

1. **`FilActualiteService.java`**
   * Gere les operations CRUD des publications.

2. **`CommentaireService.java`**
   * Gere les operations CRUD des commentaires.

3. **`AnnouncementService.java`**
   * Gere les annonces affichees dans le fil.

4. **`SocialInteractionService.java`**
   * Gere les likes, les sauvegardes et les partages.

5. **`MessengerService.java`**
   * Gere les conversations, les messages, les messages non lus et la persistance en base de donnees.

6. **`MessengerRealtimeBridge.java`**
   * Sert de pont temps reel pour rafraichir l'interface Messenger apres un envoi, une lecture ou une creation de conversation.

7. **`MessengerCallService.java`**
   * Gere la logique des appels ou invitations d'appel depuis Messenger.

8. **`StreamingIntegrationService.java`**
   * Recupere des flux et highlights YouTube via API HTTP.

---

## 8. Comment fonctionne le Fil d'actualite

### A. Publication et affichage

Quand un utilisateur publie un post :

1. Il saisit un texte, et peut ajouter une image, une video ou un evenement.
2. `FilActualiteController` construit un objet `FilActualite`.
3. Cet objet est envoye vers `FilActualiteService`.
4. Le service enregistre la publication.
5. L'interface recharge ensuite le flux et met en avant le nouveau post.

Le fil supporte donc :

* des **posts texte**
* des **posts media**
* des **liens**
* des **evenements**
* des **annonces**

### B. CRUD complet

Le module suit le meme principe CRUD que les autres taches du projet :

* **Create :** ajout d'un post, commentaire ou annonce
* **Read :** affichage du fil, des commentaires, des annonces et des statistiques
* **Update :** modification des publications ou annonces depuis le back office
* **Delete :** suppression depuis les interfaces d'administration

Dans le code, on voit que `FilActualiteController` pilote plusieurs vues back office :

* gestion des posts
* gestion des annonces
* gestion des commentaires

Cela montre que le module ne sert pas seulement a afficher des donnees, mais aussi a les **administrer proprement**.

---

## 9. Partie Messenger integree

La partie Messenger est une vraie sous-application dans le fil d'actualite.

### Les fichiers principaux

1. **`MessengerService.java`**
   * Gere la base de donnees Messenger.
   * Cree ou retrouve une conversation.
   * Enregistre les messages.
   * Marque les messages comme lus.
   * Calcule le nombre de messages non lus.

2. **`FilActualiteController.java`**
   * Gere l'interface Messenger.
   * Ouvre la fenetre inbox.
   * Affiche les conversations.
   * Affiche les messages.
   * Envoie les nouveaux messages.
   * Gere la recherche de contacts et le rafraichissement.

### Le principe technique

Le Messenger repose sur trois tables logiques :

* `conversations`
* `conversation_participants`
* `messages`

Quand un utilisateur envoie un message :

1. Le controller recupere la conversation active.
2. Il appelle `messengerService.sendMessage(...)`.
3. Le message est insere en base.
4. La conversation est mise a jour avec le dernier message.
5. `MessengerRealtimeBridge.publish()` declenche le rafraichissement de l'interface.

Donc, d'un point de vue soutenance, tu peux dire :

**"Nous avons integre un Messenger temps reel simplifie dans le fil d'actualite, avec persistance en base, suivi des messages lus/non lus, creation automatique de conversation et rafraichissement dynamique de l'interface."**

---

## 10. Partie Notifications

Les notifications sont gerees directement dans `FilActualiteController`.

### Ce que fait le systeme

* Il maintient une liste `appNotifications`
* Il gere les notifications deja lues via `readNotificationKeys`
* Il calcule le badge de notifications
* Il ouvre une popup de notifications
* Il permet de tout marquer comme lu

### Role fonctionnel

Les notifications servent a informer l'utilisateur de l'activite recente :

* interactions sociales
* changements dans le fil
* activite Messenger
* rappels d'attention utilisateur

Donc ici, le principe est le meme que pour les autres modules :

* une **source de donnees**
* une **liste observable JavaFX**
* une **mise a jour de l'interface**
* une **gestion d'etat lu/non lu**

---

## 11. Partie IA dans le Fil d'actualite

Dans cette partie, l'IA n'est pas un modele lourd comme le Face ID, mais une **IA fonctionnelle et decisionnelle** qui aide l'utilisateur.

### Ce qu'on retrouve dans le module

1. **Hashtags IA**
   * Le design du fil met en avant des tags et des tendances intelligentes.

2. **Resume IA**
   * Le code contient la logique d'affichage d'un resume IA du contenu ou d'indications intelligentes dans le flux.

3. **Filtres intelligents**
   * Le systeme classe les publications par type : media, equipes, joueurs, tournois, recommandations IA, etc.

4. **Analyse d'activite**
   * Le controller calcule des indicateurs comme :
   * nombre de publications
   * nombre d'evenements
   * nombre de posts avec media
   * derniere activite
   * tendances de publication

### Comment presenter cette IA

Tu peux expliquer cela ainsi :

**"Dans le fil d'actualite, l'IA a un role d'assistance et d'organisation. Elle ne remplace pas l'utilisateur, mais l'aide a mieux lire, filtrer, resumer et prioriser le contenu affiche."**

---

## 12. Partie API et integration externe

Le fil d'actualite utilise aussi une logique d'integration API, surtout avec le streaming.

### Fichier principal

* **`StreamingIntegrationService.java`**

### Ce que fait ce service

1. Il utilise `HttpClient` de Java.
2. Il appelle l'API YouTube.
3. Il recupere :
   * des lives esports
   * des highlights esports
4. Il parse la reponse JSON.
5. Il transforme les resultats en cartes exploitables dans l'interface.

### Ce que cela montre

Cela prouve que le module ne travaille pas uniquement avec la base locale, mais sait aussi :

* consommer une API externe
* gerer une cle API
* parser des donnees JSON
* injecter ces donnees dans l'interface utilisateur

---

## 13. Partie Design et architecture

Le design suit le meme principe moderne que dans les autres modules :

* separation **Controller / Service / Entity**
* interface JavaFX riche
* utilisation de `ObservableList`
* vues front office et back office
* logique de badges, popups, cartes, panneaux et messagerie

### Architecture appliquee

1. **Controller**
   * Gere les actions utilisateur et la mise a jour visuelle

2. **Service**
   * Gere la logique metier et les acces base/API

3. **Entity**
   * Modele les donnees : post, commentaire, message, conversation, notification, utilisateur

4. **Vue JavaFX / FXML**
   * Gere l'affichage et l'experience utilisateur

Donc si on te demande le principe de conception, tu peux dire :

**"Nous avons respecte une architecture en couches : interface, controleur, services et entites. Cela rend le module maintenable, extensible et reutilisable."**

---

## 14. Partie Taches avancees IA

Quand tu dis "les taches IA", il faut bien distinguer deux niveaux :

### A. Dans le fil d'actualite

Les taches avancees sont :

* tri intelligent du contenu
* recommandations ou categories IA
* synthese/resume
* priorisation visuelle
* aide a la lecture de l'activite

### B. Dans les autres modules

Le meme principe est applique ailleurs, surtout dans la gestion d'equipe :

1. **`TaskService.java`**
   * Gere des taches d'equipe avec statut `TODO`, `IN_PROGRESS`, `BLOCKED`, `DONE`.

2. **`TeamAiAdvisorService.java`**
   * Joue le role de moteur d'analyse IA metier.
   * Il ne fait pas du deep learning, mais une **IA de recommandation et d'aide a la decision**.

### Ce que cette IA fait

Le service genere plusieurs roles IA :

* **Analyste de performance IA**
* **Recruteur IA**
* **Coach tactique IA**
* **Assistant planning IA**
* **Community manager IA**
* **Moderateur IA**

### Son principe

Cette IA prend des donnees deja existantes :

* performances
* taches
* retards
* charges de travail
* candidatures
* alertes

Puis elle produit :

* un resume
* un diagnostic
* un niveau d'etat
* des recommandations d'action

Donc pour la soutenance, tu peux dire :

**"Nous avons combine deux formes d'IA dans le projet : une IA predictive/base modele pour le Face ID, et une IA d'aide a la decision pour les autres modules comme le fil d'actualite, le recrutement, le planning et la moderation."**

---

## 15. Resume final pour presenter cette partie

Si le professeur te demande :
**"Ou sont Messenger, notifications, taches IA, API, design, CRUD et les principes de travail ?"**

Tu peux repondre :

**"Tout cela se trouve principalement dans le module Fil d'actualite, pilote par `FilActualiteController.java`, avec plusieurs services specialises. Ce module suit les memes principes que les autres taches du projet : architecture en couches, CRUD complet, separation controller/service/entity, enrichissement par IA, integration d'API externes et interface JavaFX dynamique. Le Messenger gere les conversations et messages en base, les notifications gerent l'activite utilisateur, l'IA aide a filtrer et resumer, et les autres modules reappliquent exactement la meme logique avec leurs propres services metier comme `TaskService` et `TeamAiAdvisorService`."**

---

## 16. Les autres taches : toutes les pages, leur design et leurs boutons

Dans le projet, le travail ne se limite pas au Face ID ou au fil d'actualite. L'application est organisee en **plusieurs pages FXML reliees entre elles**, avec un **design JavaFX coherent** et des **boutons fonctionnels** qui declenchent des actions reelles.

Le principe global est toujours le meme :

1. **Une page FXML** pour l'interface
2. **Un controller Java** pour les boutons et la logique
3. **Des services** pour la base de donnees, l'IA ou les traitements
4. **Une navigation centralisee** pour passer d'une page a l'autre

---

## 17. Navigation generale de l'application

La navigation principale est geree par :

* **`AppNavigator.java`**

Ce composant permet d'ouvrir les grandes interfaces :

* `goToLogin()`
* `goToManager()`
* `goToUserHome()`
* `goToAdmin()`

Cela veut dire que les boutons ne sont pas decoratifs : ils sont relies a de vraies methodes de changement de scene.

### Principe de navigation

* l'utilisateur se connecte
* selon son role, il est redirige
* chaque layout charge ensuite les sous-pages internes

Donc la logique est :

* **Login**
* **Layout principal selon le role**
* **Chargement dynamique des modules**

---

## 18. Les pages principales du projet

### A. Pages communes / base

* `login-view.fxml`
* `main-view.fxml`

Ces pages servent au point d'entree et a l'initialisation de l'application.

### B. Partie utilisateur

* `user-layout-view.fxml`
* `user-home-view.fxml`
* `user-feed-view.fxml`
* `user-teams-view.fxml`
* `user-team-detail-view.fxml`
* `user-apply-view.fxml`
* `user-candidatures-view.fxml`
* `user-manager-request-view.fxml`
* `user-store-view.fxml`
* `user-orders-view.fxml`
* `user-account-view.fxml`
* `user-tournaments-view.fxml`

### C. Partie manager

* `manager-layout-view.fxml`
* `feed-view.fxml`
* `team-form-view.fxml`
* `team-dashboard-view.fxml`
* `store-view.fxml`
* `orders-view.fxml`
* `account-view.fxml`
* `tournaments-view.fxml`

### D. Partie admin

* `admin-layout-view.fxml`
* `admin-overview-view.fxml`
* `admin-feed-view.fxml`
* `admin-teams-view.fxml`
* `admin-team-form-view.fxml`
* `admin-manager-requests-view.fxml`
* `admin-accounts-view.fxml`
* `candidate-management-view.fxml`

Cela montre bien que chaque profil a ses propres pages, son propre parcours et ses propres boutons.

---

## 19. Design des pages

Le design repose sur JavaFX avec FXML et stylesheet CSS.

### Ce que cela apporte

* separation entre structure visuelle et logique
* interface moderne avec cartes, panneaux, badges, boutons et sidebars
* coherence graphique entre les modules
* front office et back office differencies

### Elements de design visibles dans le projet

* sidebar de navigation
* topbar
* cartes statistiques
* formulaires stylises
* tables de gestion
* badges d'etat
* boutons d'action
* popups et panneaux detail

Donc si on vous demande si le design est vraiment integre dans toutes les pages, la reponse est oui :

**"Chaque module a sa vue FXML, son style visuel et ses composants interactifs. Le design n'est pas ajoute a la fin, il fait partie de la structure du projet."**

---

## 20. Les boutons fonctionnels

Dans le projet, les boutons sont relies aux controllers avec des methodes `on...` ou `handle...`.

### Exemples de boutons qui marchent vraiment

#### Partie admin

Dans `AdminLayoutController.java`, on trouve des actions comme :

* `onFeed()`
* `onOverview()`
* `onTeams()`
* `onRequests()`
* `onAccounts()`
* `onProfile()`
* `onLogout()`

Ces boutons chargent les pages correspondantes dans le layout admin.

#### Partie utilisateur

Dans les controllers user, on retrouve des actions comme :

* navigation vers les equipes
* navigation vers les candidatures
* navigation vers le profil
* deconnexion
* soumission d'une candidature
* lancement du matching IA

#### Partie manager

Dans les controllers manager, on retrouve des actions comme :

* edition d'equipe
* gestion des candidatures
* analyse IA
* envoi de messages
* retour a la liste des equipes
* ajout / suppression / sauvegarde

### Ce que cela signifie

Les boutons realisent des actions concretes :

* changer de page
* enregistrer des donnees
* modifier des informations
* supprimer
* filtrer
* lancer une analyse IA
* ouvrir des details
* envoyer un message

Autrement dit :

**"Tous les boutons importants sont relies a une logique metier reelle, pas seulement a un affichage statique."**

---

## 21. Meme principe pour toutes les taches

Toutes les autres taches du projet suivent la meme methode de travail.

### A. Gestion des equipes

Pages concernees :

* `team-form-view.fxml`
* `team-dashboard-view.fxml`
* `user-teams-view.fxml`
* `user-team-detail-view.fxml`
* `admin-teams-view.fxml`
* `admin-team-form-view.fxml`

Fonctionnalites :

* CRUD equipe
* creation et modification
* detail equipe
* dashboard manager
* suivi des membres
* suivi des candidatures
* analyse IA

### B. Gestion des candidatures

Pages concernees :

* `user-apply-view.fxml`
* `user-candidatures-view.fxml`
* `candidate-management-view.fxml`

Fonctionnalites :

* postuler a une equipe
* voir ses candidatures
* filtrer
* accepter / refuser / analyser
* scoring intelligent

### C. Gestion du compte et du profil

Pages concernees :

* `account-view.fxml`
* `user-account-view.fxml`
* `admin-accounts-view.fxml`

Fonctionnalites :

* consultation du profil
* actions rapides
* edition ou moderation des comptes
* logout

### D. Boutique et commandes

Pages concernees :

* `store-view.fxml`
* `user-store-view.fxml`
* `orders-view.fxml`
* `user-orders-view.fxml`

Fonctionnalites :

* consultation des produits
* commandes
* suivi
* IA de recommandation dans le module store

### E. Tournois

Pages concernees :

* `tournaments-view.fxml`
* `user-tournaments-view.fxml`

Fonctionnalites :

* consultation des tournois
* suivi du calendrier
* lien avec le fil d'actualite

### F. Fil d'actualite

Pages concernees :

* `feed-view.fxml`
* `user-feed-view.fxml`
* `admin-feed-view.fxml`

Fonctionnalites :

* publications
* annonces
* commentaires
* interactions sociales
* notifications
* Messenger
* media
* integration API

---

## 22. Le principe d'integration complet

Quand vous dites :
**"integrer tous les pages avec tous leurs design avec les boutons qui marche et tous"**

Techniquement, cela veut dire que le projet a deja ete pense comme une application complete avec :

* des pages separees par module
* des layouts pour chaque role
* des vues FXML reliees aux controllers
* des boutons connectes a des methodes reelles
* des services qui executent la logique
* une interface visuelle coherente
* de la navigation entre toutes les pages

Donc le professeur peut comprendre que :

**"Chaque tache du projet est integree dans une vraie page, avec son design, ses champs, ses actions, ses validations et ses boutons operationnels."**

---

## 23. Phrase simple a dire a l'oral

Si on vous pose la question a l'oral, vous pouvez dire :

**"Oui, les autres taches sont aussi entierement integrees. Chaque fonctionnalite du projet possede sa propre page FXML, son controller Java, son design JavaFX, ses boutons relies a des methodes reelles, et ses services metier. Le meme principe a ete applique partout : interface, navigation, CRUD, validation, logique metier et parfois IA ou API selon le module."**
