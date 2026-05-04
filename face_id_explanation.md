# Explication détaillée du fonctionnement du Face ID

Ce document est conçu pour t'aider à te préparer pour ta validation. Il explique exactement comment la fonctionnalité Face ID a été implémentée dans ton projet Java, étape par étape, y compris les fichiers concernés, le fonctionnement de la prédiction, et la gestion du dataset.

## 1. Les Fichiers Clés de la Fonctionnalité

Le système de reconnaissance faciale est principalement géré par deux fichiers Java dans ton projet :

1. **`FaceApiCaptureDialog.java` (Le Contrôleur / L'Interface et l'IA)**
   * **Rôle :** Gère l'interface de capture (Webcam), charge les modèles d'Intelligence Artificielle, détecte le visage, vérifie la qualité de l'image (luminosité, flou) et extrait les "caractéristiques" du visage (le *descriptor*).
   * **Chemin :** `edu/ProjetPI/controllers/FaceApiCaptureDialog.java`

2. **`FaceIdAuthService.java` (Le Service / La Logique Métier)**
   * **Rôle :** Gère la logique d'authentification. Il prend le vecteur du visage extrait, le convertit en JSON pour la base de données (lors de l'inscription), ou le compare avec les visages stockés dans la base de données (lors de la connexion) en utilisant des formules mathématiques (distance euclidienne et similarité cosinus).
   * **Chemin :** `edu/ProjetPI/services/FaceIdAuthService.java`

---

## 2. Comment fonctionne la Prédiction (Le côté IA)

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

## 3. Le Processus Étape par Étape

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

## 💡 Résumé pour ta validation (Points forts à dire au prof)
* "Nous n'avons pas réinventé la roue en entraînant un modèle de zéro. Nous avons intégré l'état de l'art avec les modèles **YuNet** pour la détection et **SFace** pour la reconnaissance, en utilisant **ONNX Runtime en Java**."
* "La sécurité et la fiabilité sont assurées par des filtres algorithmiques : nous vérifions la luminosité et la netteté de l'image (variance de Laplace) avant d'accepter un visage."
* "Nous ne stockons pas d'images réelles des utilisateurs dans la base de données pour des raisons de confidentialité (RGPD). Nous stockons uniquement une représentation mathématique irréversible (un vecteur JSON)."
* "La comparaison se fait via le calcul de la Distance Euclidienne et la Similarité Cosinus."
