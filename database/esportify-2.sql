-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : dim. 03 mai 2026 à 01:08
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `esportify`
--

-- --------------------------------------------------------

--
-- Structure de la table `announcements`
--

CREATE TABLE `announcements` (
  `id` int(11) NOT NULL,
  `title` varchar(180) NOT NULL,
  `content` longtext DEFAULT NULL,
  `tag` varchar(60) NOT NULL,
  `link` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `media_type` varchar(255) NOT NULL,
  `media_filename` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `announcements`
--

INSERT INTO `announcements` (`id`, `title`, `content`, `tag`, `link`, `created_at`, `media_type`, `media_filename`) VALUES
(5, 'annonce', 'aaaaaaaaaaa', 'promo', NULL, '2026-02-10 09:50:43', 'image', 'd3224154bb54bc4f203d77fa.jpg'),
(9, 'Top 5 posts du jour', '1. ?????? (likes: 0, commentaires: 0)\n   ??????\n2. Le sport ??lectronique[2] (en anglais electronic sport, abr??g?? en e-sport), ou encore le jeu... (likes: 0, commentaires: 0)\n   Le sport ??lectronique[2] (en anglais electronic sport, abr??g?? en e-sport), ou encore le jeu vid??o de comp??tition[3], est la pratique sur internet ou en tournoi sur...\n3. ?? Texte ?? est issu du mot latin ?? textum ??, d??riv?? du verbe ?? texere ?? qui signifie ?? tisser... (likes: 0, commentaires: 0)\n   ?? Texte ?? est issu du mot latin ?? textum ??, d??riv?? du verbe ?? texere ?? qui signifie ?? tisser ??. Le mot s\'applique ?? l\'entrelacement des fibres utilis??es dans le...', 'highlight', NULL, '2026-02-23 02:37:33', 'text', NULL),
(12, 'Alertes tendances', 'Sujets chauds detectes en temps reel :\n- sport (7 mentions)\n- lectronique (5 mentions)\n- tournoi (2 mentions)\n- encore (2 mentions)\n- internet (2 mentions)\n- seau (2 mentions)\n- jeux (2 mentions)', 'trend', NULL, '2026-02-24 01:40:06', 'text', NULL),
(13, 'Alertes tendances', 'Sujets chauds detectes en temps reel :\n- sport (3 mentions)\n- lectronique (1 mentions)\n- anglais (1 mentions)\n- electronic (1 mentions)\n- encore (1 mentions)\n- comp (1 mentions)\n- tition (1 mentions)', 'trend', NULL, '2026-02-24 10:10:23', 'text', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `candidature`
--

CREATE TABLE `candidature` (
  `id` int(11) NOT NULL,
  `niveau` varchar(50) NOT NULL,
  `motivation` longtext NOT NULL,
  `statut` varchar(20) NOT NULL,
  `date_candidature` datetime NOT NULL,
  `reason` varchar(255) NOT NULL,
  `play_style` varchar(100) NOT NULL,
  `equipe_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `region` varchar(100) DEFAULT NULL,
  `disponibilite` varchar(100) DEFAULT NULL,
  `reason_ai_score` int(11) DEFAULT NULL,
  `reason_ai_label` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `candidature`
--

INSERT INTO `candidature` (`id`, `niveau`, `motivation`, `statut`, `date_candidature`, `reason`, `play_style`, `equipe_id`, `user_id`, `region`, `disponibilite`, `reason_ai_score`, `reason_ai_label`) VALUES
(5, 'Interm?diaire', 'Candidature spontan?e', 'Accept?', '2026-02-05 02:07:37', 'dcdc', 'dcdc', 44, NULL, NULL, NULL, NULL, NULL),
(6, 'D?butant', 'Candidature spontan?e', 'Accept?', '2026-02-05 14:08:14', 'aa', 'aa', 45, NULL, NULL, NULL, NULL, NULL),
(7, 'Interm?diaire', 'Candidature spontan?e', 'En attente', '2026-02-05 14:23:41', 'aa', 'aa', 46, NULL, NULL, NULL, NULL, NULL),
(8, 'Interm?diaire', 'Candidature spontan?e', 'Accept?', '2026-02-06 15:32:24', 'aaaaaaaaaaaaaaaaaaaaa', 'aaaaaa', 51, NULL, NULL, NULL, NULL, NULL),
(11, 'Interm????diaire', 'Candidature spontan????e', 'Accept??', '2026-02-06 23:10:23', 'aaaaaaaaaaaaaaaaaaaaaa', 'aaaaaaaaaaaaaaaaaaaaa', 62, NULL, NULL, NULL, NULL, NULL),
(12, 'D????butant', 'Candidature spontan????e', 'En attente', '2026-02-06 23:18:20', 'aaaaaaaaaaaaaaaa', 'aaaaaaaaaaaaaaaaaaaaa', 62, NULL, NULL, NULL, NULL, NULL),
(13, 'Confirm??', 'Candidature spontan??e', 'En attente', '2026-02-07 00:01:10', 'aaaaaaaaaaaaaaaaa', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 62, NULL, NULL, NULL, NULL, NULL),
(22, 'Confirm??', 'Candidature spontan??e', 'Accept??', '2026-02-15 17:55:39', 'oumouriiiiiiiiiiiiiii', 'awper', 63, NULL, NULL, NULL, NULL, NULL),
(27, 'Interm??diaire', 'Candidature spontan??e', 'Accept??', '2026-02-17 11:21:27', 'aaaaaaaaaaaa', 'aaaaaaaaaaaaaaaaaaaaaaaa', 75, NULL, NULL, NULL, NULL, NULL),
(28, 'Expert', 'Candidature spontan??e', 'Refus????', '2026-02-17 11:22:24', 'zzzzzzzzzzzzzzzzzzzz', 'zzzzzzzzzzzzz', 75, NULL, NULL, NULL, NULL, NULL),
(30, 'Expert', 'Candidature spontan????e', 'Refus????', '2026-02-22 02:18:51', 'Je souhaite rejoindre votre ????quipe pour progresser dans un cadre comp????titif. Mon objectif est d????????am????liorer ma discipline d????????entra????nement, ma communication en game et mon sens de la strat????gie. Je suis motiv????, r????gulier, et p', 'FPS/BATTLEROYAL', 75, 30, 'Asia', 'Moyenne', 78, 'pro'),
(33, 'Confirm????', 'Candidature spontan????e', 'En attente', '2026-02-22 12:27:55', 'Je souhaite rejoindre votre ????quipe pour progresser dans un cadre comp????titif. Mon objectif est d????????am????liorer ma discipline d????????entra????nement, ma communication en game et mon sens de la strat????gie. Je suis motiv????, r????gulier, et p', 'FPS/BATTLE ROYALE', 76, 30, 'Europe', '?????lev????e', 78, 'pro'),
(35, 'Expert', 'Candidature spontan????e', 'Accept????', '2026-02-22 14:13:37', 'Je souhaite rejoindre votre ????quipe car je partage votre vision comp????titive, votre professionnalisme et votre engagement envers la performance. Votre structure, votre organisation et votre ambition de progresser au plus haut niveau correspondent parf', 'Je suis un joueur comp????titif avec une excellente capacit???? d????????adaptation et un fort sens ', 77, 31, 'Europe', '?????lev????e', 51, 'moyen'),
(36, 'Expert', 'Candidature spontanée', 'Accepté', '2026-02-24 10:19:04', 'Je souhaite rejoindre votre équipe parce que votre projet combine performance, discipline et progression collective. Je recherche un environnement où les objectifs sont clairs, le travail est structuré (scrims, reviews, plan d’amélioration), et où chaque ', 'Je joue un style proactif, propre et orienté objectif.\r\n\r\nRôle principal: [ton rôle]\r\nPool de persos', 79, 43, 'Europe', 'Élevée', 66, 'moyen');

-- --------------------------------------------------------

--
-- Structure de la table `categorie`
--

CREATE TABLE `categorie` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `categorie`
--

INSERT INTO `categorie` (`id`, `nom`) VALUES
(2, 'pc gamer'),
(3, 'clavier'),
(4, 'carte mre'),
(5, 'souris');

-- --------------------------------------------------------

--
-- Structure de la table `chat_message`
--

CREATE TABLE `chat_message` (
  `id` int(11) NOT NULL,
  `message` longtext NOT NULL,
  `created_at` datetime NOT NULL,
  `is_read` tinyint(4) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `equipe_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `chat_message`
--

INSERT INTO `chat_message` (`id`, `message`, `created_at`, `is_read`, `user_id`, `equipe_id`) VALUES
(5, 'hi', '2026-02-17 11:25:26', 1, 23, 75),
(6, 'hi', '2026-02-22 02:32:01', 1, 12, 75),
(7, 'hi', '2026-02-22 02:32:08', 1, 12, 75),
(8, 'hi', '2026-02-22 02:39:53', 1, 12, 75),
(9, 'hi', '2026-02-22 02:48:51', 1, 12, 75),
(10, 'hi', '2026-02-22 04:00:53', 1, 30, 76),
(11, 'azzz', '2026-02-22 12:42:23', 1, 30, 75),
(12, 'hi', '2026-02-22 14:15:43', 1, 30, 77);

-- --------------------------------------------------------

--
-- Structure de la table `chat_messages`
--

CREATE TABLE `chat_messages` (
  `id` int(11) NOT NULL,
  `sender_id` int(11) NOT NULL,
  `recipient_id` int(11) NOT NULL,
  `body` longtext NOT NULL,
  `type` varchar(20) NOT NULL DEFAULT 'text',
  `call_url` varchar(255) DEFAULT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `chat_messages`
--

INSERT INTO `chat_messages` (`id`, `sender_id`, `recipient_id`, `body`, `type`, `call_url`, `is_read`, `created_at`) VALUES
(1, 12, 31, 'hi', 'text', NULL, 1, '2026-02-22 02:47:05'),
(2, 12, 32, 'Appel vocal', 'call_audio', 'https://meet.jit.si/esportify-call_audio-1771760950351', 0, '2026-02-22 12:49:10'),
(3, 31, 12, 'Appel vocal', 'call_audio', 'https://meet.jit.si/esportify-call_audio-1771767347336', 0, '2026-02-22 14:35:49'),
(4, 34, 11, 'hello', 'text', NULL, 0, '2026-02-22 16:23:04'),
(7, 42, 43, 'hello', 'text', NULL, 1, '2026-02-24 09:57:51'),
(10, 42, 43, 'aaaaaaaaaaaaaaaaaa', 'text', NULL, 1, '2026-02-28 00:29:12'),
(11, 43, 42, 'aasba', 'text', NULL, 1, '2026-02-28 00:29:56');

-- --------------------------------------------------------

--
-- Structure de la table `commande`
--

CREATE TABLE `commande` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) DEFAULT NULL,
  `prenom` varchar(255) DEFAULT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `quantite` int(11) DEFAULT NULL,
  `numtel` int(11) DEFAULT NULL,
  `statut` varchar(255) NOT NULL,
  `pays` varchar(255) DEFAULT NULL,
  `gouvernerat` varchar(255) DEFAULT NULL,
  `code_postal` varchar(20) DEFAULT NULL,
  `adresse_detail` varchar(500) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `identity_key` varchar(190) DEFAULT NULL,
  `ai_blocked` tinyint(1) NOT NULL DEFAULT 0,
  `ai_risk_score` double DEFAULT NULL,
  `ai_block_reason` varchar(500) DEFAULT NULL,
  `ai_blocked_at` datetime DEFAULT NULL COMMENT '(DC2Type:datetime_immutable)',
  `ai_block_until` datetime DEFAULT NULL COMMENT '(DC2Type:datetime_immutable)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `commande`
--

INSERT INTO `commande` (`id`, `nom`, `prenom`, `adresse`, `quantite`, `numtel`, `statut`, `pays`, `gouvernerat`, `code_postal`, `adresse_detail`, `user_id`, `identity_key`, `ai_blocked`, `ai_risk_score`, `ai_block_reason`, `ai_blocked_at`, `ai_block_until`) VALUES
(65, 'zid', 'ilyes', 'buford  city', 1, 93987977, 'paid', 'Tunisie', 'hh', '30518', 'tunis', 6, 'zid|ilyes|93987977', 0, NULL, NULL, NULL, NULL),
(66, NULL, NULL, NULL, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, NULL, '||0', 0, NULL, NULL, NULL, NULL),
(67, 'dhifallah', 'aysser', 'kelibia', 1, 12333333, 'paid', 'Tunisie', 'manouba', '2010', 'manouba hay said', 27, 'dhifallah|aysser|12333333', 0, NULL, NULL, NULL, NULL),
(68, 'ghaieth', 'aysser', 'kelibia', 1, 12345678, 'paid', 'Tunisie', 'manouba', '2010', 'manouba hay said', NULL, 'ghaieth|aysser|12345678', 0, NULL, NULL, NULL, NULL),
(69, NULL, NULL, NULL, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, NULL, '||0', 0, NULL, NULL, NULL, NULL),
(70, 'dhifallah', 'aysser', 'kelibia', 1, 11111111, 'paid', 'Tunisie', 'NABEUL', '8090', 'RUE MOUNIKER', 27, 'dhifallah|aysser|11111111', 0, NULL, NULL, NULL, NULL),
(72, 'dhifallah', 'aysser', 'kelibia', 1, 12345678, 'paid', 'Tunisie', 'aaaa', '8090', 'RUE MOUNIKER', 27, 'dhifallah|aysser|12345678', 0, NULL, NULL, NULL, NULL),
(73, 'dhifallah', 'aysser', 'kelibia', 1, 12345678, 'paid', 'Tunisie', 'aaaa', '8090', 'RUE MOUNIKER', 4, 'dhifallah|aysser|12345678', 0, NULL, NULL, NULL, NULL),
(74, 'dhifallah', 'aysser', 'kelibia', 4, 12345678, 'paid', 'Tunisie', 'aaaa', '8090', 'RUE MOUNIKER', 1, 'dhifallah|aysser|12345678', 0, NULL, NULL, NULL, NULL),
(75, 'dhifallah', 'aysser', NULL, 1, 12345678, 'draft', NULL, NULL, NULL, NULL, 4, 'dhifallah|aysser|12345678', 0, NULL, NULL, NULL, NULL),
(76, NULL, NULL, NULL, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, 4, '||0', 0, NULL, NULL, NULL, NULL),
(77, 'dhifallah', 'aysser', NULL, 1, 22222222, 'draft', NULL, NULL, NULL, NULL, 1, 'dhifallah|aysser|22222222', 0, NULL, NULL, NULL, NULL),
(78, 'aysser', 'dhifallah', 'kelibia', 1, 29787777, 'pending_payment', 'Tunisie', 'aaaa', '8090', 'RUE MOUNIKER', 28, 'aysser|dhifallah|29787777', 1, 93.93, 'Commande temporairement bloquee (score de risque: 94/100). Reessayez apres 21/02/2026 01:56.', '2026-02-21 00:40:22', '2026-02-21 01:56:22'),
(79, 'aysser', 'dhifallah', 'kelibia', 1, 29787777, 'pending_payment', 'Tunisie', 'aaaa', '8090', 'RUE MOUNIKER', 4, 'aysser|dhifallah|29787777', 1, 93.93, 'Commande temporairement bloquee (score de risque: 94/100). Reessayez apres 21/02/2026 01:56.', '2026-02-21 00:40:22', '2026-02-21 01:56:22'),
(80, NULL, NULL, NULL, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, 31, '||0', 0, NULL, NULL, NULL, NULL),
(81, 'Bouamor', 'Mohamed ghaieth', 'hay said nahj beja', 1, 56276418, 'pending_payment', 'Tunisie', 'manouba', '2010', '47', 31, 'bouamor|mohamed ghaieth|56276418', 0, NULL, NULL, NULL, NULL),
(82, NULL, NULL, NULL, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, 34, '||0', 0, NULL, NULL, NULL, NULL),
(83, 'dhifallah', 'aysser', 'fdsfds', 21, 12345678, 'pending_payment', 'Tunisie', 'jhj', '15151', 'dgfdfdgf', NULL, 'dhifallah|aysser|12345678', 0, NULL, NULL, NULL, NULL),
(84, 'a', 'a', '2121', 10, 12345678, 'pending_payment', 'sd', 'sd', '212', 'sqd', 41, 'a|a|12345678', 0, NULL, NULL, NULL, NULL),
(85, NULL, NULL, NULL, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, 43, '||0', 0, NULL, NULL, NULL, NULL),
(86, 'dhifallah', 'aysser', NULL, 1, 12345678, 'draft', NULL, NULL, NULL, NULL, 42, 'dhifallah|aysser|12345678', 1, 75.77, 'Commande temporairement bloquee (score de risque: 76/100). Reessayez apres 28/02/2026 01:11.', '2026-02-28 00:31:58', '2026-02-28 01:11:54'),
(87, 'Bensalah', 'Amen', 'hay ezzouhour 3', 1, 55555555, 'pending_payment', 'Tunisie', 'manouba', '2113', 'aaaaaaaaaaaaaaa', 42, 'bensalah|amen|55555555', 0, NULL, NULL, NULL, NULL),
(88, 'dhifallah', 'aysserkk', NULL, 1, 14725836, 'draft', NULL, NULL, NULL, NULL, 42, 'dhifallah|aysserkk|14725836', 1, 85.32, 'Commande temporairement bloquee (score de risque: 85/100). Reessayez apres 11/04/2026 17:41.', '2026-04-11 16:42:31', '2026-04-11 17:41:01'),
(89, 'dhifallah', 'aysser', 'kelibia', 3, 223445556, 'EN_ATTENTE', NULL, NULL, NULL, NULL, NULL, 'PI_CMD|2026-05-02|340.00', 0, NULL, NULL, NULL, NULL),
(90, 'dhifallah', 'aysser', 'kelibia', 1, 223335667, 'EN_LIVRAISON', 'Tunisie', 'Gouvernorat Zaghouan', '1140', 'ADR:El Ghrifette, El Ghrifet, Délégation El Fahs, Gouvernorat Zaghouan, 1140, Tunisie\nDESC:Position GPS: 36.421282, 9.887695', 61, 'PI_CMD|2026-05-02|100.00', 0, NULL, NULL, NULL, NULL),
(91, 'dhifallah', 'aysser', 'kelibia', 1, 23445666, 'PAYEE', NULL, NULL, NULL, NULL, 61, 'PI_CMD|2026-05-02|100.00', 0, NULL, NULL, NULL, NULL),
(92, 'dhifallah', 'aysser', 'kelibia', 3, 234455577, 'PAYEE', NULL, NULL, NULL, NULL, 61, 'PI_CMD|2026-05-02|1640.00', 0, NULL, NULL, NULL, NULL),
(93, NULL, NULL, NULL, 2, NULL, 'DRAFT', NULL, NULL, NULL, NULL, 61, 'PI_CMD|2026-05-02|100.00', 0, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `commande_boutique`
--

CREATE TABLE `commande_boutique` (
  `id` int(11) NOT NULL,
  `produit_id` int(11) DEFAULT NULL,
  `nom_client` varchar(100) DEFAULT NULL,
  `email_client` varchar(100) DEFAULT NULL,
  `methode_paiement` varchar(50) DEFAULT NULL,
  `quantite` int(11) DEFAULT NULL,
  `prix_total` double DEFAULT NULL,
  `date_commande` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `commande_boutique`
--

INSERT INTO `commande_boutique` (`id`, `produit_id`, `nom_client`, `email_client`, `methode_paiement`, `quantite`, `prix_total`, `date_commande`) VALUES
(1, 1, 'ilyes', 'ilyeszid33@gmail.com', 'Carte Bancaire', 140, 14000, '2026-05-01 23:13:13');

-- --------------------------------------------------------

--
-- Structure de la table `commentaires`
--

CREATE TABLE `commentaires` (
  `id` int(11) NOT NULL,
  `author_id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `content` longtext NOT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `commentaires`
--

INSERT INTO `commentaires` (`id`, `author_id`, `post_id`, `content`, `created_at`) VALUES
(2, 11, 10, 'heellloooo', '2026-02-10 00:39:37'),
(3, 4, 12, 'hi', '2026-02-10 00:40:50'),
(5, 4, 21, 'Je participe avec mon ??quipe.', '2026-02-10 01:18:32'),
(6, 2, 19, 'Vid??o test OK.', '2026-02-10 01:18:32'),
(7, 11, 18, 'Hello', '2026-02-10 01:55:52'),
(8, 4, 20, 'aaaa', '2026-02-10 02:39:31'),
(9, 4, 19, 'waw', '2026-02-10 10:02:29'),
(14, 34, 37, 'waw', '2026-02-23 03:43:50'),
(15, 42, 47, 'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMWJhYzV1eTZhY3h0NnN4bTZ2Y3R6bGN6NTcyenN6cXEwbjFoeWN6YiZlcD12MV9naWZzX3NlYXJjaCZjdD1n/3o7aD2saalBwwftBIY/giphy.gif', '2026-02-24 10:09:50');

-- --------------------------------------------------------

--
-- Structure de la table `doctrine_migration_versions`
--

CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `doctrine_migration_versions`
--

INSERT INTO `doctrine_migration_versions` (`version`, `executed_at`, `execution_time`) VALUES
('DoctrineMigrations\\Version20260127180417', '2026-02-18 14:15:54', 167),
('DoctrineMigrations\\Version20260127181104', '2026-02-18 14:15:54', 6),
('DoctrineMigrations\\Version20260131121000', '2026-02-18 14:15:54', 6),
('DoctrineMigrations\\Version20260131140000', '2026-02-18 14:15:54', 4),
('DoctrineMigrations\\Version20260131153000', '2026-02-18 14:15:54', 4),
('DoctrineMigrations\\Version20260204155237', '2026-02-18 14:15:54', 23),
('DoctrineMigrations\\Version20260204160000', '2026-02-18 14:15:54', 680),
('DoctrineMigrations\\Version20260206173814', '2026-02-18 14:15:55', 718),
('DoctrineMigrations\\Version20260206175008', '2026-02-18 14:15:55', 103),
('DoctrineMigrations\\Version20260206204412', '2026-02-18 14:15:56', 13),
('DoctrineMigrations\\Version20260207080454', '2026-02-18 14:15:56', 54),
('DoctrineMigrations\\Version20260207101449', '2026-02-18 14:15:56', 0),
('DoctrineMigrations\\Version20260207120000', '2026-02-18 14:15:56', 121),
('DoctrineMigrations\\Version20260207130000', '2026-02-18 14:15:56', 39),
('DoctrineMigrations\\Version20260207150000', '2026-02-18 14:15:56', 113),
('DoctrineMigrations\\Version20260207151000', '2026-02-18 14:15:56', 5),
('DoctrineMigrations\\Version20260207152000', '2026-02-18 14:15:56', 79),
('DoctrineMigrations\\Version20260207160000', '2026-02-18 14:15:56', 29),
('DoctrineMigrations\\Version20260207170000', '2026-02-18 14:15:56', 30),
('DoctrineMigrations\\Version20260207190000', '2026-02-18 14:20:19', 1),
('DoctrineMigrations\\Version20260207200000', '2026-02-18 14:20:19', 1),
('DoctrineMigrations\\Version20260207213000', '2026-02-18 14:20:19', 1),
('DoctrineMigrations\\Version20260208102432', '2026-02-18 14:20:19', 1),
('DoctrineMigrations\\Version20260208120000', '2026-02-18 14:20:19', 1),
('DoctrineMigrations\\Version20260208121000', '2026-02-18 14:20:19', 1),
('DoctrineMigrations\\Version20260210093947', '2026-02-18 14:20:29', 22),
('DoctrineMigrations\\Version20260211233000', '2026-02-18 14:20:29', 7),
('DoctrineMigrations\\Version20260212000500', '2026-02-18 14:20:29', 56),
('DoctrineMigrations\\Version20260212012000', '2026-02-18 14:20:50', 1),
('DoctrineMigrations\\Version20260212153000', '2026-02-18 14:20:50', 1),
('DoctrineMigrations\\Version20260213121500', '2026-02-18 14:20:50', 1),
('DoctrineMigrations\\Version20260213231500', '2026-02-18 14:20:50', 1),
('DoctrineMigrations\\Version20260215110000', '2026-02-18 14:20:50', 1),
('DoctrineMigrations\\Version20260215114000', '2026-02-18 14:20:50', 1),
('DoctrineMigrations\\Version20260216123000', '2026-02-18 14:20:50', 1),
('DoctrineMigrations\\Version20260216220000', '2026-02-18 14:20:50', 1),
('DoctrineMigrations\\Version20260218100000', '2026-02-18 14:23:39', 100),
('DoctrineMigrations\\Version20260218110000', '2026-02-18 14:29:09', 8),
('DoctrineMigrations\\Version20260218120000', '2026-02-18 14:41:47', 61),
('DoctrineMigrations\\Version20260218134543', '2026-02-18 14:46:26', 128),
('DoctrineMigrations\\Version20260218134920', '2026-02-18 14:49:54', 47),
('DoctrineMigrations\\Version20260220153000', '2026-02-20 15:50:28', 407),
('DoctrineMigrations\\Version20260220161000', '2026-02-20 16:23:44', 33),
('DoctrineMigrations\\Version20260222110000', '2026-02-22 01:50:38', 127),
('DoctrineMigrations\\Version20260222113000', '2026-02-22 01:53:38', 29),
('DoctrineMigrations\\Version20260222130000', '2026-02-22 02:55:50', 36),
('DoctrineMigrations\\Version20260222170000', '2026-02-22 03:36:43', 13),
('DoctrineMigrations\\Version20260222180000', '2026-02-22 16:19:03', 189);

-- --------------------------------------------------------

--
-- Structure de la table `equipe`
--

CREATE TABLE `equipe` (
  `id` int(11) NOT NULL,
  `nom_equipe` varchar(255) NOT NULL,
  `logo` varchar(255) DEFAULT NULL,
  `description` longtext NOT NULL,
  `date_creation` datetime NOT NULL,
  `classement` varchar(50) NOT NULL,
  `tag` varchar(255) DEFAULT NULL,
  `region` varchar(100) DEFAULT NULL,
  `max_members` int(11) NOT NULL DEFAULT 5,
  `is_private` tinyint(1) NOT NULL DEFAULT 0,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `manager_id` int(11) DEFAULT NULL,
  `suspension_reason` longtext DEFAULT NULL,
  `suspended_until` datetime DEFAULT NULL COMMENT '(DC2Type:datetime_immutable)',
  `suspension_duration_days` int(11) DEFAULT NULL,
  `discord_invite_url` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `equipe`
--

INSERT INTO `equipe` (`id`, `nom_equipe`, `logo`, `description`, `date_creation`, `classement`, `tag`, `region`, `max_members`, `is_private`, `is_active`, `manager_id`, `suspension_reason`, `suspended_until`, `suspension_duration_days`, `discord_invite_url`) VALUES
(44, 'youssef team', '6983ecf5e0b1a.jpg', 'go', '2026-02-05 00:00:00', 'Argent', 'YSF', 'Middle East', 5, 0, 1, NULL, NULL, NULL, NULL, NULL),
(45, 'aysser', NULL, 'dsde', '2026-02-05 00:00:00', 'Challenger', 'ZZD', 'Middle East', 5, 0, 1, NULL, NULL, NULL, NULL, NULL),
(46, 'ghaieth team', NULL, 'ggg', '2026-02-05 00:00:00', 'Argent', 'YSF', 'Europe', 5, 0, 1, NULL, NULL, NULL, NULL, NULL),
(50, 'ghaieth team', NULL, '2000', '2026-02-05 00:00:00', 'Argent', 'GBFGB', 'Europe', 5, 0, 1, NULL, NULL, NULL, NULL, NULL),
(51, 'youssef team', NULL, 'hhhh', '2026-02-06 00:00:00', 'Bronze', 'GBFGB', 'South America', 5, 0, 1, NULL, NULL, NULL, NULL, NULL),
(56, 'blender team', '69864cb5ec68a.png', 'olaolaola', '2026-02-06 21:19:01', 'Argent', 'BLD', 'North America', 50, 0, 1, NULL, NULL, NULL, NULL, NULL),
(62, 'Ghaieth teams', '6986666ea49d8.png', 'aaaaaaaaaaaaaaaaaaaa', '2026-02-06 00:00:00', 'Argent', 'GG', 'Asia', 50, 0, 1, NULL, NULL, NULL, NULL, NULL),
(63, 'sarra team', '6986735d8aa52.jpg', 'sarraaaaaa', '2026-02-06 00:00:00', 'Or', 'SARRA', 'Asia', 50, 0, 1, NULL, NULL, NULL, NULL, NULL),
(75, 'Alpha Team', '699a6eb3d6d3b.png', 'we are the top of gaming join us', '2026-02-17 00:00:00', 'Bronze', '#ALPHA', 'Asia', 5, 0, 1, 12, NULL, NULL, NULL, 'https://discord.gg/yGkd9kT9'),
(76, 'blender team', '699a704701841.jpg', 'together we domainate', '2026-02-22 00:00:00', 'Platine', '#BLD', 'Europe', 40, 0, 1, 32, NULL, NULL, NULL, 'https://discord.gg/yGkd9kT9'),
(77, 'Aura Team', '699aff74aef1a.jpg', '?????quipe eSport professionnelle d????di????e ???? la performance et ???? l\'excellence comp????titive. Nous r????unissons des joueurs passionn????s, engag????s et strat????giques avec pour objectif de repr????senter nos couleurs au plus haut niveau et de contribuer activement ???? la croissance de l\'eSport.', '2026-02-22 00:00:00', 'Diamant', '#AURA', 'Europe', 25, 1, 1, 30, NULL, NULL, NULL, 'https://discord.gg/MdnXUtN3'),
(79, 'test validation', '699d60dc0baaf.png', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', '2026-02-24 00:00:00', 'Bronze', 'TST', 'Middle East', 5, 0, 1, 41, NULL, NULL, NULL, 'https://discord.gg/qDPuX3sM');

-- --------------------------------------------------------

--
-- Structure de la table `event_participants`
--

CREATE TABLE `event_participants` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `event_participants`
--

INSERT INTO `event_participants` (`id`, `user_id`, `post_id`, `created_at`) VALUES
(2, 4, 21, '2026-02-10 01:18:52'),
(5, 34, 21, '2026-02-22 16:41:08'),
(6, 34, 15, '2026-02-23 03:49:18');

-- --------------------------------------------------------

--
-- Structure de la table `feed_ai_analysis`
--

CREATE TABLE `feed_ai_analysis` (
  `id` int(11) NOT NULL,
  `entity_type` varchar(20) NOT NULL,
  `entity_id` int(11) NOT NULL,
  `source_hash` varchar(64) DEFAULT NULL,
  `summary_short` longtext DEFAULT NULL,
  `summary_long` longtext DEFAULT NULL,
  `hashtags` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`hashtags`)),
  `category` varchar(50) DEFAULT NULL,
  `toxicity_score` int(11) NOT NULL DEFAULT 0,
  `hate_speech_score` int(11) NOT NULL DEFAULT 0,
  `spam_score` int(11) NOT NULL DEFAULT 0,
  `duplicate_score` int(11) NOT NULL DEFAULT 0,
  `media_risk_score` int(11) NOT NULL DEFAULT 0,
  `auto_action` varchar(20) NOT NULL DEFAULT 'allow',
  `flags` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`flags`)),
  `translations` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`translations`)),
  `updated_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `feed_ai_analysis`
--

INSERT INTO `feed_ai_analysis` (`id`, `entity_type`, `entity_id`, `source_hash`, `summary_short`, `summary_long`, `hashtags`, `category`, `toxicity_score`, `hate_speech_score`, `spam_score`, `duplicate_score`, `media_risk_score`, `auto_action`, `flags`, `translations`, `updated_at`) VALUES
(1, 'post', 2, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[\"#event\"]', 'event', 0, 0, 1, 0, 0, 'allow', '[]', NULL, '2026-02-23 02:35:56'),
(2, 'post', 1, '0d38d312b30ab299c8875a06f621487d8a9ed15ed74bbfc0b4f09f0f4dbcf0d5', 'Sdfsq.', 'Sdfsq.', '[\"#sdfsq\"]', 'general', 0, 0, 1, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:35:59'),
(3, 'post', 32, 'bf2cb58a68f684d95a3b78ef8f661c9a4e5b09e82cc8f9cc88cce90528caeb27', 'Aaaaaaaaaa.', 'Aaaaaaaaaa.', '[\"#aaaaaaaaaa\"]', 'general', 0, 0, 14, 0, 0, 'allow', '[]', NULL, '2026-02-23 02:36:02'),
(4, 'post', 31, 'bf2cb58a68f684d95a3b78ef8f661c9a4e5b09e82cc8f9cc88cce90528caeb27', 'Aaaaaaaaaa.', 'Aaaaaaaaaa.', '[\"#aaaaaaaaaa\"]', 'general', 0, 0, 14, 0, 0, 'allow', '[]', NULL, '2026-02-23 02:36:05'),
(5, 'post', 30, '30457619e65e645dcb949d0c41b70b4f4723c70d23ce60356b28f6d6a8681cd7', 'Sqd.', 'Sqd.', '[\"#sqd\"]', 'general', 0, 0, 1, 0, 0, 'allow', '[]', '{\"en\":\"en: sqd\",\"ar\":\"ar: sqd\"}', '2026-02-23 02:36:08'),
(6, 'post', 29, '9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08', 'Test.', 'Test.', '[\"#test\"]', 'general', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:36:10'),
(7, 'post', 28, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[\"#event\"]', 'event', 0, 0, 1, 0, 0, 'allow', '[]', NULL, '2026-02-23 02:36:13'),
(8, 'post', 27, '2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824', 'Hello.', 'Hello.', '[\"#hello\"]', 'general', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:36:15'),
(9, 'post', 25, '2997fcc413e7983eb154f5e7ad95b47d013ee6f68cc373baf1b2a7cd3fdd63dd', 'E-sport world cup.', 'E-sport world cup.', '[\"#tournoi\",\"#cup\",\"#sport\",\"#world\"]', 'tournoi', 0, 0, 1, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:36:17'),
(10, 'post', 24, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[\"#event\"]', 'event', 0, 0, 1, 0, 0, 'allow', '[]', NULL, '2026-02-23 02:36:20'),
(11, 'post', 23, '96eb268f9967821df1d15195649bceb3ac866e622470c1c0da2f12cde978912c', 'Fffffffffff.', 'Fffffffffff.', '[\"#fffffffffff\"]', 'general', 0, 0, 11, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:36:23'),
(12, 'post', 22, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[\"#event\"]', 'event', 0, 0, 1, 0, 0, 'allow', '[]', NULL, '2026-02-23 02:36:26'),
(13, 'post', 21, 'f3fe7efe82bc9bbcd3819a40fda50f3209e007f7d914dfbb0b4170ded3c58474', 'Event test: Tournoi du week-end TOURNOI WEEK-END Discord: https: //discord. gg/esportify.', 'Event test: Tournoi du week-end TOURNOI WEEK-END Discord: https: //discord. gg/esportify.', '[\"#tournoi\",\"#discord\",\"#end\",\"#week\",\"#esportify\",\"#event\",\"#test\"]', 'tournoi', 0, 0, 0, 60, 0, 'allow', '[]', '{\"ar\":\"Event test : \\u0628\\u0637\\u0648\\u0644\\u0629 du week-end\",\"en\":\"Event test : tournament du week-end\"}', '2026-02-23 02:36:38'),
(14, 'post', 20, '40fc17d34eb09d680579a40cc86daed60f5224f7d34a33d3e5e5072457bb9875', 'Poster image test.', 'Poster image test.', '[\"#image\",\"#poster\",\"#test\"]', 'general', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:36:35'),
(15, 'post', 19, '2748791f0266f2891692c72aca197fdc5d220dec6c6bcbc199bb9610742f6bb5', 'Scrim du vendredi - lien vid? ?o int? ?gr? ?.', 'Scrim du vendredi - lien vid? ?o int? ?gr? ?.', '[\"#int\",\"#lien\",\"#scrim\",\"#vendredi\",\"#vid\"]', 'general', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', '{\"ar\":\"Scrim du vendredi - \\u0631\\u0627\\u0628\\u0637 vid??o int??gr??.\",\"en\":\"Scrim du vendredi - link vid??o int??gr??.\"}', '2026-02-23 02:36:32'),
(16, 'post', 18, 'dfe9b268d0fb986be30584c31a0f60943c8df7dab3b19718ac0fedfdc136c107', 'Bienvenue sur E-Sportify! Premier post de test.', 'Bienvenue sur E-Sportify! Premier post de test.', '[\"#bienvenue\",\"#post\",\"#premier\",\"#sportify\",\"#test\"]', 'general', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', '{\"en\":\"Bienvenue sur E-Sportify! Premier post of test.\"}', '2026-02-23 03:55:53'),
(17, 'post', 17, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[\"#event\"]', 'event', 0, 0, 1, 0, 0, 'allow', '[]', NULL, '2026-02-23 02:36:40'),
(18, 'post', 16, '2689367b205c16ce32ed4200942b8b8b1e262dfc70d9bc9fbc77c49699a4f1df', 'Ok.', 'Ok.', '[]', 'general', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:36:42'),
(19, 'post', 15, 'bea33c2cfef25f2f030023a31414818dedc00848f3a5c3ceaa000b6d598ae3dc', 'Aaaa sssss.', 'Aaaa sssss.', '[\"#aaaa\",\"#sssss\"]', 'general', 0, 0, 0, 0, 0, 'allow', '[]', NULL, '2026-02-23 02:36:44'),
(20, 'post', 14, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[\"#event\"]', 'event', 0, 0, 1, 0, 0, 'allow', '[]', NULL, '2026-02-23 02:36:47'),
(21, 'post', 13, '17e2cd856421d5e5d7ab65da7a3dc8c0293e36f70e957fdc13d797aaf81f9dfa', 'Ok ok.', 'Ok ok.', '[]', 'general', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:36:49'),
(22, 'post', 12, '2b9fcaa8efb8464b78992a82a404664ba1730f4baf1530c4f14cdb185ff6c1aa', 'Okii.', 'Okii.', '[\"#okii\"]', 'general', 0, 0, 1, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:36:52'),
(23, 'post', 11, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[\"#event\"]', 'event', 0, 0, 1, 0, 0, 'allow', '[]', NULL, '2026-02-23 02:36:54'),
(24, 'post', 10, 'e46240714b5db3a23eee60479a623efba4d633d27fe4f03c904b9e219a7fbe60', 'Aaaaaaa.', 'Aaaaaaa.', '[\"#aaaaaaa\"]', 'general', 0, 0, 11, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:36:56'),
(25, 'post', 9, '61be55a8e2f6b4e172338bddf184d6dbee29c98853e0a0485ecee7f27b9af0b4', 'Aaaa.', 'Aaaa.', '[\"#aaaa\"]', 'general', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:36:59'),
(26, 'post', 8, '2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824', 'Hello.', 'Hello.', '[\"#hello\"]', 'general', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:37:02'),
(27, 'post', 7, '9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08', 'test', 'test', '[\"#test\",\"#esport\"]', 'general', 0, 0, 0, 0, 0, 'allow', '[]', '{\"en\":\"en: test\",\"ar\":\"ar: test\"}', '2026-02-23 01:59:23'),
(28, 'post', 3, '3478ec33c746bb4fe7319974e39dc5383dde5baf529da8f084be07ec9e77499a', 'Openning', 'Openning', '[\"#openning\",\"#esport\"]', 'general', 0, 0, 0, 0, 0, 'allow', '[]', '{\"en\":\"en: Openning\",\"ar\":\"ar: Openning\"}', '2026-02-23 01:59:32'),
(29, 'comment', 13, '653d544cbec26541c4a82b4a128124605384172c39a750983db00e1b140b1043', 'Https: //media. giphy. com/media/v1.', 'Https: //media. giphy. com/media/v1. Y2lkPTc5MGI3NjExbDRxdjd0ZXl4YzY5dHd5c3hmMmptb2VjMjUxN2diZWt3cDA4ZnF2ZSZlcD12MV9naWZzX3NlYXJjaCZjdD1n/MdA16VIoXKKxNE8Stk/giphy. gif.', '[\"#giphy\",\"#media\",\"#gif\",\"#mda16vioxkkxne8stk\",\"#y2lkptc5mgi3njexbdrxdjd0zxl4yzy5dhd5c3hmmmptb2vjmjuxn2dizwt3cda4znf2zszlcd12mv9nawzzx3nlyxjjaczjdd1n\"]', 'general', 0, 0, 0, 0, 0, 'allow', '[]', NULL, '2026-02-23 02:37:05'),
(30, 'comment', 9, '23384e6ae85686b7926ebcbda69e9c0a6d1bb1a2fa4d9cd6fc7b7e58f3c906af', 'Waw.', 'Waw.', '[\"#waw\"]', 'general', 0, 0, 1, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', '{\"fr\":\"Waw.\"}', '2026-02-23 04:06:21'),
(31, 'comment', 8, '61be55a8e2f6b4e172338bddf184d6dbee29c98853e0a0485ecee7f27b9af0b4', 'Aaaa.', 'Aaaa.', '[\"#aaaa\"]', 'general', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:37:11'),
(32, 'comment', 7, '185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969', 'Hello.', 'Hello.', '[\"#hello\"]', 'general', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:37:14'),
(33, 'comment', 6, '351daf8391d64e2cae57342c4e323e533a4293417123c063a0ad2cb04999438b', 'Vid? ?o test OK.', 'Vid? ?o test OK.', '[\"#test\",\"#vid\"]', 'general', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:37:22'),
(34, 'comment', 5, '831994725d5ee69dfb58752bfa6b8066ee0f3115fe802d8498e54573d6fa98db', 'Je participe avec mon? ?quipe.', 'Je participe avec mon? ?quipe.', '[\"#mon\",\"#participe\",\"#quipe\"]', 'general', 0, 0, 1, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', '{\"fr\":\"Je participe avec mon? ?quipe.\",\"ar\":\"\\u0627\\u0646\\u0627 participe \\u0645\\u0639 mon? ?quipe.\",\"en\":\"I participe with mon? ?quipe.\"}', '2026-02-23 03:15:24'),
(35, 'comment', 4, '3002b270d6a2769816c2cc70f634763e706e61fb6316931fe608d1f944aa6c69', 'Super id? ?e pour le tournoi!', 'Super id? ?e pour le tournoi!', '[\"#tournoi\",\"#super\"]', 'tournoi', 0, 0, 0, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', '{\"ar\":\"Super id? ?e \\u0644 le \\u0628\\u0637\\u0648\\u0644\\u0629!\",\"en\":\"Super id? ?e for le tournament!\",\"fr\":\"Super id??e pour le tournoi !\"}', '2026-02-23 03:15:37'),
(36, 'comment', 3, '8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4', 'Hi.', 'Hi.', '[]', 'general', 0, 0, 1, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:37:25'),
(37, 'comment', 2, 'cf71539631d21516ae1d09b9d15212890cecab88525de731bcc00f6e9c7ea42d', 'Heellloooo.', 'Heellloooo.', '[\"#heellloooo\"]', 'general', 0, 0, 1, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', NULL, '2026-02-23 02:37:28'),
(44, 'post', 33, '6ac3c336e4094835293a3fed8a4b5fedde1b5e2626d9838fed50693bba00af0e', 'fuck', 'fuck', '[\"#fuck\",\"#esport\",\"#image\"]', 'general', 15, 0, 0, 0, 0, 'allow', '[]', NULL, '2026-02-22 16:25:02'),
(48, 'post', 34, '6ac3c336e4094835293a3fed8a4b5fedde1b5e2626d9838fed50693bba00af0e', 'fuck', 'fuck', '[\"#fuck\",\"#esport\",\"#image\"]', 'general', 15, 0, 0, 100, 0, 'block', '[\"doublon_probable\",\"action_blocage_auto\"]', NULL, '2026-02-22 16:50:03'),
(65, 'post', 35, 'c67dce3562e00c2f69e3237db88a807cbcaf51486f0e2be67481975390b848ec', '?? Texte ?? est issu du mot latin ?? textum ??, d??riv?? du verbe ?? texere ?? qui signifie ?? tisser ??.', '?? Texte ?? est issu du mot latin ?? textum ??, d??riv?? du verbe ?? texere ?? qui signifie ?? tisser ??.', '[\"#event\",\"#cle\",\"#mot\",\"#texte\",\"#chez\",\"#exemple\",\"#sens\",\"#texere\"]', 'event', 0, 0, 0, 100, 0, 'allow', '[\"duplicate\"]', '{\"en\":\"\\u00ab Texte \\u00bb est issu of the mot latin \\u00ab textum \\u00bb, d\\u00e9riv\\u00e9 of the verbe \\u00ab texere \\u00bb qui signifie \\u00ab tisser \\u00bb. Le mot s\'applique at l\'entrelacement some fibres utilis\\u00e9es in le tissage, voir par exemple Ovide: \\u00ab Quo super iniecit textum rude sedula Baucis = (un si\\u00e8ge) sur lequel Baucis empress\\u00e9e avait jet\\u00e9 un tissu grossier \\u00bb[2] ou au tressage (exemple chez Martial \\u00ab Vimineum textum = panier d\'osier tress\\u00e9 \\u00bb). Le verbe at aussi le sens large of construire comme in \\u00ab basilicam texere = construire une basilique \\u00bb chez Cic\\u00e9ron[3]. Le sens figur\\u00e9 d\'\\u00e9l\\u00e9ments of langage organis\\u00e9s et encha\\u00een\\u00e9s appara\\u00eet before l\'Empire romain: il d\\u00e9signe un agencement particulier of the discours. Exemple: \\u00ab epistolas texere = composer some \\u00e9p\\u00eetres \\u00bb - Cic\\u00e9ron (Ier si\\u00e8cle av. J. -C. )[4] ou plus nettement chez Quintilien (Ier si\\u00e8cle apr. J. -C. ): \\u00ab verba in textu jungantur = l\'agencement some mots in la phrase \\u00bb[5]. The formes anciennes of the Moyen \\u00c2ge d\\u00e9signent au XIIe si\\u00e8cle le volume qui contient le texte sacr\\u00e9 some \\u00c9vangiles, puis au XIIIe si\\u00e8cle, le texte original d\'un livre saint ou some propos of quelqu\'un. Au XVIIe si\\u00e8cle le mot s\\u2019applique au passage d\'un ouvrage pris comme r\\u00e9f\\u00e9rence et au d\\u00e9but of the XIXe si\\u00e8cle le mot texte at son sens g\\u00e9n\\u00e9ral d\'\\u00ab \\u00e9crit \\u00bb[6].\",\"ar\":\"\\u00ab Texte \\u00bb est issu du mot latin \\u00ab textum \\u00bb, d\\u00e9riv\\u00e9 du verbe \\u00ab texere \\u00bb qui signifie \\u00ab tisser \\u00bb. Le mot s\'applique \\u00e0 l\'entrelacement des fibres utilis\\u00e9es dans le tissage, voir par exemple Ovide: \\u00ab Quo super iniecit textum rude sedula Baucis = (un si\\u00e8ge) sur lequel Baucis empress\\u00e9e avait jet\\u00e9 un tissu grossier \\u00bb[2] ou au tressage (exemple chez Martial \\u00ab Vimineum textum = panier d\'osier tress\\u00e9 \\u00bb). Le verbe a aussi le sens large de construire comme dans \\u00ab basilicam texere = construire une basilique \\u00bb chez Cic\\u00e9ron[3]. Le sens figur\\u00e9 d\'\\u00e9l\\u00e9ments de langage organis\\u00e9s et encha\\u00een\\u00e9s appara\\u00eet avant l\'Empire romain: il d\\u00e9signe un agencement particulier du discours. Exemple: \\u00ab epistolas texere = composer des \\u00e9p\\u00eetres \\u00bb - Cic\\u00e9ron (Ier si\\u00e8cle av. J. -C. )[4] ou plus nettement chez Quintilien (Ier si\\u00e8cle apr. J. -C. ): \\u00ab verba in textu jungantur = l\'agencement des mots dans la phrase \\u00bb[5]. Les formes anciennes du Moyen \\u00c2ge d\\u00e9signent au XIIe si\\u00e8cle le volume qui contient le texte sacr\\u00e9 des \\u00c9vangiles, puis au XIIIe si\\u00e8cle, le texte original d\'un livre saint ou des propos de quelqu\'un. Au XVIIe si\\u00e8cle le mot s\\u2019applique au passage d\'un ouvrage pris comme r\\u00e9f\\u00e9rence et au d\\u00e9but du XIXe si\\u00e8cle le mot texte a son sens g\\u00e9n\\u00e9ral d\'\\u00ab \\u00e9crit \\u00bb[6].\",\"fr\":\"\\u00ab Texte \\u00bb est issu du mot latin \\u00ab textum \\u00bb, d\\u00e9riv\\u00e9 du verbe \\u00ab texere \\u00bb qui signifie \\u00ab tisser \\u00bb. Le mot s\'applique \\u00e0 l\'entrelacement des fibres utilis\\u00e9es dans le tissage, voir par exemple Ovide : \\u00ab Quo super iniecit textum rude sedula Baucis = (un si\\u00e8ge) sur lequel Baucis empress\\u00e9e avait jet\\u00e9 un tissu grossier \\u00bb[2] ou au tressage (exemple chez Martial \\u00ab Vimineum textum = panier d\'osier tress\\u00e9 \\u00bb). Le verbe a aussi le sens large de construire comme dans \\u00ab basilicam texere = construire une basilique \\u00bb chez Cic\\u00e9ron[3]. Le sens figur\\u00e9 d\'\\u00e9l\\u00e9ments de langage organis\\u00e9s et encha\\u00een\\u00e9s appara\\u00eet avant l\'Empire romain : il d\\u00e9signe un agencement particulier du discours. Exemple : \\u00ab epistolas texere = composer des \\u00e9p\\u00eetres \\u00bb - Cic\\u00e9ron (Ier si\\u00e8cle av. J.-C.)[4] ou plus nettement chez Quintilien (Ier si\\u00e8cle apr. J.-C.) : \\u00ab verba in textu jungantur = l\'agencement des mots dans la phrase \\u00bb[5]. Les formes anciennes du Moyen \\u00c2ge d\\u00e9signent au XIIe si\\u00e8cle le volume qui contient le texte sacr\\u00e9 des \\u00c9vangiles, puis au XIIIe si\\u00e8cle, le texte original d\'un livre saint ou des propos de quelqu\'un. Au XVIIe si\\u00e8cle le mot s\\u2019applique au passage d\'un ouvrage pris comme r\\u00e9f\\u00e9rence et au d\\u00e9but du XIXe si\\u00e8cle le mot texte a son sens g\\u00e9n\\u00e9ral d\'\\u00ab \\u00e9crit \\u00bb[6].\"}', '2026-02-23 03:55:14'),
(66, 'post', 36, '002710705aea05fb924a62c2a99db5e985d5dbf828c15e9ed60698d7a5b6673b', 'Le sport ??lectronique[2] (en anglais electronic sport, abr??g?? en e-sport), ou encore le jeu vid??o de comp??tition[3], est la pratique sur internet ou en tourn. ..', 'Le sport ??lectronique[2] (en anglais electronic sport, abr??g?? en e-sport), ou encore le jeu vid??o de comp??tition[3], est la pratique sur internet ou en tournoi sur r??seau local d\'un jeu vid??o, seul ou en ??quipe, sur ordinateur ou sur console de jeux vid??o.', '[\"#tournoi\",\"#sport\",\"#lectronique\",\"#par\",\"#ann\",\"#vid\",\"#encore\",\"#internet\"]', 'tournoi', 0, 0, 6, 100, 0, 'block', '[\"duplicate\",\"blocked_auto\"]', '{\"en\":\"Le sport? ?lectronique[2] (en anglais electronic sport, abr? ?g? ? en e-sport), ou encore le jeu vid? ?o of comp? ?tition[3], est la pratique sur internet ou en tournament sur r? ?seau local d\'un jeu vid? ?o, seul ou en? ?quipe, sur ordinateur ou sur console of jeux vid? ?o. L\'essor of the sport? ?lectronique commence? ? la fin some ann? ?es 1980 with the premiers jeux en r? ?seau multijoueur et, ?? partir some ann? ?es 1990, sur internet. Au cours some ann? ?es 2000 et 2010, le sport? ?lectronique acquiert of plus en plus of notori? ?t? ?, et some tournois dot? ?s of prix importants commencent? ? appara? ?tre sur la sc? ?ne internationale. The meilleurs adeptes mondiaux of the sport? ?lectronique se rencontrent lors of tournois officiels, organis? ?s par exemple par l\'eSports World Convention (ESWC), la Major League Gaming (MLG), la Cyberathlete Professional League (CPL) ou encore l\'Evolution Championship Series (EVO), for ne citer que quelques organisateurs. Le sport? ?lectronique? ? haut level est souvent financ? ? par the sponsors et par the revenus g? ?n? ?r? ?s par la diffusion en streaming qui incluent souvent of la publicit? ?.\",\"fr\":\"Le sport? ?lectronique[2] (en anglais electronic sport, abr? ?g? ? en e-sport), ou encore le jeu vid? ?o de comp? ?tition[3], est la pratique sur internet ou en tournoi sur r? ?seau local d\'un jeu vid? ?o, seul ou en? ?quipe, sur ordinateur ou sur console de jeux vid? ?o. L\'essor du sport? ?lectronique commence? ? la fin des ann? ?es 1980 avec les premiers jeux en r? ?seau multijoueur et, ?? partir des ann? ?es 1990, sur internet. Au cours des ann? ?es 2000 et 2010, le sport? ?lectronique acquiert de plus en plus de notori? ?t? ?, et des tournois dot? ?s de prix importants commencent? ? appara? ?tre sur la sc? ?ne internationale. Les meilleurs adeptes mondiaux du sport? ?lectronique se rencontrent lors de tournois officiels, organis? ?s par exemple par l\'eSports World Convention (ESWC), la Major League Gaming (MLG), la Cyberathlete Professional League (CPL) ou encore l\'Evolution Championship Series (EVO), pour ne citer que quelques organisateurs. Le sport? ?lectronique? ? haut niveau est souvent financ? ? par les sponsors et par les revenus g? ?n? ?r? ?s par la diffusion en streaming qui incluent souvent de la publicit? ?.\",\"ar\":\"Le sport? ?lectronique[2] (en anglais electronic sport, abr? ?g? ? en e-sport), ou encore le jeu vid? ?o de comp? ?tition[3], est la pratique sur internet ou en \\u0628\\u0637\\u0648\\u0644\\u0629 sur r? ?seau local d\'un jeu vid? ?o, seul ou en? ?quipe, sur ordinateur ou sur console de jeux vid? ?o.\"}', '2026-02-24 01:36:56'),
(67, 'post', 37, '355b7226e20cb564774ef99442e77ac003100624b42b5a92944f0dad75b126b0', '??????.', '??????.', '[\"#event\"]', 'event', 0, 0, 1, 0, 0, 'allow', '[]', NULL, '2026-02-23 02:35:48'),
(68, 'post', 38, 'eab6ca232ed4585f84a7f083472c910f9962f9acd5811710f7b303c399a0009f', 'Bonjour, annonce tournoi aujourd hui: equipe joueurs, resultat victoire, commentaire publication.', 'Bonjour, annonce tournoi aujourd hui: equipe joueurs, resultat victoire, commentaire publication.', '[\"#tournoi\",\"#annonce\",\"#bonjour\",\"#commentaire\",\"#equipe\",\"#hui\",\"#joueurs\",\"#publication\"]', 'tournoi', 4, 3, 6, 1, 0, 'allow', '[]', '{\"en\":\"Hello, announcement tournament today: team players, result victory, comment post.\",\"ar\":\"\\u0645\\u0631\\u062d\\u0628\\u0627, \\u0625\\u0639\\u0644\\u0627\\u0646 \\u0628\\u0637\\u0648\\u0644\\u0629 aujourd hui: \\u0641\\u0631\\u064a\\u0642 \\u0644\\u0627\\u0639\\u0628\\u064a\\u0646, \\u0646\\u062a\\u064a\\u062c\\u0629 \\u0641\\u0648\\u0632, \\u062a\\u0639\\u0644\\u064a\\u0642 \\u0645\\u0646\\u0634\\u0648\\u0631.\"}', '2026-02-24 10:03:04'),
(69, 'comment', 14, '23384e6ae85686b7926ebcbda69e9c0a6d1bb1a2fa4d9cd6fc7b7e58f3c906af', 'Waw.', 'Waw.', '[\"#waw\"]', 'general', 5, 3, 1, 0, 0, 'allow', '[]', NULL, '2026-02-23 03:43:55'),
(70, 'post', 39, '0017dea7770f7ecff7ab3c20506546129e96bdeba2f544bb8e5414eb79786122', 'Pub.', 'Pub.', '[\"#pub\"]', 'general', 7, 3, 7, 0, 0, 'allow', '[]', NULL, '2026-02-23 04:00:39'),
(71, 'post', 40, '759cfde265aaddb6f728ed08d97862bbd9b56fd39de97a049c640b4c5b70aac9', 'Test22.', 'Test22.', '[\"#test22\"]', 'general', 5, 1, 1, 0, 0, 'allow', '[]', '{\"ar\":\"Test22\"}', '2026-02-24 03:27:55'),
(72, 'post', 41, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[]', 'general', 0, 0, 0, 0, 0, 'allow', '[]', NULL, '2026-02-24 02:55:27'),
(73, 'post', 42, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[]', 'general', 0, 0, 0, 0, 0, 'allow', '[]', NULL, '2026-02-24 03:01:43'),
(74, 'post', 43, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[]', 'general', 0, 0, 0, 0, 0, 'allow', '[]', NULL, '2026-02-24 03:04:31'),
(75, 'post', 44, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[]', 'general', 0, 0, 0, 0, 0, 'allow', '[]', NULL, '2026-02-24 03:07:10'),
(76, 'post', 45, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[]', 'general', 0, 0, 0, 0, 0, 'allow', '[]', NULL, '2026-02-24 03:07:54'),
(77, 'post', 46, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '', '', '[]', 'general', 0, 0, 0, 0, 0, 'allow', '[]', NULL, '2026-02-24 03:21:28'),
(78, 'post', 47, 'da6369b7d1b283db60ca89b565fc0029e320758a9c8ddbbeb903e9188c2916ef', 'Le sport? ?lectronique[2] (en anglais electronic sport, abr? ?g? ? en e-sport), ou encore le jeu vid? ?o de comp?', 'Le sport? ?lectronique[2] (en anglais electronic sport, abr? ?g? ? en e-sport), ou encore le jeu vid? ?o de comp? ?tition[3], est la pratique sur internet ou en tournoi sur r? N\'hesitez pas a donner votre avis et partager vos retours en commentaire.', '[\"#tournoi\",\"#sport\",\"#abr\",\"#anglais\",\"#avis\",\"#commentaire\",\"#comp\",\"#donner\"]', 'tournoi', 0, 0, 0, 0, 0, 'allow', '[]', NULL, '2026-02-24 10:09:23'),
(79, 'comment', 15, '0c96b0df219a8856fdd3369d8532cd1cfa38720dc35e887a846ead948bc504cd', 'Https: //media. giphy. com/media/v1.', 'Https: //media. giphy. com/media/v1. Y2lkPTc5MGI3NjExMWJhYzV1eTZhY3h0NnN4bTZ2Y3R6bGN6NTcyenN6cXEwbjFoeWN6YiZlcD12MV9naWZzX3NlYXJjaCZjdD1n/3o7aD2saalBwwftBIY/giphy. gif.', '[\"#giphy\",\"#media\",\"#3o7ad2saalbwwftbiy\",\"#gif\",\"#y2lkptc5mgi3njexmwjhyzv1etzhy3h0nnn4btz2y3r6bgn6ntcyenn6cxewbjfoewn6yizlcd12mv9nawzzx3nlyxjjaczjdd1n\"]', 'general', 0, 0, 0, 0, 0, 'allow', '[]', NULL, '2026-02-24 10:09:50');

-- --------------------------------------------------------

--
-- Structure de la table `lignecommande`
--

CREATE TABLE `lignecommande` (
  `id` int(11) NOT NULL,
  `commandeId` int(11) NOT NULL,
  `produitId` int(11) NOT NULL,
  `quantite` int(11) NOT NULL,
  `prixUnitaire` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `lignecommande`
--

INSERT INTO `lignecommande` (`id`, `commandeId`, `produitId`, `quantite`, `prixUnitaire`) VALUES
(1, 89, 1, 2, 100),
(2, 89, 3, 1, 140),
(4, 90, 1, 1, 100),
(6, 91, 1, 1, 100),
(10, 92, 4, 1, 140),
(11, 92, 1, 1, 100),
(12, 92, 6, 1, 1400),
(14, 93, 1, 2, 100);

-- --------------------------------------------------------

--
-- Structure de la table `ligne_commande`
--

CREATE TABLE `ligne_commande` (
  `id` int(11) NOT NULL,
  `quantite` int(11) NOT NULL,
  `prix` int(11) NOT NULL,
  `commande_id` int(11) NOT NULL,
  `produit_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `ligne_commande`
--

INSERT INTO `ligne_commande` (`id`, `quantite`, `prix`, `commande_id`, `produit_id`) VALUES
(104, 110, 120, 65, 10),
(105, 1, 100, 66, 1),
(106, 1, 100, 67, 1),
(107, 41, 1405, 68, 5),
(108, 2, 100, 68, 1),
(109, 1, 120, 68, 10),
(110, 2, 140, 68, 3),
(111, 1, 120, 69, 11),
(112, 1, 100, 70, 1),
(113, 2, 15, 70, 12),
(114, 2, 120, 70, 10),
(115, 1, 140, 70, 3),
(116, 1, 120, 70, 11),
(119, 1, 100, 72, 1),
(120, 5, 100, 73, 1),
(121, 1, 140, 73, 3),
(122, 2, 1405, 73, 5),
(123, 4, 120, 73, 11),
(124, 1, 16, 73, 13),
(125, 4, 140, 74, 3),
(127, 1, 100, 75, 1),
(128, 1, 100, 76, 1),
(129, 1, 100, 77, 1),
(130, 1, 100, 78, 1),
(131, 2, 100, 79, 1),
(132, 4, 140, 79, 3),
(133, 3, 1400, 79, 6),
(134, 1, 15, 79, 12),
(135, 3, 1405, 79, 5),
(136, 2, 120, 79, 11),
(137, 1, 16, 79, 13),
(138, 1, 100, 80, 1),
(139, 1, 140, 81, 3),
(141, 21, 140, 83, 3),
(142, 10, 1405, 84, 5),
(143, 1, 100, 85, 1),
(144, 1, 100, 86, 1),
(145, 1, 100, 87, 1),
(146, 1, 100, 88, 1);

-- --------------------------------------------------------

--
-- Structure de la table `likes`
--

CREATE TABLE `likes` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `likes`
--

INSERT INTO `likes` (`id`, `user_id`, `post_id`, `created_at`) VALUES
(2, 11, 8, '2026-02-10 00:37:38'),
(3, 11, 10, '2026-02-10 00:39:18'),
(4, 4, 12, '2026-02-10 00:40:51'),
(5, 4, 10, '2026-02-10 00:40:55'),
(6, 4, 11, '2026-02-10 00:45:07'),
(7, 11, 15, '2026-02-10 01:01:24'),
(8, 11, 17, '2026-02-10 01:04:10'),
(10, 4, 21, '2026-02-10 01:18:39'),
(11, 2, 19, '2026-02-10 01:18:39'),
(12, 11, 18, '2026-02-10 01:55:42'),
(13, 4, 20, '2026-02-10 02:39:35'),
(14, 4, 23, '2026-02-10 10:02:05'),
(16, 12, 27, '2026-02-10 11:45:52'),
(21, 22, 31, '2026-02-16 20:39:05'),
(22, 12, 32, '2026-02-17 11:42:31'),
(23, 34, 21, '2026-02-22 16:26:22'),
(26, 42, 47, '2026-02-24 10:09:29'),
(27, 42, 32, '2026-03-15 01:01:18');

-- --------------------------------------------------------

--
-- Structure de la table `manager_request`
--

CREATE TABLE `manager_request` (
  `id` int(11) NOT NULL,
  `motivation` longtext NOT NULL,
  `status` varchar(20) NOT NULL,
  `created_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `nom` varchar(255) DEFAULT NULL,
  `experience` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `manager_request`
--

INSERT INTO `manager_request` (`id`, `motivation`, `status`, `created_at`, `user_id`, `nom`, `experience`) VALUES
(2, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'accepted', '2026-02-07 17:17:51', 4, 'ghaieth', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaa'),
(3, 'test', 'accepted', '2026-02-09 15:04:45', 11, 'Mohamed', 'Test'),
(4, 'test', 'accepted', '2026-02-09 15:06:10', 11, 'Mohamed bouzid', 'test test test etc'),
(5, 'sssssss', 'accepted', '2026-02-09 23:31:09', 11, 'aaa', 'zzz'),
(6, 'aaaaaaaaaaaaaaaaaaaaaaaa', 'accepted', '2026-02-10 03:58:04', 4, 'aaaaa', 'aaaaaaaaaaaaaaa'),
(8, 'aaaaaaaaaaaaaaaaaaaaaaaaaa', 'pending', '2026-02-10 11:53:16', 12, 'Ben Salah', 'aaaaaaaaaaaaaaaaaaa'),
(9, 'aaaaaaaaaaaaaaaaaaaaa', 'accepted', '2026-02-22 03:53:13', 32, 'gaaloul', 'competitve'),
(10, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'pending', '2026-02-22 14:03:10', 30, 'ghaieth', 'aaaaaaaaaaaaaaaaaaaaaaaaaaa');

-- --------------------------------------------------------

--
-- Structure de la table `messenger_messages`
--

CREATE TABLE `messenger_messages` (
  `id` bigint(20) NOT NULL,
  `body` longtext NOT NULL,
  `headers` longtext NOT NULL,
  `queue_name` varchar(190) NOT NULL,
  `created_at` datetime NOT NULL,
  `available_at` datetime NOT NULL,
  `delivered_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `messenger_messages`
--

INSERT INTO `messenger_messages` (`id`, `body`, `headers`, `queue_name`, `created_at`, `available_at`, `delivered_at`) VALUES
(1, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;s:64:\\\"amnesia vient de cr??er un ??v??nement : aaaa (25/02/2026 01:01)\\\";i:1;s:5:\\\"utf-8\\\";i:2;N;i:3;N;i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"noreply@esportify.local\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:43:\\\"Nouvel ??v??nement dans le fil d\\\'actualit??\\\";}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:15:\\\"admin@admin.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-02-10 00:01:17', '2026-02-10 00:01:17', NULL),
(2, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;s:64:\\\"amnesia vient de cr??er un ??v??nement : aaaa (25/02/2026 01:01)\\\";i:1;s:5:\\\"utf-8\\\";i:2;N;i:3;N;i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"noreply@esportify.local\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:43:\\\"Nouvel ??v??nement dans le fil d\\\'actualit??\\\";}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:26:\\\"ghaiethbouamor23@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-02-10 00:01:17', '2026-02-10 00:01:17', NULL),
(3, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;s:64:\\\"amnesia vient de cr??er un ??v??nement : aaaa (25/02/2026 01:01)\\\";i:1;s:5:\\\"utf-8\\\";i:2;N;i:3;N;i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"noreply@esportify.local\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:43:\\\"Nouvel ??v??nement dans le fil d\\\'actualit??\\\";}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:27:\\\"ghaiethbouamor013@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-02-10 00:01:17', '2026-02-10 00:01:17', NULL),
(4, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;s:64:\\\"amnesia vient de cr??er un ??v??nement : aaaa (25/02/2026 01:01)\\\";i:1;s:5:\\\"utf-8\\\";i:2;N;i:3;N;i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"noreply@esportify.local\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:43:\\\"Nouvel ??v??nement dans le fil d\\\'actualit??\\\";}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:27:\\\"ghaiethbouamor773@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-02-10 00:01:17', '2026-02-10 00:01:17', NULL),
(5, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;s:64:\\\"amnesia vient de cr??er un ??v??nement : aaaa (25/02/2026 01:01)\\\";i:1;s:5:\\\"utf-8\\\";i:2;N;i:3;N;i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"noreply@esportify.local\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:43:\\\"Nouvel ??v??nement dans le fil d\\\'actualit??\\\";}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:18:\\\"ilyeszid@esprit.tn\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-02-10 00:01:17', '2026-02-10 00:01:17', NULL),
(6, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;s:64:\\\"amnesia vient de cr??er un ??v??nement : aaaa (25/02/2026 01:01)\\\";i:1;s:5:\\\"utf-8\\\";i:2;N;i:3;N;i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"noreply@esportify.local\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:43:\\\"Nouvel ??v??nement dans le fil d\\\'actualit??\\\";}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:19:\\\"ilyes.zid@esprit.tn\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-02-10 00:01:17', '2026-02-10 00:01:17', NULL),
(7, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;s:64:\\\"amnesia vient de cr??er un ??v??nement : aaaa (25/02/2026 01:01)\\\";i:1;s:5:\\\"utf-8\\\";i:2;N;i:3;N;i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"noreply@esportify.local\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:43:\\\"Nouvel ??v??nement dans le fil d\\\'actualit??\\\";}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:20:\\\"ilyeszid33@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-02-10 00:01:17', '2026-02-10 00:01:17', NULL),
(8, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;s:64:\\\"amnesia vient de cr??er un ??v??nement : aaaa (25/02/2026 01:01)\\\";i:1;s:5:\\\"utf-8\\\";i:2;N;i:3;N;i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"noreply@esportify.local\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:43:\\\"Nouvel ??v??nement dans le fil d\\\'actualit??\\\";}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:17:\\\"ilyes14@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-02-10 00:01:17', '2026-02-10 00:01:17', NULL),
(9, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;s:64:\\\"amnesia vient de cr??er un ??v??nement : aaaa (25/02/2026 01:01)\\\";i:1;s:5:\\\"utf-8\\\";i:2;N;i:3;N;i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"noreply@esportify.local\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:43:\\\"Nouvel ??v??nement dans le fil d\\\'actualit??\\\";}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:24:\\\"youssefchleghm@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-02-10 00:01:17', '2026-02-10 00:01:17', NULL),
(10, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;s:64:\\\"amnesia vient de cr??er un ??v??nement : aaaa (25/02/2026 01:01)\\\";i:1;s:5:\\\"utf-8\\\";i:2;N;i:3;N;i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"noreply@esportify.local\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:43:\\\"Nouvel ??v??nement dans le fil d\\\'actualit??\\\";}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:14:\\\"amen@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-02-10 00:01:17', '2026-02-10 00:01:17', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `notifications`
--

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `recipient_id` int(11) NOT NULL,
  `type` varchar(80) NOT NULL,
  `title` varchar(180) NOT NULL,
  `message` longtext NOT NULL,
  `link` varchar(255) DEFAULT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `notifications`
--

INSERT INTO `notifications` (`id`, `recipient_id`, `type`, `title`, `message`, `link`, `is_read`, `created_at`) VALUES
(1, 1, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a ??t?? cr???? avec succ??s. Commencez ?? explorer le fil d\'actualit??.', '/fil', 0, '2026-02-18 14:31:01'),
(2, 2, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a ??t?? cr???? avec succ??s. Commencez ?? explorer le fil d\'actualit??.', '/fil', 0, '2026-02-18 14:35:36'),
(4, 1, 'post', 'Nouvelle publication', 'ilyes a publi?? dans le fil d\'actualit??.', '/fil#post-1', 0, '2026-02-18 14:45:24'),
(5, 2, 'post', 'Nouvelle publication', 'ilyes a publi?? dans le fil d\'actualit??.', '/fil#post-1', 0, '2026-02-18 14:45:24'),
(6, 1, 'post', 'Nouvelle publication', 'ilyes a publi?? dans le fil d\'actualit??.', '/fil#post-2', 0, '2026-02-18 14:45:26'),
(7, 2, 'post', 'Nouvelle publication', 'ilyes a publi?? dans le fil d\'actualit??.', '/fil#post-2', 0, '2026-02-18 14:45:26'),
(59, 26, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a ??t?? cr???? avec succ??s. Commencez ?? explorer le fil d\'actualit??.', '/fil', 0, '2026-02-18 15:07:37'),
(60, 27, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a ??t?? cr???? avec succ??s. Commencez ?? explorer le fil d\'actualit??.', '/fil', 0, '2026-02-20 15:55:44'),
(61, 28, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a ??t?? cr???? avec succ??s. Commencez ?? explorer le fil d\'actualit??.', '/fil', 0, '2026-02-21 00:12:19'),
(62, 29, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a ????t???? cr???????? avec succ????s. Commencez ???? explorer le fil d\'actualit????.', '/fil', 0, '2026-02-21 13:30:28'),
(63, 30, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a ????t???? cr???????? avec succ????s. Commencez ???? explorer le fil d\'actualit????.', '/fil', 0, '2026-02-22 01:41:23'),
(64, 31, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a ????t???? cr???????? avec succ????s. Commencez ???? explorer le fil d\'actualit????.', '/fil', 0, '2026-02-22 02:27:49'),
(65, 32, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a ????t???? cr???????? avec succ????s. Commencez ???? explorer le fil d\'actualit????.', '/fil', 0, '2026-02-22 03:52:27'),
(66, 1, 'team_report', 'Signalements ????quipe : blender team', 'Cette ????quipe a ????t???? signal????e (1 signalement(s) r????cents).', '/admin/equipes', 0, '2026-02-22 04:04:47'),
(67, 27, 'team_report', 'Signalements ????quipe : blender team', 'Cette ????quipe a ????t???? signal????e (1 signalement(s) r????cents).', '/admin/equipes', 0, '2026-02-22 04:04:47'),
(68, 1, 'team_report', 'Signalements ????quipe : blender team', 'Cette ????quipe a ????t???? signal????e (2 signalement(s) r????cents).', '/admin/equipes', 0, '2026-02-22 04:07:59'),
(69, 27, 'team_report', 'Signalements ????quipe : blender team', 'Cette ????quipe a ????t???? signal????e (2 signalement(s) r????cents).', '/admin/equipes', 0, '2026-02-22 04:07:59'),
(70, 33, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a ????t???? cr???????? avec succ????s. Commencez ???? explorer le fil d\'actualit????.', '/fil', 0, '2026-02-22 04:09:33'),
(71, 1, 'team_report', 'Signalements ????quipe : blender team', 'Cette ????quipe a ????t???? signal????e (3 signalement(s) r????cents).', '/admin/equipes', 0, '2026-02-22 04:09:56'),
(72, 27, 'team_report', 'Signalements ????quipe : blender team', 'Cette ????quipe a ????t???? signal????e (3 signalement(s) r????cents).', '/admin/equipes', 0, '2026-02-22 04:09:56'),
(73, 34, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a ??t?? cr???? avec succ??s. Commencez ?? explorer le fil d\'actualit??.', '/fil', 1, '2026-02-22 16:19:33'),
(74, 1, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(75, 2, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(77, 4, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(78, 5, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(79, 6, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(80, 8, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(81, 11, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(82, 12, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(85, 26, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(86, 27, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(87, 28, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(88, 29, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(89, 30, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(90, 31, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(91, 32, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(92, 33, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-33', 0, '2026-02-22 16:25:02'),
(93, 2, 'like', 'Nouveau like', 'medmed a aim?? votre publication.', '/fil#post-21', 0, '2026-02-22 16:26:22'),
(94, 1, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(95, 2, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(97, 4, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(98, 5, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(99, 6, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(100, 8, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(101, 11, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(102, 12, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(105, 26, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(106, 27, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(107, 28, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(108, 29, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(109, 30, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(110, 31, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(111, 32, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(112, 33, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-34', 0, '2026-02-22 16:27:07'),
(113, 2, 'participation', 'Nouvelle participation', 'medmed participe ?? votre ??v??nement.', '/fil#post-21', 0, '2026-02-22 16:41:08'),
(114, 1, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(115, 2, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(117, 4, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(118, 5, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(119, 6, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(120, 8, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(121, 11, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(122, 12, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(125, 26, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(126, 27, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(127, 28, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(128, 29, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(129, 30, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(130, 31, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(131, 32, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(132, 33, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-35', 0, '2026-02-22 16:58:17'),
(133, 1, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(134, 2, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(136, 4, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(137, 5, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(138, 6, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(139, 8, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(140, 11, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(141, 12, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(144, 26, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(145, 27, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(146, 28, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(147, 29, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(148, 30, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(149, 31, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(150, 32, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(151, 33, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-36', 0, '2026-02-23 02:23:08'),
(152, 1, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(153, 2, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(155, 4, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(156, 5, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(157, 6, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(158, 8, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(159, 11, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(160, 12, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(163, 26, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(164, 27, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(165, 28, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(166, 29, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(167, 30, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(168, 31, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(169, 32, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(170, 33, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-37', 0, '2026-02-23 02:27:34'),
(171, 1, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(172, 2, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(174, 4, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(175, 5, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(176, 6, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(177, 8, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(178, 11, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(179, 12, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(182, 26, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(183, 27, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(184, 28, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(185, 29, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(186, 30, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(187, 31, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(188, 32, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(189, 33, 'post', 'Nouvelle publication', 'medmed a publi?? dans le fil d\'actualit??.', '/fil#post-38', 0, '2026-02-23 03:42:38'),
(190, 11, 'participation', 'Nouvelle participation', 'medmed participe ?? votre ??v??nement.', '/fil#post-15', 0, '2026-02-23 03:49:18'),
(191, 1, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(192, 2, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(194, 4, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(195, 5, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(196, 6, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(197, 8, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(198, 11, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(199, 12, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(202, 26, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(203, 27, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(204, 28, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(205, 29, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(206, 30, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(207, 31, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(208, 32, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(209, 33, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-39', 0, '2026-02-23 04:00:39'),
(210, 1, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(211, 2, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(213, 4, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(214, 5, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(215, 6, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(216, 8, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(217, 11, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(218, 12, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(221, 26, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(222, 27, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(223, 28, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(224, 29, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(225, 30, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(226, 31, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(227, 32, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(228, 33, 'post', 'Nouvelle publication', 'medmed a publi???? dans le fil d\'actualit????.', '/fil#post-40', 0, '2026-02-23 04:10:32'),
(230, 41, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a été créé avec succès. Commencez à explorer le fil d\'actualité.', '/fil', 0, '2026-02-24 00:39:13'),
(231, 34, 'like', 'Nouveau like', 'amenamen a aimÃ© votre publication.', '/fil#post-40', 0, '2026-02-24 02:19:41'),
(232, 34, 'like', 'Nouveau like', 'amenbensalah123 a aimÃ© votre publication.', '/fil#post-40', 0, '2026-02-24 02:20:13'),
(233, 1, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(234, 2, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(236, 4, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(237, 5, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(238, 6, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(239, 8, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(240, 11, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(241, 12, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(243, 26, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(244, 27, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(245, 28, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(246, 29, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(247, 30, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(248, 31, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(249, 32, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(250, 33, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(251, 34, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(254, 39, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(255, 41, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-41', 0, '2026-02-24 02:55:27'),
(256, 1, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(257, 2, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(259, 4, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(260, 5, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(261, 6, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(262, 8, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(263, 11, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(264, 12, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(266, 26, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(267, 27, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(268, 28, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(269, 29, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(270, 30, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(271, 31, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(272, 32, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(273, 33, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(274, 34, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(277, 39, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(278, 41, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-42', 0, '2026-02-24 03:01:43'),
(279, 1, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(280, 2, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(282, 4, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(283, 5, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(284, 6, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(285, 8, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(286, 11, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(287, 12, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(289, 26, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(290, 27, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(291, 28, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(292, 29, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(293, 30, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(294, 31, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(295, 32, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(296, 33, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(297, 34, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(300, 39, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(301, 41, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-43', 0, '2026-02-24 03:04:31'),
(302, 1, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(303, 2, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(305, 4, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(306, 5, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(307, 6, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(308, 8, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(309, 11, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(310, 12, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(312, 26, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(313, 27, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(314, 28, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(315, 29, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(316, 30, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(317, 31, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(318, 32, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(319, 33, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(320, 34, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(323, 39, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(324, 41, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-44', 0, '2026-02-24 03:07:10'),
(325, 1, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(326, 2, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(328, 4, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(329, 5, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(330, 6, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(331, 8, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(332, 11, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(333, 12, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(335, 26, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(336, 27, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(337, 28, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(338, 29, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(339, 30, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(340, 31, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(341, 32, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(342, 33, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(343, 34, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(346, 39, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(347, 41, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-45', 0, '2026-02-24 03:07:54'),
(348, 1, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(349, 2, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(351, 4, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(352, 5, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(353, 6, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(354, 8, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(355, 11, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(356, 12, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(358, 26, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(359, 27, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(360, 28, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(361, 29, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(362, 30, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(363, 31, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(364, 32, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(365, 33, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(366, 34, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(369, 39, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(370, 41, 'post', 'Nouvelle publication', 'amenbensalah123 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-46', 0, '2026-02-24 03:21:28'),
(371, 42, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a été créé avec succès. Commencez à explorer le fil d\'actualité.', '/fil', 0, '2026-02-24 03:39:05'),
(372, 43, 'welcome', 'Bienvenue sur E-Sportify', 'Votre compte a été créé avec succès. Commencez à explorer le fil d\'actualité.', '/fil', 0, '2026-02-24 03:39:54'),
(374, 1, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(375, 2, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(377, 4, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(378, 5, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(379, 6, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(380, 8, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(381, 11, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(382, 12, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(384, 26, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(385, 27, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(386, 28, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(387, 29, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(388, 30, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(389, 31, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(390, 32, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(391, 33, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(392, 34, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(394, 39, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(395, 41, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23'),
(396, 43, 'post', 'Nouvelle publication', 'test1 a publiÃ© dans le fil d\'actualitÃ©.', '/fil#post-47', 0, '2026-02-24 10:09:23');

-- --------------------------------------------------------

--
-- Structure de la table `participation`
--

CREATE TABLE `participation` (
  `tournoi_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `participation`
--

INSERT INTO `participation` (`tournoi_id`, `user_id`) VALUES
(24, 42),
(24, 43),
(25, 42),
(25, 43),
(26, 42),
(26, 43),
(27, 42),
(27, 43),
(28, 42),
(28, 43),
(29, 42),
(29, 43),
(30, 42),
(30, 43),
(31, 42),
(31, 43),
(32, 42),
(32, 43),
(33, 42),
(33, 43),
(34, 42),
(34, 43),
(35, 42),
(35, 43),
(36, 42),
(36, 43),
(37, 42),
(37, 43),
(38, 42),
(38, 43),
(39, 42),
(39, 43),
(40, 42),
(40, 43),
(41, 42),
(41, 43),
(42, 42),
(42, 43);

-- --------------------------------------------------------

--
-- Structure de la table `participation_request`
--

CREATE TABLE `participation_request` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `tournoi_id` int(11) NOT NULL,
  `status` varchar(20) NOT NULL,
  `message` longtext DEFAULT NULL,
  `player_level` varchar(20) DEFAULT NULL,
  `rules_accepted` tinyint(1) NOT NULL,
  `applicant_name` varchar(255) DEFAULT NULL,
  `applicant_email` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `participation_request`
--

INSERT INTO `participation_request` (`id`, `user_id`, `tournoi_id`, `status`, `message`, `player_level`, `rules_accepted`, `applicant_name`, `applicant_email`, `created_at`) VALUES
(40, 43, 27, 'approved', 'testtesttesttest', 'pro', 1, NULL, NULL, '2026-02-24 03:57:54'),
(41, 42, 27, 'approved', 'testtesttesttest', 'amateur', 1, NULL, NULL, '2026-02-24 03:58:33');

-- --------------------------------------------------------

--
-- Structure de la table `password_reset_codes`
--

CREATE TABLE `password_reset_codes` (
  `id` int(11) NOT NULL,
  `email` varchar(180) NOT NULL,
  `code_hash` varchar(255) NOT NULL,
  `expires_at` datetime NOT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `password_reset_codes`
--

INSERT INTO `password_reset_codes` (`id`, `email`, `code_hash`, `expires_at`, `created_at`) VALUES
(1, 'ilyeszid33@gmail.com', '$2y$10$EtWIsH4I9QsUZOU07I4alubT.SmZbu3CqgVQOcCsX50VD.8I2OxyO', '2026-02-18 15:02:18', '2026-02-18 14:52:18'),
(17, 'ghaiethbouamor23@gmail.com', '$2y$10$UWaX95W7N4I4NCEyRa238.os69CHcN0U8bYnoTXI.eDC7QPcTOeU2', '2026-02-24 00:49:33', '2026-02-24 00:39:33'),
(18, 'amen.bensalah@esprit.tn', '$2y$10$gaMMxmSTMHAkehxNJHHptOCTWmhU/2jvSTKLYMwnWqD4ftpfwdPwy', '2026-03-15 01:07:54', '2026-03-15 00:57:54');

-- --------------------------------------------------------

--
-- Structure de la table `payment`
--

CREATE TABLE `payment` (
  `id` int(11) NOT NULL,
  `amount` double NOT NULL,
  `created_at` datetime NOT NULL,
  `status` varchar(255) NOT NULL,
  `commande_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `payment`
--

INSERT INTO `payment` (`id`, `amount`, `created_at`, `status`, `commande_id`) VALUES
(5, 132, '2026-02-20 19:06:14', 'paid', 65),
(6, 1, '2026-02-20 19:06:15', 'paid', 67),
(7, 582.05, '2026-02-20 19:06:15', 'paid', 68),
(8, 6.3, '2026-02-20 19:06:15', 'paid', 70),
(9, 1, '2026-02-20 19:15:24', 'paid', 72),
(10, 5.6, '2026-02-20 23:56:03', 'paid', 74),
(11, 39.46, '2026-02-20 23:56:24', 'paid', 73),
(12, 100, '2026-05-01 23:00:00', 'PAYEE', 91),
(13, 1640, '2026-05-01 23:00:00', 'PAYEE', 92);

-- --------------------------------------------------------

--
-- Structure de la table `posts`
--

CREATE TABLE `posts` (
  `id` int(11) NOT NULL,
  `content` longtext DEFAULT NULL,
  `media_type` varchar(255) NOT NULL DEFAULT 'text',
  `media_filename` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `video_url` varchar(255) DEFAULT NULL,
  `is_event` tinyint(1) NOT NULL DEFAULT 0,
  `event_title` varchar(180) DEFAULT NULL,
  `event_date` datetime DEFAULT NULL,
  `event_location` varchar(255) DEFAULT NULL,
  `max_participants` int(11) DEFAULT NULL,
  `author_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `posts`
--

INSERT INTO `posts` (`id`, `content`, `media_type`, `media_filename`, `created_at`, `image_path`, `video_url`, `is_event`, `event_title`, `event_date`, `event_location`, `max_participants`, `author_id`) VALUES
(1, 'sdfsq', 'text', NULL, '2026-02-18 14:45:24', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
(2, NULL, 'text', NULL, '2026-02-18 14:45:26', 'b588aa03e8846f73cb9e0ab6.png', NULL, 0, NULL, NULL, NULL, NULL, NULL),
(3, 'Openning', 'image', '455f9dccaf88f4fb5dbebc4f.png', '2026-02-08 13:56:58', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
(7, 'test', 'text', NULL, '2026-02-08 19:19:36', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
(8, 'hello', '', NULL, '2026-02-10 00:34:43', NULL, NULL, 0, NULL, NULL, NULL, NULL, 11),
(9, 'aaaa', '', NULL, '2026-02-10 00:38:59', 'b1969bed3a580db82e7dcaa6.png', NULL, 0, NULL, NULL, NULL, NULL, 1),
(10, 'aaaaaaa', '', NULL, '2026-02-10 00:39:11', '5a0b8eef4f59461c552157c7.png', NULL, 0, NULL, NULL, NULL, NULL, 1),
(11, NULL, '', NULL, '2026-02-10 00:39:45', 'e117407081da5f64ffe8fb96.png', NULL, 0, NULL, NULL, NULL, NULL, 11),
(12, 'okii', '', NULL, '2026-02-10 00:40:01', '0bb5b86563c3fa2813be6644.png', NULL, 0, NULL, NULL, NULL, NULL, 11),
(13, 'ok ok', '', NULL, '2026-02-10 01:00:05', 'b00468e1e39d559e9fc240bb.png', NULL, 0, NULL, NULL, NULL, NULL, 11),
(14, NULL, '', NULL, '2026-02-10 01:01:01', NULL, 'https://www.youtube.com/watch?v=diRiCP0r58A', 0, NULL, NULL, NULL, NULL, 11),
(15, NULL, '', NULL, '2026-02-10 01:01:14', NULL, NULL, 1, 'aaaa', '2026-02-25 01:01:00', 'sssss', 3, 11),
(16, 'ok', '', NULL, '2026-02-10 01:01:28', NULL, 'https://www.youtube.com/watch?v=diRiCP0r58A', 0, NULL, NULL, NULL, NULL, 11),
(17, NULL, '', NULL, '2026-02-10 01:04:03', NULL, 'https://www.youtube.com/watch?v=ZggspAgMsyE', 0, NULL, NULL, NULL, NULL, 11),
(18, 'Bienvenue sur E-Sportify ! Premier post de test.', 'text', NULL, '2026-02-10 01:18:24', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
(19, 'Scrim du vendredi - lien vid??o int??gr??.', 'text', NULL, '2026-02-10 01:18:24', NULL, 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 0, NULL, NULL, NULL, NULL, 4),
(20, 'Poster image test', 'text', NULL, '2026-02-10 01:18:24', 'https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=1200&auto=format&fit=crop', NULL, 0, NULL, NULL, NULL, NULL, NULL),
(21, 'Event test : Tournoi du week-end', 'text', NULL, '2026-02-10 01:18:24', NULL, NULL, 1, 'TOURNOI WEEK-END', '2026-02-13 01:18:24', 'Discord: https://discord.gg/esportify', 16, 2),
(22, NULL, '', NULL, '2026-02-10 02:00:52', '0fdb2f8d3f872d9a4fc8607c.png', NULL, 0, NULL, NULL, NULL, NULL, 11),
(23, 'fffffffffff', '', NULL, '2026-02-10 09:58:08', NULL, NULL, 0, NULL, NULL, NULL, NULL, 1),
(24, NULL, '', NULL, '2026-02-10 10:06:41', NULL, 'https://www.youtube.com/watch?v=7nQF5h-NioI&list=PLyKilQX_Qo-QE0XPiGEheGJ4_WWaV6LFL', 0, NULL, NULL, NULL, NULL, 4),
(25, 'E-sport world cup', '', NULL, '2026-02-10 10:07:31', NULL, 'https://www.youtube.com/watch?v=WGJR1ZYYgXc', 0, NULL, NULL, NULL, NULL, 4),
(27, 'hello', '', NULL, '2026-02-10 11:40:18', NULL, NULL, 0, NULL, NULL, NULL, NULL, 12),
(28, NULL, '', NULL, '2026-02-10 11:44:11', NULL, 'https://www.youtube.com/watch?v=N9bF8JfMcBA', 0, NULL, NULL, NULL, NULL, 1),
(29, 'test', '', NULL, '2026-02-10 11:46:08', NULL, NULL, 0, NULL, NULL, NULL, NULL, 12),
(30, 'sqd', '', NULL, '2026-02-14 19:36:54', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
(31, 'aaaaaaaaaa', '', NULL, '2026-02-16 20:23:44', NULL, NULL, 0, NULL, NULL, NULL, NULL, 22),
(32, 'aaaaaaaaaa', '', NULL, '2026-02-17 10:02:41', NULL, NULL, 0, NULL, NULL, NULL, NULL, 24),
(35, '?? Texte ?? est issu du mot latin ?? textum ??, d??riv?? du verbe ?? texere ?? qui signifie ?? tisser ??. Le mot s\'applique ?? l\'entrelacement des fibres utilis??es dans le tissage, voir par exemple Ovide : ?? Quo super iniecit textum rude sedula Baucis = (un si??ge) sur lequel Baucis empress??e avait jet?? un tissu grossier ??[2] ou au tressage (exemple chez Martial ?? Vimineum textum = panier d\'osier tress?? ??). Le verbe a aussi le sens large de construire comme dans ?? basilicam texere = construire une basilique ?? chez Cic??ron[3].\r\n\r\nLe sens figur?? d\'??l??ments de langage organis??s et encha??n??s appara??t avant l\'Empire romain : il d??signe un agencement particulier du discours. Exemple : ?? epistolas texere = composer des ??p??tres ?? - Cic??ron (Ier si??cle av. J.-C.)[4] ou plus nettement chez Quintilien (Ier si??cle apr. J.-C.) : ?? verba in textu jungantur = l\'agencement des mots dans la phrase ??[5].\r\n\r\nLes formes anciennes du Moyen ??ge d??signent au XIIe si??cle le volume qui contient le texte sacr?? des ??vangiles, puis au XIIIe si??cle, le texte original d\'un livre saint ou des propos de quelqu\'un. Au XVIIe si??cle le mot s???applique au passage d\'un ouvrage pris comme r??f??rence et au d??but du XIXe si??cle le mot texte a son sens g??n??ral d\'?? ??crit ??[6].', 'text', NULL, '2026-02-22 16:58:16', NULL, NULL, 0, NULL, NULL, NULL, NULL, 34),
(36, 'Le sport ??lectronique[2] (en anglais electronic sport, abr??g?? en e-sport), ou encore le jeu vid??o de comp??tition[3], est la pratique sur internet ou en tournoi sur r??seau local d\'un jeu vid??o, seul ou en ??quipe, sur ordinateur ou sur console de jeux vid??o.\r\n\r\nL\'essor du sport ??lectronique commence ?? la fin des ann??es 1980 avec les premiers jeux en r??seau multijoueur et, ?? partir des ann??es 1990, sur internet. Au cours des ann??es 2000 et 2010, le sport ??lectronique acquiert de plus en plus de notori??t??, et des tournois dot??s de prix importants commencent ?? appara??tre sur la sc??ne internationale.\r\n\r\nLes meilleurs adeptes mondiaux du sport ??lectronique se rencontrent lors de tournois officiels, organis??s par exemple par l\'eSports World Convention (ESWC), la Major League Gaming (MLG), la Cyberathlete Professional League (CPL) ou encore l\'Evolution Championship Series (EVO), pour ne citer que quelques organisateurs.\r\n\r\nLe sport ??lectronique ?? haut niveau est souvent financ?? par les sponsors et par les revenus g??n??r??s par la diffusion en streaming qui incluent souvent de la publicit??.', 'text', NULL, '2026-02-23 02:23:05', NULL, NULL, 0, NULL, NULL, NULL, NULL, 34),
(37, '??????', 'text', NULL, '2026-02-23 02:27:31', NULL, NULL, 0, NULL, NULL, NULL, NULL, 34),
(38, 'Bonjour, annonce tournoi aujourd hui: equipe joueurs, resultat victoire, commentaire publication.', 'text', NULL, '2026-02-23 03:42:33', NULL, NULL, 0, NULL, NULL, NULL, NULL, 34),
(39, 'pub', 'text', NULL, '2026-02-23 04:00:33', NULL, NULL, 0, NULL, NULL, NULL, NULL, 34),
(40, 'test22', 'text', NULL, '2026-02-23 04:10:27', NULL, NULL, 0, NULL, NULL, NULL, NULL, 34),
(41, NULL, 'text', NULL, '2026-02-24 02:55:26', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
(42, NULL, 'text', NULL, '2026-02-24 03:01:43', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
(43, NULL, 'text', NULL, '2026-02-24 03:04:30', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
(44, NULL, 'text', NULL, '2026-02-24 03:07:09', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
(45, NULL, 'text', NULL, '2026-02-24 03:07:54', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
(46, NULL, 'text', NULL, '2026-02-24 03:21:28', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
(47, 'Le sport? ?lectronique[2] (en anglais electronic sport, abr? ?g? ? en e-sport), ou encore le jeu vid? ?o de comp? ?tition[3], est la pratique sur internet ou en tournoi sur r? N\'hesitez pas a donner votre avis et partager vos retours en commentaire.', 'text', NULL, '2026-02-24 10:09:23', NULL, NULL, 0, NULL, NULL, NULL, NULL, 42);

-- --------------------------------------------------------

--
-- Structure de la table `post_media`
--

CREATE TABLE `post_media` (
  `id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `type` varchar(20) NOT NULL,
  `path` varchar(255) NOT NULL,
  `position` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `post_media`
--

INSERT INTO `post_media` (`id`, `post_id`, `type`, `path`, `position`) VALUES
(1, 2, 'image', 'b588aa03e8846f73cb9e0ab6.png', 0),
(4, 41, 'image', 'packages-699d050f11753470487201.png', 0),
(5, 42, 'image', 'etat-transition-699d06875eaab491486057.png', 0),
(6, 43, 'image', 'classes-conception-699d072e92b04012512959.png', 0),
(7, 44, 'image', 'seq-objet-699d07cd9c238671600084.png', 0),
(8, 44, 'image', 'packages-699d07cd9cf20129739503.png', 1),
(9, 44, 'image', 'comp-deploiment-699d07cd9dc07521507820.png', 2),
(10, 45, 'image', '63393793-63db-4cf1-b40b-f0c936869a78-699d07fa1cbae215529356.jpg', 0),
(11, 45, 'image', '034f761c-c3d6-45e8-b94f-c99b96d00b93-699d07fa1dd11584451624.jpg', 1),
(12, 45, 'image', '88269372-fb33-4d1a-b757-6dff4220295c-699d07fa1e7ba676701751.jpg', 2),
(13, 46, 'image', '607f9d8b-8e67-494b-9560-a5eede5835f9-699d0b281e386886746297.jpg', 0);

-- --------------------------------------------------------

--
-- Structure de la table `produit`
--

CREATE TABLE `produit` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `prix` int(11) NOT NULL,
  `stock` int(11) NOT NULL,
  `description` varchar(255) NOT NULL,
  `image` varchar(255) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `statut` varchar(50) NOT NULL DEFAULT 'disponible',
  `owner_user_id` int(11) DEFAULT NULL,
  `owner_equipe_id` int(11) DEFAULT NULL,
  `categorie_id` int(11) DEFAULT NULL,
  `video_url` varchar(255) DEFAULT NULL,
  `technical_specs` text DEFAULT NULL,
  `install_difficulty` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `produit`
--

INSERT INTO `produit` (`id`, `nom`, `prix`, `stock`, `description`, `image`, `active`, `statut`, `owner_user_id`, `owner_equipe_id`, `categorie_id`, `video_url`, `technical_specs`, `install_difficulty`) VALUES
(1, 'carte mere hytts', 100, 137, 'La carte m??re est le circuit imprim?? principal d\'un ordinateur, agissant comme le syst??me nerveux central qui interconnecte le processeur, la m??moire (RAM), le stockage et les p??riph??riques. Elle g??re l\'alimentation et la communication entre ces co', 'C:\\Users\\ilyes\\OneDrive\\Pictures\\Screenshots\\Capture d\'écran 2026-02-07 232800.png', 0, 'disponible', NULL, NULL, 4, NULL, NULL, NULL),
(3, 'carte mere ttht7410', 140, 21, 'La carte m??re est le circuit imprim?? principal d\'un ordinateur, agissant comme le syst??me nerveux central qui interconnecte le processeur, la m??moire (RAM), le stockage et les p??riph??riques. Elle g??re l\'alimentation et la communication entre ces co', 'uploads/images/Capture-d-ecran-2026-02-07-232800-6987bfea8b7f4.png', 0, 'disponible', NULL, NULL, 4, NULL, NULL, NULL),
(4, 'pc gamer mpla', 140, 20, 'Un ordinateur personnel (PC) est un appareil num??rique polyvalent (travail, jeux, internet) compos?? d\'??l??ments mat??riels essentiels : processeur (cerveau), carte m??re (connexion), m??moire RAM (temporaire), stockage SSD/HDD (permanent) et p??riph??r', 'C:\\Users\\ilyes\\OneDrive\\Pictures\\Screenshots\\Capture d\'écran 2026-02-07 232644.png', 0, 'disponible', NULL, NULL, 2, NULL, NULL, NULL),
(5, 'pc gamer', 1405, 40, 'Un ordinateur personnel (PC) est un appareil num??rique polyvalent (travail, jeux, internet) compos?? d\'??l??ments mat??riels essentiels : processeur (cerveau), carte m??re (connexion), m??moire RAM (temporaire), stockage SSD/HDD (permanent) et p??riph??r', 'C:\\Users\\ilyes\\OneDrive\\Pictures\\Screenshots\\Capture d\'écran 2026-02-07 232649.png', 0, 'disponible', NULL, NULL, 2, NULL, NULL, NULL),
(6, 'pc gamer', 1400, 139, 'Un ordinateur personnel (PC) est un appareil num??rique polyvalent (travail, jeux, internet) compos?? d\'??l??ments mat??riels essentiels : processeur (cerveau), carte m??re (connexion), m??moire RAM (temporaire), stockage SSD/HDD (permanent) et p??riph??r', 'C:\\Users\\ilyes\\OneDrive\\Pictures\\Screenshots\\Capture d\'écran 2026-02-07 232644.png', 0, 'disponible', NULL, NULL, 2, NULL, NULL, NULL),
(10, 'souris httys', 120, 0, 'La souris est un petit mammif??re rongeur (5-10 cm, 20-70 g) de la famille des murid??s, caract??ris?? par un museau pointu, de grandes oreilles, une longue queue et un pelage souvent gris ou brun', 'C:\\Users\\ilyes\\OneDrive\\Pictures\\Screenshots\\Capture d\'écran 2026-02-07 232705.png', 0, 'disponible', NULL, NULL, 5, NULL, NULL, NULL),
(11, 'souris htx2012', 120, 0, 'La souris est un petit mammif??re rongeur (5-10 cm, 20-70 g) de la famille des murid??s, caract??ris?? par un museau pointu, de grandes oreilles, une longue queue et un pelage souvent gris ou brun', 'C:\\Users\\ilyes\\OneDrive\\Pictures\\Screenshots\\Capture d\'écran 2026-02-07 232710.png', 0, 'disponible', NULL, NULL, 5, NULL, NULL, NULL),
(12, 'clavier', 15, 140, 'Un clavier d\'ordinateur est un p??riph??rique d\'entr??e essentiel, compos?? d\'environ 100 touches, permettant de saisir du texte, des chiffres et de commander un syst??me informatique. Issu des machines ?? ??crire, il se d??cline en versions filaires (USB', 'C:\\Users\\ilyes\\OneDrive\\Pictures\\Screenshots\\Capture d\'écran 2026-02-07 232728.png', 0, 'disponible', NULL, NULL, 3, NULL, NULL, NULL),
(13, 'clavier tt47', 16, 15, 'Un clavier d\'ordinateur est un p??riph??rique d\'entr??e essentiel, compos?? d\'environ 100 touches, permettant de saisir du texte, des chiffres et de commander un syst??me informatique. Issu des machines ?? ??crire, il se d??cline en versions filaires (USB', 'C:\\Users\\ilyes\\OneDrive\\Pictures\\Screenshots\\Capture d\'écran 2026-02-07 232732.png', 0, 'disponible', NULL, NULL, 3, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `recommendation`
--

CREATE TABLE `recommendation` (
  `id` int(11) NOT NULL,
  `score` double DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `produit_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `recrutement`
--

CREATE TABLE `recrutement` (
  `id` int(11) NOT NULL,
  `nom_rec` varchar(255) NOT NULL,
  `description` longtext NOT NULL,
  `status` varchar(50) NOT NULL,
  `date_publication` datetime NOT NULL,
  `equipe_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `resultat_tournoi`
--

CREATE TABLE `resultat_tournoi` (
  `id_resultat` int(11) NOT NULL,
  `rank` int(11) NOT NULL,
  `score` double NOT NULL,
  `id_tournoi` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `team_reports`
--

CREATE TABLE `team_reports` (
  `id` int(11) NOT NULL,
  `reason` longtext NOT NULL,
  `created_at` datetime NOT NULL,
  `equipe_id` int(11) NOT NULL,
  `reporter_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `team_reports`
--

INSERT INTO `team_reports` (`id`, `reason`, `created_at`, `equipe_id`, `reporter_id`) VALUES
(1, 'tricherie aaaaaaaaaaaaaaaa', '2026-02-22 04:04:47', 76, 30),
(2, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', '2026-02-22 04:07:59', 76, 4),
(3, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', '2026-02-22 04:09:56', 76, 33);

-- --------------------------------------------------------

--
-- Structure de la table `tournoi`
--

CREATE TABLE `tournoi` (
  `id_tournoi` int(11) NOT NULL,
  `creator_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `type_tournoi` varchar(50) NOT NULL,
  `type_game` varchar(50) NOT NULL,
  `game` varchar(255) NOT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `status` varchar(50) NOT NULL,
  `prize_won` double NOT NULL,
  `max_places` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `tournoi`
--

INSERT INTO `tournoi` (`id_tournoi`, `creator_id`, `name`, `type_tournoi`, `type_game`, `game`, `start_date`, `end_date`, `status`, `prize_won`, `max_places`) VALUES
(24, 42, 'Solo Sports Test Cup', 'solo', 'Sports', 'EA Sports FC 25', '2026-02-20 10:00:00', '2026-02-20 12:00:00', 'finished', 1000, 2),
(25, 42, 'Solo Mind Test Cup', 'solo', 'Mind', 'Chess', '2026-02-22 18:00:00', '2026-02-22 20:00:00', 'finished', 1000, 2),
(26, 42, 'Solo FPS Test Cup', 'solo', 'FPS', 'Valorant', '2026-02-21 14:00:00', '2026-02-21 16:00:00', 'finished', 1000, 2),
(27, 1, 'test test ', 'solo', 'FPS', 'cs', '2026-02-25 03:55:00', '2026-02-26 03:56:00', 'planned', 10000, 4),
(28, 1, 'CS2 Open Clash', 'solo', 'FPS', 'Counter-Strike 2', '2026-02-21 04:15:25', '2026-02-28 04:15:25', 'active', 50000, 16),
(29, 1, 'Valorant Masters Prime', 'solo', 'FPS', 'Valorant', '2026-03-01 04:15:25', '2026-03-04 04:15:25', 'planned', 75000, 16),
(30, 1, 'EAFC Solo Cup', 'solo', 'Sports', 'EA Sports FC 26', '2026-02-23 04:15:25', '2026-02-26 04:15:25', 'active', 25000, 32),
(31, 1, 'Warzone Night Royale', 'solo', 'Battle_royale', 'Warzone', '2026-02-22 04:15:25', '2026-03-01 04:15:25', 'active', 100000, 24),
(32, 1, 'Chess Masters Circuit', 'solo', 'Mind', 'Chess', '2026-01-30 04:15:25', '2026-02-04 04:15:25', 'completed', 15000, 32),
(33, 1, 'League Rift Series', 'solo', 'Sports', 'League of Legends', '2026-03-03 04:15:25', '2026-03-08 04:15:25', 'planned', 120000, 20),
(34, 1, 'Apex Frontier Showdown', 'solo', 'Battle_royale', 'Apex Legends', '2026-02-27 04:15:25', '2026-03-02 04:15:25', 'planned', 42000, 20),
(35, 1, 'Rocket League Turbo League', 'solo', 'Sports', 'Rocket League', '2026-02-22 04:15:25', '2026-02-28 04:15:25', 'active', 36000, 16),
(36, 1, 'Overwatch Payload Finals', 'solo', 'FPS', 'Overwatch 2', '2026-03-06 04:15:25', '2026-03-10 04:15:25', 'planned', 47000, 16),
(37, 1, 'R6 Siege Breach Cup', 'solo', 'FPS', 'Rainbow Six Siege', '2026-02-20 04:15:25', '2026-02-25 04:15:25', 'active', 54000, 16),
(38, 1, 'Tekken Iron Ladder', 'solo', 'Mind', 'Tekken 8', '2026-03-02 04:15:25', '2026-03-05 04:15:25', 'planned', 18000, 32),
(39, 1, 'AI Duel FPS Score Cup', 'solo', 'FPS', 'Valorant', '2026-02-14 04:15:25', '2026-02-15 04:15:25', 'completed', 5000, 8),
(40, 1, 'AI Duel Sports Score Cup', 'solo', 'Sports', 'EA Sports FC 26', '2026-02-16 04:15:25', '2026-02-17 04:15:25', 'completed', 4500, 8),
(41, 1, 'AI Duel Battle Royale Cup', 'solo', 'Battle_royale', 'Fortnite', '2026-02-18 04:15:25', '2026-02-19 04:15:25', 'completed', 6500, 20),
(42, 1, 'StarCraft Mind Arena', 'solo', 'Mind', 'StarCraft II', '2026-01-25 04:15:25', '2026-01-29 04:15:25', 'completed', 16000, 24);

-- --------------------------------------------------------

--
-- Structure de la table `tournoi_match`
--

CREATE TABLE `tournoi_match` (
  `id` int(11) NOT NULL,
  `tournoi_id` int(11) NOT NULL,
  `player_a_id` int(11) DEFAULT NULL,
  `player_b_id` int(11) DEFAULT NULL,
  `scheduled_at` datetime DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  `score_a` int(11) DEFAULT NULL,
  `score_b` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `home_name` varchar(255) DEFAULT NULL,
  `away_name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `tournoi_match`
--

INSERT INTO `tournoi_match` (`id`, `tournoi_id`, `player_a_id`, `player_b_id`, `scheduled_at`, `status`, `score_a`, `score_b`, `created_at`, `home_name`, `away_name`) VALUES
(36, 24, 42, 43, '2026-02-20 10:30:00', 'played', 3, 1, '2026-02-24 03:46:29', 'test1', 'test2'),
(37, 25, 42, 43, '2026-02-22 18:30:00', 'played', 1, 1, '2026-02-24 03:46:29', 'test1', 'test2'),
(38, 26, 42, 43, '2026-02-21 14:30:00', 'played', 0, 3, '2026-02-24 03:46:29', 'test1', 'test2'),
(42, 39, 42, 43, '2026-02-14 04:15:52', 'played', 13, 10, '2026-02-24 04:15:52', 'test1', 'test2'),
(43, 40, 42, 43, '2026-02-16 04:15:52', 'played', 2, 4, '2026-02-24 04:15:52', 'test1', 'test2'),
(44, 41, 42, 43, '2026-02-18 04:15:52', 'played', NULL, NULL, '2026-02-24 04:15:52', 'test1', 'test2');

-- --------------------------------------------------------

--
-- Structure de la table `tournoi_match_participant_result`
--

CREATE TABLE `tournoi_match_participant_result` (
  `id` int(11) NOT NULL,
  `match_id` int(11) NOT NULL,
  `participant_id` int(11) NOT NULL,
  `placement` varchar(20) NOT NULL,
  `points` int(11) NOT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `tournoi_match_participant_result`
--

INSERT INTO `tournoi_match_participant_result` (`id`, `match_id`, `participant_id`, `placement`, `points`, `created_at`) VALUES
(15, 41, 42, 'second', 1, '2026-02-24 04:15:25'),
(16, 41, 43, 'first', 3, '2026-02-24 04:15:25'),
(17, 44, 42, 'second', 1, '2026-02-24 04:15:52'),
(18, 44, 43, 'first', 3, '2026-02-24 04:15:52');

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `email` varchar(180) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `role` enum('ROLE_ADMIN','ROLE_JOUEUR','ROLE_MANAGER','ROLE_ORGANISATEUR') NOT NULL DEFAULT 'ROLE_JOUEUR',
  `pseudo` varchar(100) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `face_descriptor` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '(DC2Type:json)' CHECK (json_valid(`face_descriptor`)),
  `face_descriptor_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`face_descriptor_json`)),
  `last_login` datetime DEFAULT NULL,
  `warning_sent_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `user`
--

INSERT INTO `user` (`id`, `email`, `password`, `nom`, `role`, `pseudo`, `avatar`, `face_descriptor`, `face_descriptor_json`, `last_login`, `warning_sent_at`) VALUES
(1, 'admin@admin.com', '$2y$13$zRr6I2.ZHl/UDBYYrSj2qu4JGJ/ahGAdsCbF2.bLKO1fWzl0gbTL6', 'Admin', 'ROLE_ADMIN', 'admin', NULL, NULL, NULL, '2026-05-02 23:55:04', NULL),
(2, 'ilyes@gmail.com', '$2y$13$9.Op00JdnHpcXUCAYETdI.QvhzMME7DiaQoeKkRgS.zoIz4fwfuKC', 'ilyes', 'ROLE_JOUEUR', 'ilyes', '800f2ce025aa9f2ebc7295f8.png', NULL, NULL, NULL, NULL),
(4, 'ghaiethbouamor773@gmail.com', '$2y$13$jSxjw3XTopDlp8LVyeBhG.7275Zm1CS19OMkZBqLR3kybr/vl2XHO', 'ghaieth', 'ROLE_JOUEUR', '7riga', NULL, NULL, NULL, NULL, NULL),
(5, 'ilyeszid@esprit.tn', '$2y$13$o6YcNLpmzRJ.E5fiM4F8lOx.u/2wwF5QKTfOEhYIvR4KQcbZ.krSe', '', 'ROLE_JOUEUR', NULL, NULL, NULL, NULL, NULL, NULL),
(6, 'ilyes.zid@esprit.tn', '$2y$13$lvBoJoMoPHDDyOJyjwfRu.H9J1lHxzBOENFKXgyCsyq6Yun2aaE6a', 'ilyes zid', 'ROLE_JOUEUR', 'ilyes', NULL, NULL, NULL, NULL, NULL),
(8, 'ilyes14@gmail.com', '$2y$13$4wFj53Knc/.qnqIwvY3IpeV0uxaOqvMep4X2n945MYXzdWC2kUz2W', 'ilyes', 'ROLE_JOUEUR', 'ilyes', NULL, NULL, NULL, NULL, NULL),
(11, 'gmail@mohamed.nt', '$2y$13$DRVaJyN3.1zoP3A7Xb0CyeQNK6rKCO/ZDLqrHbNr3yFtBWJxobPoW', 'mohamedaaa', 'ROLE_MANAGER', 'amnesia', NULL, NULL, NULL, NULL, NULL),
(12, 'amen@amen.com', '$2y$13$PoClSjS/niOHDOflV3zfwul5eiTSJR6q.8ThIzWO1Js48v9oyPfLe', 'amen', 'ROLE_JOUEUR', 'amen123', NULL, NULL, NULL, NULL, NULL),
(26, 'ilyeszid@gmail.com', '$2y$13$sM1i63IcSUsLF6T1tuvx9e1mGkRt8wIp8.AKs4MedP98VLDNqR8tC', 'ilyes', 'ROLE_JOUEUR', 'ilyes', NULL, NULL, NULL, NULL, NULL),
(27, 'dhifallah17.aysser@gmail.com', '$2y$13$sWdbwXl1fg5fAPKv87GAtu2r0X9zFUY9.swZc8wB/TuG/M2faeWca', 'aysser dhifallah', 'ROLE_ADMIN', 'aysser25', NULL, NULL, NULL, NULL, NULL),
(28, 'dhifallah.aysser@gmail.com', '$2y$13$IYhYQ/yKcHwRDTSSBYoqG.bOqvYK.NHHZsvdGNRPVvx8aJkwh/bt2', 'aysser dhifallah', 'ROLE_JOUEUR', 'aysser25', NULL, NULL, NULL, NULL, NULL),
(29, 'youssef@gmail.com', '$2y$13$MDWBPq1Na4ZNzg.Msz8pReczxGFWyqdY4jQrx4OFAUZMzy9V8A/Nu', 'youssef', 'ROLE_JOUEUR', 'chpat', NULL, NULL, NULL, NULL, NULL),
(30, 'ghaiethbouamor1@gmail.com', '$2y$13$ahQKprfK4MfnyMNdwibV2uPKDQvbRetGNtW.CkVHlCR5ERnnzlQHC', 'ghaieth', 'ROLE_MANAGER', 'jellyfish', NULL, '[-0.07481438666582108,0.11277826875448227,0.02856130339205265,0.0021249919664114714,-0.10479679703712463,0.003022253978997469,-0.016452796757221222,-0.13426122069358826,0.2537117898464203,-0.17255277931690216,0.3135627508163452,0.028409138321876526,-0.23080827295780182,9.892391972243786e-5,-0.036662109196186066,0.10114617645740509,-0.11956202983856201,-0.12101265043020248,-0.00891154259443283,-0.015753518790006638,0.07401532679796219,0.07384451478719711,0.004206088371574879,0.05588122457265854,-0.13368846476078033,-0.31205683946609497,-0.0929543673992157,-0.10179990530014038,0.04960602521896362,-0.12275546789169312,-0.017574351280927658,0.023631762713193893,-0.1297469437122345,-0.02992129512131214,0.0035046394914388657,0.08917099237442017,-0.1183919683098793,-0.09979396313428879,0.23530016839504242,0.018149591982364655,-0.19270440936088562,-0.0744708776473999,-0.00856957957148552,0.2664701044559479,0.09920987486839294,-0.006047183182090521,0.06572789698839188,-0.1335708647966385,0.10112187266349792,-0.25546756386756897,0.07440837472677231,0.07861492037773132,0.14782026410102844,0.05613616481423378,0.09157713502645493,-0.21531426906585693,0.0050399526953697205,0.05632160231471062,-0.21995589137077332,0.06049538403749466,0.08059102296829224,-0.049541909247636795,-0.026578545570373535,-0.028459981083869934,0.18886999785900116,0.10646963864564896,-0.08622971177101135,-0.10973160713911057,0.18689528107643127,-0.20274506509304047,-0.08268488943576813,0.17051783204078674,-0.0338001511991024,-0.2743667960166931,-0.22698810696601868,-0.008573424071073532,0.5146229863166809,0.191153883934021,-0.12793326377868652,0.006571801379323006,-0.008255582302808762,-0.09806399047374725,0.0745154544711113,0.08726461231708527,-0.0546344518661499,0.03366580605506897,-0.1425391286611557,0.06558678299188614,0.22120977938175201,-0.002349885180592537,-0.06113043054938316,0.217161625623703,0.01857336238026619,0.05380472168326378,0.06189180165529251,0.0828455239534378,-0.08169618993997574,0.03131376579403877,-0.08728396147489548,-0.01767570525407791,0.015304944477975368,-0.06730706244707108,-0.022206459194421768,0.07873977720737457,-0.177780881524086,0.243636354804039,-0.012074428610503674,-0.05989020690321922,-0.02246686816215515,-0.028217071667313576,-0.11095982044935226,0.04868705943226814,0.17308391630649567,-0.3007976710796356,0.1198340356349945,0.08056408166885376,0.040464505553245544,0.23267441987991333,0.015403389930725098,0.051809024065732956,0.000658128410577774,-0.13077810406684875,-0.25495535135269165,-0.1099286824464798,-0.014343490824103355,-0.06566259264945984,0.09860727936029434,0.021414197981357574]', NULL, NULL, NULL),
(31, 'ghaiethbouamor2@gmail.com', '$2y$13$XY4xvZRAcaOEp.Dw5cWzd.6M3D2Rve4B4LBzQaq6Qbh4YMWR2u1Oa', 'ghaieth', 'ROLE_JOUEUR', 'azizos', NULL, NULL, NULL, NULL, NULL),
(32, 'ghaiethbouamor3@gmail.com', '$2y$13$aI8rm90As7ggT9UzhFA4YeJnvojeNZJTLSHGltgffyzBjosQvo9/C', 'ghaieth', 'ROLE_MANAGER', 'gaaloul', NULL, NULL, NULL, NULL, NULL),
(33, 'ghaiethbouamor4@gmail.com', '$2y$13$dAJTz6R8lBqP/8xRspiqjOEwwN7RJj7mmAUyHrXue73Jvmfihc9ti', 'ahmed', 'ROLE_JOUEUR', 'qzzzez', NULL, NULL, NULL, NULL, NULL),
(34, 'mohamed@mohamed.com', '$2y$13$ia2Bwu3qzJjQp20wG/xWiu/.Hxgf7eZEeVc.rP1FROUFTp7w3jocW', 'med', 'ROLE_JOUEUR', 'medmed', NULL, NULL, NULL, NULL, NULL),
(39, 'laktharala07@gmail.com', '$2y$13$qQiQTf7seqI1X3.pWlyazuVnx4nLcFbqATMj4RuT6LijQlGwPthZe', 'Ala Lkh', 'ROLE_JOUEUR', 'Ala Lkh', NULL, NULL, NULL, NULL, NULL),
(41, 'ghaiethbouamor23@gmail.com', '$2y$13$XmO2cDJa61DtUVXQkofiPOMVIb4JP2nDPCUMFfvMe4By69Y2IjytW', 'ghaieth', 'ROLE_MANAGER', 'ghaieth111', NULL, NULL, NULL, NULL, NULL),
(42, 'test1@test.com', '$2y$13$lyim7ya/7yPWT0PKjK/LPurbg88SXZLnOBtjIpTM8kvISt7jSUK5G', 'testa', 'ROLE_JOUEUR', 'test1', NULL, NULL, NULL, '2026-04-28 10:34:57', NULL),
(43, 'test2@test.com', '$2y$13$nKEC5H/c36Rf5hQ2H27..ewP8mxE3csEePnQV6YLY6BVIiqxZh/xu', 'testb', 'ROLE_JOUEUR', 'test2', NULL, NULL, NULL, NULL, NULL),
(47, 'amenbensalah038@gmail.com', '$2y$13$qJVF4SoJ9.SGwoj4wKbhTeq2DK9qcR9GjkZaY2r2rXgX0JbWOkzu2', 'Ben salah Amen', 'ROLE_JOUEUR', 'Ben salah Amen', NULL, NULL, NULL, '2026-04-28 10:37:40', NULL),
(50, 'azert@azert.azer', '$2y$13$FKYLJuPfwAjson1eWn1gfumNn/XSs0gqk6vdnCtUxtD0x4BH1hsOu', 'azert', 'ROLE_JOUEUR', 'azert', NULL, NULL, NULL, NULL, NULL),
(53, 'amen@gmail.com', '$2y$13$.fMC4871q49WhqEmB0cQ7uknavqG0WcL2kj.eyby/7mVOw6ub4Nce', 'Amen', 'ROLE_JOUEUR', 'OraclesShtap', NULL, NULL, '[0.0070789475407273955,0.0070789475407273955,0.0070789475407273955,0.0070789475407273955,0.02720211608411671,0.02720211608411671,0.02720211608411671,0.02720211608411671,0.043620724443085505,0.043620724443085505,0.043620724443085505,0.043620724443085505,0.020443959373333695,0.020443959373333695,0.020443959373333695,0.020443959373333695,0.033766164035261954,0.033766164035261954,0.033766164035261954,0.033766164035261954,-0.03816344883016212,-0.03816344883016212,-0.03816344883016212,-0.03816344883016212,-0.035404620912710176,-0.035404620912710176,-0.035404620912710176,-0.035404620912710176,0.0023203798577353077,0.0023203798577353077,0.0023203798577353077,0.0023203798577353077,0.04860294564958352,0.04860294564958352,0.04860294564958352,0.04860294564958352,-0.002758386982481292,-0.002758386982481292,-0.002758386982481292,-0.002758386982481292,-0.015115495327800873,-0.015115495327800873,-0.015115495327800873,-0.015115495327800873,0.04421635375920915,0.04421635375920915,0.04421635375920915,0.04421635375920915,0.02819444064600447,0.02819444064600447,0.02819444064600447,0.02819444064600447,0.05826376719280318,0.05826376719280318,0.05826376719280318,0.05826376719280318,-0.08168670383248769,-0.08168670383248769,-0.08168670383248769,-0.08168670383248769,0.028644651083545872,0.028644651083545872,0.028644651083545872,0.028644651083545872,-0.025926783690061798,-0.025926783690061798,-0.025926783690061798,-0.025926783690061798,-0.008454131854745008,-0.008454131854745008,-0.008454131854745008,-0.008454131854745008,-0.028117475388392803,-0.028117475388392803,-0.028117475388392803,-0.028117475388392803,0.01468134550007073,0.01468134550007073,0.01468134550007073,0.01468134550007073,0.027053520249966632,0.027053520249966632,0.027053520249966632,0.027053520249966632,-0.01817335681703311,-0.01817335681703311,-0.01817335681703311,-0.01817335681703311,-0.03058182320379064,-0.03058182320379064,-0.03058182320379064,-0.03058182320379064,-0.027917300935486573,-0.027917300935486573,-0.027917300935486573,-0.027917300935486573,-0.00966952663490324,-0.00966952663490324,-0.00966952663490324,-0.00966952663490324,-0.08228148457971139,-0.08228148457971139,-0.08228148457971139,-0.08228148457971139,-0.021412343671142562,-0.021412343671142562,-0.021412343671142562,-0.021412343671142562,-0.02474385998621453,-0.02474385998621453,-0.02474385998621453,-0.02474385998621453,0.032747786446563405,0.032747786446563405,0.032747786446563405,0.032747786446563405,0.00434674973490281,0.00434674973490281,0.00434674973490281,0.00434674973490281,0.011000928720071412,0.011000928720071412,0.011000928720071412,0.011000928720071412,0.0792879909653356,0.0792879909653356,0.0792879909653356,0.0792879909653356,0.04886061103681938,0.04886061103681938,0.04886061103681938,0.04886061103681938,0.056180797080142245,0.056180797080142245,0.056180797080142245,0.056180797080142245,0.024067956569271538,0.024067956569271538,0.024067956569271538,0.024067956569271538,0.00727879131915461,0.00727879131915461,0.00727879131915461,0.00727879131915461,-0.0375640670374229,-0.0375640670374229,-0.0375640670374229,-0.0375640670374229,0.05530511448051785,0.05530511448051785,0.05530511448051785,0.05530511448051785,-0.0037240697140979735,-0.0037240697140979735,-0.0037240697140979735,-0.0037240697140979735,-0.025553244794479204,-0.025553244794479204,-0.025553244794479204,-0.025553244794479204,0.011220927233588217,0.011220927233588217,0.011220927233588217,0.011220927233588217,0.01684405431067656,0.01684405431067656,0.01684405431067656,0.01684405431067656,-0.07132111042247757,-0.07132111042247757,-0.07132111042247757,-0.07132111042247757,0.056590215154299,0.056590215154299,0.056590215154299,0.056590215154299,0.08496579074280035,0.08496579074280035,0.08496579074280035,0.08496579074280035,0.05859489974661398,0.05859489974661398,0.05859489974661398,0.05859489974661398,-0.020609000673353115,-0.020609000673353115,-0.020609000673353115,-0.020609000673353115,0.001323614252406206,0.001323614252406206,0.001323614252406206,0.001323614252406206,-0.03170128420273453,-0.03170128420273453,-0.03170128420273453,-0.03170128420273453,-0.055741428945109604,-0.055741428945109604,-0.055741428945109604,-0.055741428945109604,0.1185222892022947,0.1185222892022947,0.1185222892022947,0.1185222892022947,-0.011189550886652841,-0.011189550886652841,-0.011189550886652841,-0.011189550886652841,0.026324679812753993,0.026324679812753993,0.026324679812753993,0.026324679812753993,-0.009094951741089362,-0.009094951741089362,-0.009094951741089362,-0.009094951741089362,0.025791243152691684,0.025791243152691684,0.025791243152691684,0.025791243152691684,0.03807915742366957,0.03807915742366957,0.03807915742366957,0.03807915742366957,0.06533070215539762,0.06533070215539762,0.06533070215539762,0.06533070215539762,0.0033727389489841817,0.0033727389489841817,0.0033727389489841817,0.0033727389489841817,0.013926112889692666,0.013926112889692666,0.013926112889692666,0.013926112889692666,-0.0033724240260789937,-0.0033724240260789937,-0.0033724240260789937,-0.0033724240260789937,-0.02167100318993453,-0.02167100318993453,-0.02167100318993453,-0.02167100318993453,0.054576799797758646,0.054576799797758646,0.054576799797758646,0.054576799797758646,-0.035447192169779936,-0.035447192169779936,-0.035447192169779936,-0.035447192169779936,-0.08075964210932339,-0.08075964210932339,-0.08075964210932339,-0.08075964210932339,0.014033375751659886,0.014033375751659886,0.014033375751659886,0.014033375751659886,0.011504438158535406,0.011504438158535406,0.011504438158535406,0.011504438158535406,-0.02342022512023575,-0.02342022512023575,-0.02342022512023575,-0.02342022512023575,0.050308909960266525,0.050308909960266525,0.050308909960266525,0.050308909960266525,-0.010770593734684352,-0.010770593734684352,-0.010770593734684352,-0.010770593734684352,0.08635283868625968,0.08635283868625968,0.08635283868625968,0.08635283868625968,-0.038250398734537706,-0.038250398734537706,-0.038250398734537706,-0.038250398734537706,0.07025479818659419,0.07025479818659419,0.07025479818659419,0.07025479818659419,0.09234515924755758,0.09234515924755758,0.09234515924755758,0.09234515924755758,-0.11626551699342541,-0.11626551699342541,-0.11626551699342541,-0.11626551699342541,0.06608828521826961,0.06608828521826961,0.06608828521826961,0.06608828521826961,-2.720034239818218E-5,-2.720034239818218E-5,-2.720034239818218E-5,-2.720034239818218E-5,0.03154068882471181,0.03154068882471181,0.03154068882471181,0.03154068882471181,-0.046836558331360446,-0.046836558331360446,-0.046836558331360446,-0.046836558331360446,-0.010404100331599348,-0.010404100331599348,-0.010404100331599348,-0.010404100331599348,-0.010549973504169686,-0.010549973504169686,-0.010549973504169686,-0.010549973504169686,-0.025335035749807316,-0.025335035749807316,-0.025335035749807316,-0.025335035749807316,-0.050575272601207186,-0.050575272601207186,-0.050575272601207186,-0.050575272601207186,0.007305472003875361,0.007305472003875361,0.007305472003875361,0.007305472003875361,0.04574756294253954,0.04574756294253954,0.04574756294253954,0.04574756294253954,-0.032209077147162694,-0.032209077147162694,-0.032209077147162694,-0.032209077147162694,0.03974398787035296,0.03974398787035296,0.03974398787035296,0.03974398787035296,-0.017889730303344783,-0.017889730303344783,-0.017889730303344783,-0.017889730303344783,-0.019535799966751116,-0.019535799966751116,-0.019535799966751116,-0.019535799966751116,-0.008416577081016351,-0.008416577081016351,-0.008416577081016351,-0.008416577081016351,-0.018001034375574737,-0.018001034375574737,-0.018001034375574737,-0.018001034375574737,0.02929101638726999,0.02929101638726999,0.02929101638726999,0.02929101638726999,0.054887555103433114,0.054887555103433114,0.054887555103433114,0.054887555103433114,0.06645231284898552,0.06645231284898552,0.06645231284898552,0.06645231284898552,0.02624016111936591,0.02624016111936591,0.02624016111936591,0.02624016111936591,-0.038354819481162865,-0.038354819481162865,-0.038354819481162865,-0.038354819481162865,-0.05593071814880824,-0.05593071814880824,-0.05593071814880824,-0.05593071814880824,-0.03923013495668759,-0.03923013495668759,-0.03923013495668759,-0.03923013495668759,-0.04658097889934355,-0.04658097889934355,-0.04658097889934355,-0.04658097889934355,0.04098857354518356,0.04098857354518356,0.04098857354518356,0.04098857354518356,0.06627155405470193,0.06627155405470193,0.06627155405470193,0.06627155405470193,-0.06694318208903242,-0.06694318208903242,-0.06694318208903242,-0.06694318208903242,-0.04785872087880729,-0.04785872087880729,-0.04785872087880729,-0.04785872087880729,-0.07822518303797736,-0.07822518303797736,-0.07822518303797736,-0.07822518303797736,0.04823196585290788,0.04823196585290788,0.04823196585290788,0.04823196585290788,-0.03691697624069788,-0.03691697624069788,-0.03691697624069788,-0.03691697624069788,0.04184355532248647,0.04184355532248647,0.04184355532248647,0.04184355532248647,-0.030345526036552384,-0.030345526036552384,-0.030345526036552384,-0.030345526036552384,0.04448646339387523,0.04448646339387523,0.04448646339387523,0.04448646339387523,0.060521172375141354,0.060521172375141354,0.060521172375141354,0.060521172375141354,-0.052149770799419216,-0.052149770799419216,-0.052149770799419216,-0.052149770799419216,0.08702459938317865,0.08702459938317865,0.08702459938317865,0.08702459938317865,0.019187606074675463,0.019187606074675463,0.019187606074675463,0.019187606074675463,-0.03151682032874616,-0.03151682032874616,-0.03151682032874616,-0.03151682032874616,-0.04888554140907061,-0.04888554140907061,-0.04888554140907061,-0.04888554140907061,0.007597833976051839,0.007597833976051839,0.007597833976051839,0.007597833976051839,0.008690544173640243,0.008690544173640243,0.008690544173640243,0.008690544173640243,-0.026105056563737114,-0.026105056563737114,-0.026105056563737114,-0.026105056563737114,0.026929455041149537,0.026929455041149537,0.026929455041149537,0.026929455041149537,0.02302673608189905,0.02302673608189905,0.02302673608189905,0.02302673608189905,0.07253854167307412,0.07253854167307412,0.07253854167307412,0.07253854167307412,0.019180457762501057,0.019180457762501057,0.019180457762501057,0.019180457762501057,0.02399607447350859,0.02399607447350859,0.02399607447350859,0.02399607447350859,0.050469188072355724,0.050469188072355724,0.050469188072355724,0.050469188072355724,0.06441925384697256,0.06441925384697256,0.06441925384697256,0.06441925384697256,-0.07159531829593299,-0.07159531829593299,-0.07159531829593299,-0.07159531829593299,-0.006749766105422683,-0.006749766105422683,-0.006749766105422683,-0.006749766105422683,-0.0032099926844138607,-0.0032099926844138607,-0.0032099926844138607,-0.0032099926844138607,-0.012302665554353817,-0.012302665554353817,-0.012302665554353817,-0.012302665554353817]', '2026-04-28 10:34:00', NULL),
(55, 'amen.bensalah@esprit.tn', '$2y$13$7hbjWX68l3R8FhBP/20P3eC9NJJAyW8wkQKY5JKbQg4v67AIu6H42', 'Amen Ben Salah', 'ROLE_JOUEUR', 'amenbensalah', NULL, NULL, NULL, '2026-04-28 10:41:10', NULL),
(56, 'ilyeszid33@gmail.com', '$2y$13$GtyEGS5Dhz0MQX58.56ba.6gYiiKqPV3FWt/ICRVL.TpoR5ncF7p.', 'ilyes', 'ROLE_JOUEUR', 'zid', NULL, NULL, '[-0.061666991491299075,-0.061666991491299075,-0.061666991491299075,-0.061666991491299075,-0.01698040099155805,-0.01698040099155805,-0.01698040099155805,-0.01698040099155805,0.027441811680314863,0.027441811680314863,0.027441811680314863,0.027441811680314863,0.009975464201783697,0.009975464201783697,0.009975464201783697,0.009975464201783697,0.034769491625978734,0.034769491625978734,0.034769491625978734,0.034769491625978734,-0.008624160900085357,-0.008624160900085357,-0.008624160900085357,-0.008624160900085357,0.058772621003578086,0.058772621003578086,0.058772621003578086,0.058772621003578086,0.015078489921716082,0.015078489921716082,0.015078489921716082,0.015078489921716082,-0.028485024461718647,-0.028485024461718647,-0.028485024461718647,-0.028485024461718647,-0.015509749046227555,-0.015509749046227555,-0.015509749046227555,-0.015509749046227555,-0.006444082248211484,-0.006444082248211484,-0.006444082248211484,-0.006444082248211484,0.08069565208596775,0.08069565208596775,0.08069565208596775,0.08069565208596775,0.025205179107190598,0.025205179107190598,0.025205179107190598,0.025205179107190598,0.0013955318699329397,0.0013955318699329397,0.0013955318699329397,0.0013955318699329397,-0.004368382196153435,-0.004368382196153435,-0.004368382196153435,-0.004368382196153435,-0.0605906963747449,-0.0605906963747449,-0.0605906963747449,-0.0605906963747449,-6.078103504613374E-4,-6.078103504613374E-4,-6.078103504613374E-4,-6.078103504613374E-4,0.012483523696616722,0.012483523696616722,0.012483523696616722,0.012483523696616722,-0.059764012052666605,-0.059764012052666605,-0.059764012052666605,-0.059764012052666605,0.012023368588714665,0.012023368588714665,0.012023368588714665,0.012023368588714665,-0.016315415689596394,-0.016315415689596394,-0.016315415689596394,-0.016315415689596394,-0.030734858357156232,-0.030734858357156232,-0.030734858357156232,-0.030734858357156232,-0.01580780417126693,-0.01580780417126693,-0.01580780417126693,-0.01580780417126693,0.024182755320715378,0.024182755320715378,0.024182755320715378,0.024182755320715378,0.02573463328285696,0.02573463328285696,0.02573463328285696,0.02573463328285696,0.01298007927489488,0.01298007927489488,0.01298007927489488,0.01298007927489488,-0.058885881157293124,-0.058885881157293124,-0.058885881157293124,-0.058885881157293124,0.08879684701712487,0.08879684701712487,0.08879684701712487,0.08879684701712487,0.0444671788270742,0.0444671788270742,0.0444671788270742,0.0444671788270742,-0.06623393390676997,-0.06623393390676997,-0.06623393390676997,-0.06623393390676997,-0.015152751699390362,-0.015152751699390362,-0.015152751699390362,-0.015152751699390362,0.0595701485953614,0.0595701485953614,0.0595701485953614,0.0595701485953614,-0.07541909210740533,-0.07541909210740533,-0.07541909210740533,-0.07541909210740533,-0.035600209555275424,-0.035600209555275424,-0.035600209555275424,-0.035600209555275424,-0.003493844903683656,-0.003493844903683656,-0.003493844903683656,-0.003493844903683656,0.08710587967992345,0.08710587967992345,0.08710587967992345,0.08710587967992345,-0.10710486296953867,-0.10710486296953867,-0.10710486296953867,-0.10710486296953867,0.0342907004813616,0.0342907004813616,0.0342907004813616,0.0342907004813616,0.0012991457561066068,0.0012991457561066068,0.0012991457561066068,0.0012991457561066068,-0.0019734198484911473,-0.0019734198484911473,-0.0019734198484911473,-0.0019734198484911473,-0.07137173476972605,-0.07137173476972605,-0.07137173476972605,-0.07137173476972605,0.01858352621669691,0.01858352621669691,0.01858352621669691,0.01858352621669691,-0.02264186145108121,-0.02264186145108121,-0.02264186145108121,-0.02264186145108121,-0.020434951414445374,-0.020434951414445374,-0.020434951414445374,-0.020434951414445374,0.06884150094377066,0.06884150094377066,0.06884150094377066,0.06884150094377066,0.03956948680782936,0.03956948680782936,0.03956948680782936,0.03956948680782936,0.018605624606586678,0.018605624606586678,0.018605624606586678,0.018605624606586678,0.04753526318705828,0.04753526318705828,0.04753526318705828,0.04753526318705828,-0.08734871413270022,-0.08734871413270022,-0.08734871413270022,-0.08734871413270022,-0.06656802291447099,-0.06656802291447099,-0.06656802291447099,-0.06656802291447099,0.010485420878194823,0.010485420878194823,0.010485420878194823,0.010485420878194823,-0.0024514305044328914,-0.0024514305044328914,-0.0024514305044328914,-0.0024514305044328914,0.08728098167662997,0.08728098167662997,0.08728098167662997,0.08728098167662997,0.04946057558568856,0.04946057558568856,0.04946057558568856,0.04946057558568856,0.00973428082608668,0.00973428082608668,0.00973428082608668,0.00973428082608668,-0.021784994327261144,-0.021784994327261144,-0.021784994327261144,-0.021784994327261144,0.0067844142885003815,0.0067844142885003815,0.0067844142885003815,0.0067844142885003815,-0.04390806338544503,-0.04390806338544503,-0.04390806338544503,-0.04390806338544503,-0.06276982982834833,-0.06276982982834833,-0.06276982982834833,-0.06276982982834833,-0.019419622999470167,-0.019419622999470167,-0.019419622999470167,-0.019419622999470167,-0.018233496951441996,-0.018233496951441996,-0.018233496951441996,-0.018233496951441996,0.05520275175381889,0.05520275175381889,0.05520275175381889,0.05520275175381889,-0.036637793010267844,-0.036637793010267844,-0.036637793010267844,-0.036637793010267844,0.0019023488601577964,0.0019023488601577964,0.0019023488601577964,0.0019023488601577964,-0.08894678120938002,-0.08894678120938002,-0.08894678120938002,-0.08894678120938002,0.014310804104712442,0.014310804104712442,0.014310804104712442,0.014310804104712442,0.04296863897692944,0.04296863897692944,0.04296863897692944,0.04296863897692944,-0.01763292183874929,-0.01763292183874929,-0.01763292183874929,-0.01763292183874929,0.010199918193692172,0.010199918193692172,0.010199918193692172,0.010199918193692172,0.039326735763900396,0.039326735763900396,0.039326735763900396,0.039326735763900396,0.02325175462048717,0.02325175462048717,0.02325175462048717,0.02325175462048717,-0.01117707863037119,-0.01117707863037119,-0.01117707863037119,-0.01117707863037119,0.04751106285122979,0.04751106285122979,0.04751106285122979,0.04751106285122979,-0.06506842766340848,-0.06506842766340848,-0.06506842766340848,-0.06506842766340848,0.04530307790761948,0.04530307790761948,0.04530307790761948,0.04530307790761948,0.006925412255787668,0.006925412255787668,0.006925412255787668,0.006925412255787668,0.02587962535009125,0.02587962535009125,0.02587962535009125,0.02587962535009125,-0.043558150155817724,-0.043558150155817724,-0.043558150155817724,-0.043558150155817724,-0.028805891335053218,-0.028805891335053218,-0.028805891335053218,-0.028805891335053218,0.04874683869821905,0.04874683869821905,0.04874683869821905,0.04874683869821905,0.04834038328445473,0.04834038328445473,0.04834038328445473,0.04834038328445473,0.009192167199139293,0.009192167199139293,0.009192167199139293,0.009192167199139293,-0.04484913227567255,-0.04484913227567255,-0.04484913227567255,-0.04484913227567255,0.05768721027560266,0.05768721027560266,0.05768721027560266,0.05768721027560266,0.07337788844949361,0.07337788844949361,0.07337788844949361,0.07337788844949361,0.08622490899253607,0.08622490899253607,0.08622490899253607,0.08622490899253607,-0.03169849405192492,-0.03169849405192492,-0.03169849405192492,-0.03169849405192492,0.003303000070216076,0.003303000070216076,0.003303000070216076,0.003303000070216076,0.008345483521035944,0.008345483521035944,0.008345483521035944,0.008345483521035944,-0.017772849384713844,-0.017772849384713844,-0.017772849384713844,-0.017772849384713844,0.025996572156300527,0.025996572156300527,0.025996572156300527,0.025996572156300527,-0.008925174009339313,-0.008925174009339313,-0.008925174009339313,-0.008925174009339313,0.04860139600168756,0.04860139600168756,0.04860139600168756,0.04860139600168756,-0.039321366461856175,-0.039321366461856175,-0.039321366461856175,-0.039321366461856175,0.025673026336489272,0.025673026336489272,0.025673026336489272,0.025673026336489272,0.0792735553642714,0.0792735553642714,0.0792735553642714,0.0792735553642714,-0.05678485740018124,-0.05678485740018124,-0.05678485740018124,-0.05678485740018124,0.007831140903168282,0.007831140903168282,0.007831140903168282,0.007831140903168282,0.06974538459721756,0.06974538459721756,0.06974538459721756,0.06974538459721756,0.032695732110432515,0.032695732110432515,0.032695732110432515,0.032695732110432515,-0.007983437701758881,-0.007983437701758881,-0.007983437701758881,-0.007983437701758881,-0.023848115639621935,-0.023848115639621935,-0.023848115639621935,-0.023848115639621935,-0.06141230876369337,-0.06141230876369337,-0.06141230876369337,-0.06141230876369337,0.05398192708762916,0.05398192708762916,0.05398192708762916,0.05398192708762916,-0.05867606063240438,-0.05867606063240438,-0.05867606063240438,-0.05867606063240438,0.07308471783007035,0.07308471783007035,0.07308471783007035,0.07308471783007035,1.383276344220678E-4,1.383276344220678E-4,1.383276344220678E-4,1.383276344220678E-4,0.06993609246752505,0.06993609246752505,0.06993609246752505,0.06993609246752505,0.08517660435015792,0.08517660435015792,0.08517660435015792,0.08517660435015792,0.003833287578593861,0.003833287578593861,0.003833287578593861,0.003833287578593861,0.02154247894762817,0.02154247894762817,0.02154247894762817,0.02154247894762817,-0.0124932470915152,-0.0124932470915152,-0.0124932470915152,-0.0124932470915152,0.023667859962733042,0.023667859962733042,0.023667859962733042,0.023667859962733042,-0.01069732493608156,-0.01069732493608156,-0.01069732493608156,-0.01069732493608156,0.012657283002937891,0.012657283002937891,0.012657283002937891,0.012657283002937891,0.050450973584574184,0.050450973584574184,0.050450973584574184,0.050450973584574184,-0.07232313627767169,-0.07232313627767169,-0.07232313627767169,-0.07232313627767169,-0.010860003260815147,-0.010860003260815147,-0.010860003260815147,-0.010860003260815147,-0.04952543344701248,-0.04952543344701248,-0.04952543344701248,-0.04952543344701248,-0.04704623544350137,-0.04704623544350137,-0.04704623544350137,-0.04704623544350137,0.051110733752766487,0.051110733752766487,0.051110733752766487,0.051110733752766487,0.009476838065581165,0.009476838065581165,0.009476838065581165,0.009476838065581165,0.026878671582477292,0.026878671582477292,0.026878671582477292,0.026878671582477292,0.009928557724084729,0.009928557724084729,0.009928557724084729,0.009928557724084729,-0.017376873501508797,-0.017376873501508797,-0.017376873501508797,-0.017376873501508797,-0.046385229482437076,-0.046385229482437076,-0.046385229482437076,-0.046385229482437076,-0.07177304430541791,-0.07177304430541791,-0.07177304430541791,-0.07177304430541791,0.020771781949913987,0.020771781949913987,0.020771781949913987,0.020771781949913987]', '2026-05-02 00:33:30', NULL),
(57, 'ilyeszid3@gmail.com', '$2y$13$jebLJvYvZ8WR6CCqASG3S.oArR4yyyDgsej6STfoc.O73Kq3tmEyO', 'ilyes', 'ROLE_JOUEUR', 'ilyes', NULL, NULL, NULL, '2026-05-01 23:54:03', NULL),
(58, 'ilyesz@gmail.com', '$2y$13$jBWENWXeFpslsLxZZ9HzIOI0q0yohB7iYMBCUBWs8EXJcaEDVOQ0i', 'ilyes', 'ROLE_JOUEUR', 'ilyes', NULL, NULL, '[-0.07616530033903177,-0.07616530033903177,-0.07616530033903177,-0.07616530033903177,0.006409919900710529,0.006409919900710529,0.006409919900710529,0.006409919900710529,0.019049715947066847,0.019049715947066847,0.019049715947066847,0.019049715947066847,0.028414215038482682,0.028414215038482682,0.028414215038482682,0.028414215038482682,0.05004496940686635,0.05004496940686635,0.05004496940686635,0.05004496940686635,0.033978573836885996,0.033978573836885996,0.033978573836885996,0.033978573836885996,0.0072481216232141675,0.0072481216232141675,0.0072481216232141675,0.0072481216232141675,-0.029637982077412323,-0.029637982077412323,-0.029637982077412323,-0.029637982077412323,-0.10051853770446413,-0.10051853770446413,-0.10051853770446413,-0.10051853770446413,-0.01860424086412789,-0.01860424086412789,-0.01860424086412789,-0.01860424086412789,0.009661623469428372,0.009661623469428372,0.009661623469428372,0.009661623469428372,0.07335706112843382,0.07335706112843382,0.07335706112843382,0.07335706112843382,0.037417739099911494,0.037417739099911494,0.037417739099911494,0.037417739099911494,0.02061023787362468,0.02061023787362468,0.02061023787362468,0.02061023787362468,0.007743049515994398,0.007743049515994398,0.007743049515994398,0.007743049515994398,-0.08223001769446159,-0.08223001769446159,-0.08223001769446159,-0.08223001769446159,-0.022973297759829137,-0.022973297759829137,-0.022973297759829137,-0.022973297759829137,0.007179017319699573,0.007179017319699573,0.007179017319699573,0.007179017319699573,-0.05987580372995496,-0.05987580372995496,-0.05987580372995496,-0.05987580372995496,0.04008291829698117,0.04008291829698117,0.04008291829698117,0.04008291829698117,0.007663869544315098,0.007663869544315098,0.007663869544315098,0.007663869544315098,-0.03233286967121892,-0.03233286967121892,-0.03233286967121892,-0.03233286967121892,-0.016923973931302738,-0.016923973931302738,-0.016923973931302738,-0.016923973931302738,0.0030262639384024213,0.0030262639384024213,0.0030262639384024213,0.0030262639384024213,0.013323866953314185,0.013323866953314185,0.013323866953314185,0.013323866953314185,0.017872090111525685,0.017872090111525685,0.017872090111525685,0.017872090111525685,0.003409011564103807,0.003409011564103807,0.003409011564103807,0.003409011564103807,0.05007834763609109,0.05007834763609109,0.05007834763609109,0.05007834763609109,0.025269324664406238,0.025269324664406238,0.025269324664406238,0.025269324664406238,-0.07134189572242827,-0.07134189572242827,-0.07134189572242827,-0.07134189572242827,-0.024101054689176473,-0.024101054689176473,-0.024101054689176473,-0.024101054689176473,0.05888382746874407,0.05888382746874407,0.05888382746874407,0.05888382746874407,-0.06246395562630196,-0.06246395562630196,-0.06246395562630196,-0.06246395562630196,0.016307768276583295,0.016307768276583295,0.016307768276583295,0.016307768276583295,-0.025214713072593105,-0.025214713072593105,-0.025214713072593105,-0.025214713072593105,0.05471937565792436,0.05471937565792436,0.05471937565792436,0.05471937565792436,-0.06699034100505048,-0.06699034100505048,-0.06699034100505048,-0.06699034100505048,0.07250142444361399,0.07250142444361399,0.07250142444361399,0.07250142444361399,0.005636916005288588,0.005636916005288588,0.005636916005288588,0.005636916005288588,-0.01438671698401387,-0.01438671698401387,-0.01438671698401387,-0.01438671698401387,-0.03199230880219244,-0.03199230880219244,-0.03199230880219244,-0.03199230880219244,0.02063516363624616,0.02063516363624616,0.02063516363624616,0.02063516363624616,-0.05698858956350169,-0.05698858956350169,-0.05698858956350169,-0.05698858956350169,-0.009515103669483698,-0.009515103669483698,-0.009515103669483698,-0.009515103669483698,0.06980671289116372,0.06980671289116372,0.06980671289116372,0.06980671289116372,0.05922811268967415,0.05922811268967415,0.05922811268967415,0.05922811268967415,-0.008393286814094042,-0.008393286814094042,-0.008393286814094042,-0.008393286814094042,0.005931613674303344,0.005931613674303344,0.005931613674303344,0.005931613674303344,-0.09770045842431736,-0.09770045842431736,-0.09770045842431736,-0.09770045842431736,-0.036220032824775546,-0.036220032824775546,-0.036220032824775546,-0.036220032824775546,0.05790995731198662,0.05790995731198662,0.05790995731198662,0.05790995731198662,-0.012393281831652028,-0.012393281831652028,-0.012393281831652028,-0.012393281831652028,0.058013061602625884,0.058013061602625884,0.058013061602625884,0.058013061602625884,0.02078511033151896,0.02078511033151896,0.02078511033151896,0.02078511033151896,0.05316162972402405,0.05316162972402405,0.05316162972402405,0.05316162972402405,-0.04517664628562245,-0.04517664628562245,-0.04517664628562245,-0.04517664628562245,-0.018062819804819916,-0.018062819804819916,-0.018062819804819916,-0.018062819804819916,-0.024115098372559814,-0.024115098372559814,-0.024115098372559814,-0.024115098372559814,-0.07516675289711593,-0.07516675289711593,-0.07516675289711593,-0.07516675289711593,0.026096565482401596,0.026096565482401596,0.026096565482401596,0.026096565482401596,-0.014663584639740808,-0.014663584639740808,-0.014663584639740808,-0.014663584639740808,0.08259517299947679,0.08259517299947679,0.08259517299947679,0.08259517299947679,-0.040269046386582054,-0.040269046386582054,-0.040269046386582054,-0.040269046386582054,-0.021917775492658845,-0.021917775492658845,-0.021917775492658845,-0.021917775492658845,-0.05095312885941742,-0.05095312885941742,-0.05095312885941742,-0.05095312885941742,0.01622764618653567,0.01622764618653567,0.01622764618653567,0.01622764618653567,0.06949165375653173,0.06949165375653173,0.06949165375653173,0.06949165375653173,-0.006647785700869849,-0.006647785700869849,-0.006647785700869849,-0.006647785700869849,0.023411894732285058,0.023411894732285058,0.023411894732285058,0.023411894732285058,0.06547594628602285,0.06547594628602285,0.06547594628602285,0.06547594628602285,0.039901440628595776,0.039901440628595776,0.039901440628595776,0.039901440628595776,0.02267058640157351,0.02267058640157351,0.02267058640157351,0.02267058640157351,0.05308106863105812,0.05308106863105812,0.05308106863105812,0.05308106863105812,-0.08353359551274749,-0.08353359551274749,-0.08353359551274749,-0.08353359551274749,9.516596227381787E-5,9.516596227381787E-5,9.516596227381787E-5,9.516596227381787E-5,0.006817290008809381,0.006817290008809381,0.006817290008809381,0.006817290008809381,0.018966261510859297,0.018966261510859297,0.018966261510859297,0.018966261510859297,-0.05290030599717436,-0.05290030599717436,-0.05290030599717436,-0.05290030599717436,-0.029749697349599844,-0.029749697349599844,-0.029749697349599844,-0.029749697349599844,0.07120161917495826,0.07120161917495826,0.07120161917495826,0.07120161917495826,0.06687677003773633,0.06687677003773633,0.06687677003773633,0.06687677003773633,-0.011894818319756034,-0.011894818319756034,-0.011894818319756034,-0.011894818319756034,-0.06288402192490644,-0.06288402192490644,-0.06288402192490644,-0.06288402192490644,0.08773734588680142,0.08773734588680142,0.08773734588680142,0.08773734588680142,0.08475242499910865,0.08475242499910865,0.08475242499910865,0.08475242499910865,0.027086938513619572,0.027086938513619572,0.027086938513619572,0.027086938513619572,-0.05116269278951756,-0.05116269278951756,-0.05116269278951756,-0.05116269278951756,-0.041121518740608584,-0.041121518740608584,-0.041121518740608584,-0.041121518740608584,0.00564679717605785,0.00564679717605785,0.00564679717605785,0.00564679717605785,-0.02000892210256211,-0.02000892210256211,-0.02000892210256211,-0.02000892210256211,0.03427182094464149,0.03427182094464149,0.03427182094464149,0.03427182094464149,-0.019161100549390634,-0.019161100549390634,-0.019161100549390634,-0.019161100549390634,0.03700858455487046,0.03700858455487046,0.03700858455487046,0.03700858455487046,0.009969560142091131,0.009969560142091131,0.009969560142091131,0.009969560142091131,0.02118286700221367,0.02118286700221367,0.02118286700221367,0.02118286700221367,0.06583895272981898,0.06583895272981898,0.06583895272981898,0.06583895272981898,-0.05640746317052083,-0.05640746317052083,-0.05640746317052083,-0.05640746317052083,-0.05181128990788295,-0.05181128990788295,-0.05181128990788295,-0.05181128990788295,0.06358870517737938,0.06358870517737938,0.06358870517737938,0.06358870517737938,0.026993272211941543,0.026993272211941543,0.026993272211941543,0.026993272211941543,-0.01838177396525699,-0.01838177396525699,-0.01838177396525699,-0.01838177396525699,-0.013713097038886828,-0.013713097038886828,-0.013713097038886828,-0.013713097038886828,-0.04654105525534673,-0.04654105525534673,-0.04654105525534673,-0.04654105525534673,-0.0192979648413325,-0.0192979648413325,-0.0192979648413325,-0.0192979648413325,-0.04993551411180029,-0.04993551411180029,-0.04993551411180029,-0.04993551411180029,0.015977981408764445,0.015977981408764445,0.015977981408764445,0.015977981408764445,0.00159611698428654,0.00159611698428654,0.00159611698428654,0.00159611698428654,0.09950504788828801,0.09950504788828801,0.09950504788828801,0.09950504788828801,0.06452478548368094,0.06452478548368094,0.06452478548368094,0.06452478548368094,-0.01160626168058045,-0.01160626168058045,-0.01160626168058045,-0.01160626168058045,0.005739192006376655,0.005739192006376655,0.005739192006376655,0.005739192006376655,0.021162786794934284,0.021162786794934284,0.021162786794934284,0.021162786794934284,-0.012616358140488726,-0.012616358140488726,-0.012616358140488726,-0.012616358140488726,-0.012318748632522395,-0.012318748632522395,-0.012318748632522395,-0.012318748632522395,0.007143636246715014,0.007143636246715014,0.007143636246715014,0.007143636246715014,0.046892073504205906,0.046892073504205906,0.046892073504205906,0.046892073504205906,-0.021119746228639918,-0.021119746228639918,-0.021119746228639918,-0.021119746228639918,2.4322781297269483E-4,2.4322781297269483E-4,2.4322781297269483E-4,2.4322781297269483E-4,-0.09641831939765687,-0.09641831939765687,-0.09641831939765687,-0.09641831939765687,-0.03488460352305823,-0.03488460352305823,-0.03488460352305823,-0.03488460352305823,0.041532300409753475,0.041532300409753475,0.041532300409753475,0.041532300409753475,0.03445621338951728,0.03445621338951728,0.03445621338951728,0.03445621338951728,0.007662603704644899,0.007662603704644899,0.007662603704644899,0.007662603704644899,0.01022267742568456,0.01022267742568456,0.01022267742568456,0.01022267742568456,-0.005881033484764307,-0.005881033484764307,-0.005881033484764307,-0.005881033484764307,-0.03396336925248269,-0.03396336925248269,-0.03396336925248269,-0.03396336925248269,-0.057500391866106115,-0.057500391866106115,-0.057500391866106115,-0.057500391866106115,0.02880239328556943,0.02880239328556943,0.02880239328556943,0.02880239328556943]', '2026-05-02 00:20:12', NULL);
INSERT INTO `user` (`id`, `email`, `password`, `nom`, `role`, `pseudo`, `avatar`, `face_descriptor`, `face_descriptor_json`, `last_login`, `warning_sent_at`) VALUES
(59, 'ilyesz@gmail.comm', '$2y$13$cAT4iU32/lySAAxmvdVOkONRqBkvZ9ow35fh6S8ThPLpfDfkxlmJq', 'ilyes', 'ROLE_JOUEUR', 'ilyes', NULL, NULL, '[-0.08903833695111447,-0.08903833695111447,-0.08903833695111447,-0.08903833695111447,-0.020635892841547588,-0.020635892841547588,-0.020635892841547588,-0.020635892841547588,0.0182681083425936,0.0182681083425936,0.0182681083425936,0.0182681083425936,-0.011850736494972045,-0.011850736494972045,-0.011850736494972045,-0.011850736494972045,0.07014411822604165,0.07014411822604165,0.07014411822604165,0.07014411822604165,-0.02061705798020116,-0.02061705798020116,-0.02061705798020116,-0.02061705798020116,0.04182595825711424,0.04182595825711424,0.04182595825711424,0.04182595825711424,-0.01780693973849385,-0.01780693973849385,-0.01780693973849385,-0.01780693973849385,-0.03905528103693351,-0.03905528103693351,-0.03905528103693351,-0.03905528103693351,0.014352071399898475,0.014352071399898475,0.014352071399898475,0.014352071399898475,-0.04518502773066289,-0.04518502773066289,-0.04518502773066289,-0.04518502773066289,0.03423099368824749,0.03423099368824749,0.03423099368824749,0.03423099368824749,0.023386780388871736,0.023386780388871736,0.023386780388871736,0.023386780388871736,-0.02126623473831337,-0.02126623473831337,-0.02126623473831337,-0.02126623473831337,-0.04857574443728715,-0.04857574443728715,-0.04857574443728715,-0.04857574443728715,-0.02200418646796744,-0.02200418646796744,-0.02200418646796744,-0.02200418646796744,-0.0023667153829483693,-0.0023667153829483693,-0.0023667153829483693,-0.0023667153829483693,0.009701809505184645,0.009701809505184645,0.009701809505184645,0.009701809505184645,-0.05838413868273433,-0.05838413868273433,-0.05838413868273433,-0.05838413868273433,0.02991377822581533,0.02991377822581533,0.02991377822581533,0.02991377822581533,-0.03766416885036156,-0.03766416885036156,-0.03766416885036156,-0.03766416885036156,-0.009057313060652461,-0.009057313060652461,-0.009057313060652461,-0.009057313060652461,-0.016040699914998097,-0.016040699914998097,-0.016040699914998097,-0.016040699914998097,0.0016511050311292241,0.0016511050311292241,0.0016511050311292241,0.0016511050311292241,0.003833178219615624,0.003833178219615624,0.003833178219615624,0.003833178219615624,0.0012717591167737878,0.0012717591167737878,0.0012717591167737878,0.0012717591167737878,0.010931483038823296,0.010931483038823296,0.010931483038823296,0.010931483038823296,0.045456004579507304,0.045456004579507304,0.045456004579507304,0.045456004579507304,0.05982655718696594,0.05982655718696594,0.05982655718696594,0.05982655718696594,-0.05534119324588229,-0.05534119324588229,-0.05534119324588229,-0.05534119324588229,-0.006469041872612627,-0.006469041872612627,-0.006469041872612627,-0.006469041872612627,0.06359192456898909,0.06359192456898909,0.06359192456898909,0.06359192456898909,-0.05969385946696961,-0.05969385946696961,-0.05969385946696961,-0.05969385946696961,0.013885464474227392,0.013885464474227392,0.013885464474227392,0.013885464474227392,-0.017760801407043968,-0.017760801407043968,-0.017760801407043968,-0.017760801407043968,0.03733499003926925,0.03733499003926925,0.03733499003926925,0.03733499003926925,-0.055912075564952736,-0.055912075564952736,-0.055912075564952736,-0.055912075564952736,0.03867139950166168,0.03867139950166168,0.03867139950166168,0.03867139950166168,0.06711762904569328,0.06711762904569328,0.06711762904569328,0.06711762904569328,-0.020659040807146978,-0.020659040807146978,-0.020659040807146978,-0.020659040807146978,-0.08588852750703449,-0.08588852750703449,-0.08588852750703449,-0.08588852750703449,0.014174954226406376,0.014174954226406376,0.014174954226406376,0.014174954226406376,-0.011668811662085669,-0.011668811662085669,-0.011668811662085669,-0.011668811662085669,-0.012212776479108746,-0.012212776479108746,-0.012212776479108746,-0.012212776479108746,0.05607289366837198,0.05607289366837198,0.05607289366837198,0.05607289366837198,0.05718979136853665,0.05718979136853665,0.05718979136853665,0.05718979136853665,0.04714832198760492,0.04714832198760492,0.04714832198760492,0.04714832198760492,0.019160884002238202,0.019160884002238202,0.019160884002238202,0.019160884002238202,-0.09515331447382468,-0.09515331447382468,-0.09515331447382468,-0.09515331447382468,-0.010056465813957118,-0.010056465813957118,-0.010056465813957118,-0.010056465813957118,0.058286193008598185,0.058286193008598185,0.058286193008598185,0.058286193008598185,0.0017528613574792045,0.0017528613574792045,0.0017528613574792045,0.0017528613574792045,0.08147868136134344,0.08147868136134344,0.08147868136134344,0.08147868136134344,0.015695660153193368,0.015695660153193368,0.015695660153193368,0.015695660153193368,0.010790921694419841,0.010790921694419841,0.010790921694419841,0.010790921694419841,-0.043977573624246744,-0.043977573624246744,-0.043977573624246744,-0.043977573624246744,0.011107541680742784,0.011107541680742784,0.011107541680742784,0.011107541680742784,-0.05485087489983325,-0.05485087489983325,-0.05485087489983325,-0.05485087489983325,-0.08158207364624225,-0.08158207364624225,-0.08158207364624225,-0.08158207364624225,0.013751384727903673,0.013751384727903673,0.013751384727903673,0.013751384727903673,-0.02312509194380024,-0.02312509194380024,-0.02312509194380024,-0.02312509194380024,0.06814202152568063,0.06814202152568063,0.06814202152568063,0.06814202152568063,0.030229609814662117,0.030229609814662117,0.030229609814662117,0.030229609814662117,-0.010999571781711443,-0.010999571781711443,-0.010999571781711443,-0.010999571781711443,-0.06778405757804253,-0.06778405757804253,-0.06778405757804253,-0.06778405757804253,0.020043514545946023,0.020043514545946023,0.020043514545946023,0.020043514545946023,0.057800052470546294,0.057800052470546294,0.057800052470546294,0.057800052470546294,0.004331306367028646,0.004331306367028646,0.004331306367028646,0.004331306367028646,0.021919219698335768,0.021919219698335768,0.021919219698335768,0.021919219698335768,0.0733801295871025,0.0733801295871025,0.0733801295871025,0.0733801295871025,0.0676725633453338,0.0676725633453338,0.0676725633453338,0.0676725633453338,-0.026343251601575593,-0.026343251601575593,-0.026343251601575593,-0.026343251601575593,0.05776600484468791,0.05776600484468791,0.05776600484468791,0.05776600484468791,-0.07791062103561626,-0.07791062103561626,-0.07791062103561626,-0.07791062103561626,0.0794467144016779,0.0794467144016779,0.0794467144016779,0.0794467144016779,-0.03860040699584331,-0.03860040699584331,-0.03860040699584331,-0.03860040699584331,0.0587284524387953,0.0587284524387953,0.0587284524387953,0.0587284524387953,0.0025721488045277645,0.0025721488045277645,0.0025721488045277645,0.0025721488045277645,-0.014942047557492786,-0.014942047557492786,-0.014942047557492786,-0.014942047557492786,0.09334917526212505,0.09334917526212505,0.09334917526212505,0.09334917526212505,0.00625502906207148,0.00625502906207148,0.00625502906207148,0.00625502906207148,-0.0072927687971659355,-0.0072927687971659355,-0.0072927687971659355,-0.0072927687971659355,-0.038845602651546726,-0.038845602651546726,-0.038845602651546726,-0.038845602651546726,0.10124903207104596,0.10124903207104596,0.10124903207104596,0.10124903207104596,0.039249014172804804,0.039249014172804804,0.039249014172804804,0.039249014172804804,0.07664128678658719,0.07664128678658719,0.07664128678658719,0.07664128678658719,-0.02016979980838002,-0.02016979980838002,-0.02016979980838002,-0.02016979980838002,-0.019864917633306278,-0.019864917633306278,-0.019864917633306278,-0.019864917633306278,0.0022580195675255777,0.0022580195675255777,0.0022580195675255777,0.0022580195675255777,0.04534449940939584,0.04534449940939584,0.04534449940939584,0.04534449940939584,0.04129285765299886,0.04129285765299886,0.04129285765299886,0.04129285765299886,-0.02684336288860339,-0.02684336288860339,-0.02684336288860339,-0.02684336288860339,0.06303732672992067,0.06303732672992067,0.06303732672992067,0.06303732672992067,-0.009009384125899755,-0.009009384125899755,-0.009009384125899755,-0.009009384125899755,0.007562406515275974,0.007562406515275974,0.007562406515275974,0.007562406515275974,0.08061052813786596,0.08061052813786596,0.08061052813786596,0.08061052813786596,-0.02627385531290388,-0.02627385531290388,-0.02627385531290388,-0.02627385531290388,0.02240959893601656,0.02240959893601656,0.02240959893601656,0.02240959893601656,0.06549236844956718,0.06549236844956718,0.06549236844956718,0.06549236844956718,0.06044472063970612,0.06044472063970612,0.06044472063970612,0.06044472063970612,0.039432525185218656,0.039432525185218656,0.039432525185218656,0.039432525185218656,-0.05321292425103344,-0.05321292425103344,-0.05321292425103344,-0.05321292425103344,-0.034723190115863195,-0.034723190115863195,-0.034723190115863195,-0.034723190115863195,0.03512207271607871,0.03512207271607871,0.03512207271607871,0.03512207271607871,-0.024624668520129175,-0.024624668520129175,-0.024624668520129175,-0.024624668520129175,0.008816163039906582,0.008816163039906582,0.008816163039906582,0.008816163039906582,-0.015947308345004303,-0.015947308345004303,-0.015947308345004303,-0.015947308345004303,0.023209739225626573,0.023209739225626573,0.023209739225626573,0.023209739225626573,0.0355603115440687,0.0355603115440687,0.0355603115440687,0.0355603115440687,-0.01336031110822354,-0.01336031110822354,-0.01336031110822354,-0.01336031110822354,-0.013763606057310125,-0.013763606057310125,-0.013763606057310125,-0.013763606057310125,-0.013217409470922158,-0.013217409470922158,-0.013217409470922158,-0.013217409470922158,0.005997933178819326,0.005997933178819326,0.005997933178819326,0.005997933178819326,0.007575973751315642,0.007575973751315642,0.007575973751315642,0.007575973751315642,-0.027901259178698203,-0.027901259178698203,-0.027901259178698203,-0.027901259178698203,0.023832740052893032,0.023832740052893032,0.023832740052893032,0.023832740052893032,-0.08933916900967666,-0.08933916900967666,-0.08933916900967666,-0.08933916900967666,-0.01677981599958002,-0.01677981599958002,-0.01677981599958002,-0.01677981599958002,-0.06121866852794366,-0.06121866852794366,-0.06121866852794366,-0.06121866852794366,-0.057089809593409,-0.057089809593409,-0.057089809593409,-0.057089809593409,0.06367861771602795,0.06367861771602795,0.06367861771602795,0.06367861771602795,0.04325021507447879,0.04325021507447879,0.04325021507447879,0.04325021507447879,0.05739419601203314,0.05739419601203314,0.05739419601203314,0.05739419601203314,0.015624857411280713,0.015624857411280713,0.015624857411280713,0.015624857411280713,-0.007316417374861487,-0.007316417374861487,-0.007316417374861487,-0.007316417374861487,-0.00994616789066918,-0.00994616789066918,-0.00994616789066918,-0.00994616789066918,-0.07695039827251847,-0.07695039827251847,-0.07695039827251847,-0.07695039827251847,-0.02359416806219803,-0.02359416806219803,-0.02359416806219803,-0.02359416806219803]', '2026-05-02 12:50:19', NULL),
(60, 'ayser.dhifallah@gmail.com', '$2y$13$y/MJzsF8wdDZcuZFoIKZ2.Anw3cspzE.jX5d4O4U4hLFpce5s/c2u', 'aysser', 'ROLE_JOUEUR', 'aysser', NULL, NULL, NULL, NULL, NULL),
(61, 'aysser@gmail.com', '$2y$13$7Rg1/pl7CfDxVg20aBD24Oqua17wlKeO3GHy.NxumBLiPlBYUBdvC', 'aysser', 'ROLE_JOUEUR', 'aysser', NULL, NULL, NULL, '2026-05-02 23:47:18', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `user_saved_posts`
--

CREATE TABLE `user_saved_posts` (
  `user_id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `user_saved_posts`
--

INSERT INTO `user_saved_posts` (`user_id`, `post_id`) VALUES
(4, 18),
(4, 20),
(4, 23);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `announcements`
--
ALTER TABLE `announcements`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `candidature`
--
ALTER TABLE `candidature`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_E33BD3B86D861B89` (`equipe_id`),
  ADD KEY `FK_E33BD3B8A76ED395` (`user_id`);

--
-- Index pour la table `categorie`
--
ALTER TABLE `categorie`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `chat_message`
--
ALTER TABLE `chat_message`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_FAB3FC16A76ED395` (`user_id`),
  ADD KEY `IDX_FAB3FC166D861B89` (`equipe_id`);

--
-- Index pour la table `chat_messages`
--
ALTER TABLE `chat_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_chat_recipient_read` (`recipient_id`,`is_read`),
  ADD KEY `idx_chat_sender_recipient_created` (`sender_id`,`recipient_id`,`created_at`),
  ADD KEY `idx_chat_recipient_sender_created` (`recipient_id`,`sender_id`,`created_at`),
  ADD KEY `IDX_chat_sender` (`sender_id`),
  ADD KEY `IDX_chat_recipient` (`recipient_id`);

--
-- Index pour la table `commande`
--
ALTER TABLE `commande`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_COMMANDE_USER` (`user_id`),
  ADD KEY `IDX_COMMANDE_IDENTITY_KEY` (`identity_key`),
  ADD KEY `IDX_COMMANDE_AI_BLOCKED` (`ai_blocked`);

--
-- Index pour la table `commande_boutique`
--
ALTER TABLE `commande_boutique`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `commentaires`
--
ALTER TABLE `commentaires`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_commentaires_author` (`author_id`),
  ADD KEY `IDX_commentaires_post` (`post_id`);

--
-- Index pour la table `doctrine_migration_versions`
--
ALTER TABLE `doctrine_migration_versions`
  ADD PRIMARY KEY (`version`);

--
-- Index pour la table `equipe`
--
ALTER TABLE `equipe`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_2443196276C50E4A` (`manager_id`);

--
-- Index pour la table `event_participants`
--
ALTER TABLE `event_participants`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uniq_event_participant` (`user_id`,`post_id`),
  ADD KEY `IDX_event_participants_post` (`post_id`);

--
-- Index pour la table `feed_ai_analysis`
--
ALTER TABLE `feed_ai_analysis`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uniq_feed_ai_entity` (`entity_type`,`entity_id`),
  ADD KEY `idx_feed_ai_action` (`auto_action`),
  ADD KEY `idx_feed_ai_risk` (`toxicity_score`,`spam_score`,`hate_speech_score`);

--
-- Index pour la table `lignecommande`
--
ALTER TABLE `lignecommande`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `ligne_commande`
--
ALTER TABLE `ligne_commande`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_3170B74B82EA2E54` (`commande_id`),
  ADD KEY `IDX_3170B74BF347EFB` (`produit_id`);

--
-- Index pour la table `likes`
--
ALTER TABLE `likes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uniq_like_user_post` (`user_id`,`post_id`),
  ADD KEY `IDX_likes_post` (`post_id`);

--
-- Index pour la table `manager_request`
--
ALTER TABLE `manager_request`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_855ABA89A76ED395` (`user_id`);

--
-- Index pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750` (`queue_name`,`available_at`,`delivered_at`,`id`);

--
-- Index pour la table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_notifications_user_created` (`recipient_id`,`created_at`),
  ADD KEY `idx_notifications_user_read` (`recipient_id`,`is_read`);

--
-- Index pour la table `participation`
--
ALTER TABLE `participation`
  ADD PRIMARY KEY (`tournoi_id`,`user_id`),
  ADD KEY `IDX_PARTICIPATION_TOURNOI` (`tournoi_id`),
  ADD KEY `IDX_PARTICIPATION_USER` (`user_id`);

--
-- Index pour la table `participation_request`
--
ALTER TABLE `participation_request`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_PR_USER` (`user_id`),
  ADD KEY `IDX_PR_TOURNOI` (`tournoi_id`);

--
-- Index pour la table `password_reset_codes`
--
ALTER TABLE `password_reset_codes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_password_reset_email` (`email`);

--
-- Index pour la table `payment`
--
ALTER TABLE `payment`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_6D28840D82EA2E54` (`commande_id`);

--
-- Index pour la table `posts`
--
ALTER TABLE `posts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_posts_author` (`author_id`);

--
-- Index pour la table `post_media`
--
ALTER TABLE `post_media`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_post_media_post` (`post_id`);

--
-- Index pour la table `produit`
--
ALTER TABLE `produit`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_PRODUIT_USER` (`owner_user_id`),
  ADD KEY `FK_PRODUIT_EQUIPE` (`owner_equipe_id`),
  ADD KEY `FK_PRODUIT_CATEGORIE` (`categorie_id`);

--
-- Index pour la table `recommendation`
--
ALTER TABLE `recommendation`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_433224D2A76ED395` (`user_id`),
  ADD KEY `IDX_433224D2F347EFB` (`produit_id`);

--
-- Index pour la table `recrutement`
--
ALTER TABLE `recrutement`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_25EB23196D861B89` (`equipe_id`);

--
-- Index pour la table `resultat_tournoi`
--
ALTER TABLE `resultat_tournoi`
  ADD PRIMARY KEY (`id_resultat`),
  ADD UNIQUE KEY `UNIQ_EC3E38FF7E0950D9` (`id_tournoi`);

--
-- Index pour la table `team_reports`
--
ALTER TABLE `team_reports`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_E66D6726D861B89` (`equipe_id`),
  ADD KEY `idx_team_reports_reporter` (`reporter_id`);

--
-- Index pour la table `tournoi`
--
ALTER TABLE `tournoi`
  ADD PRIMARY KEY (`id_tournoi`),
  ADD KEY `IDX_TOURNOI_CREATOR` (`creator_id`);

--
-- Index pour la table `tournoi_match`
--
ALTER TABLE `tournoi_match`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_TOURNOI_MATCH_TOURNOI` (`tournoi_id`),
  ADD KEY `IDX_TOURNOI_MATCH_PLAYER_A` (`player_a_id`),
  ADD KEY `IDX_TOURNOI_MATCH_PLAYER_B` (`player_b_id`);

--
-- Index pour la table `tournoi_match_participant_result`
--
ALTER TABLE `tournoi_match_participant_result`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uniq_match_participant` (`match_id`,`participant_id`);

--
-- Index pour la table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_IDENTIFIER_EMAIL` (`email`);

--
-- Index pour la table `user_saved_posts`
--
ALTER TABLE `user_saved_posts`
  ADD PRIMARY KEY (`user_id`,`post_id`),
  ADD KEY `FK_saved_post` (`post_id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `announcements`
--
ALTER TABLE `announcements`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT pour la table `candidature`
--
ALTER TABLE `candidature`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- AUTO_INCREMENT pour la table `categorie`
--
ALTER TABLE `categorie`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `chat_message`
--
ALTER TABLE `chat_message`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT pour la table `chat_messages`
--
ALTER TABLE `chat_messages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT pour la table `commande`
--
ALTER TABLE `commande`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=94;

--
-- AUTO_INCREMENT pour la table `commande_boutique`
--
ALTER TABLE `commande_boutique`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `commentaires`
--
ALTER TABLE `commentaires`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT pour la table `equipe`
--
ALTER TABLE `equipe`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=80;

--
-- AUTO_INCREMENT pour la table `event_participants`
--
ALTER TABLE `event_participants`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `feed_ai_analysis`
--
ALTER TABLE `feed_ai_analysis`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=80;

--
-- AUTO_INCREMENT pour la table `lignecommande`
--
ALTER TABLE `lignecommande`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT pour la table `ligne_commande`
--
ALTER TABLE `ligne_commande`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=147;

--
-- AUTO_INCREMENT pour la table `likes`
--
ALTER TABLE `likes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT pour la table `manager_request`
--
ALTER TABLE `manager_request`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT pour la table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=401;

--
-- AUTO_INCREMENT pour la table `participation_request`
--
ALTER TABLE `participation_request`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- AUTO_INCREMENT pour la table `password_reset_codes`
--
ALTER TABLE `password_reset_codes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT pour la table `payment`
--
ALTER TABLE `payment`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT pour la table `posts`
--
ALTER TABLE `posts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=48;

--
-- AUTO_INCREMENT pour la table `post_media`
--
ALTER TABLE `post_media`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT pour la table `produit`
--
ALTER TABLE `produit`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT pour la table `recommendation`
--
ALTER TABLE `recommendation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `recrutement`
--
ALTER TABLE `recrutement`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `resultat_tournoi`
--
ALTER TABLE `resultat_tournoi`
  MODIFY `id_resultat` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `team_reports`
--
ALTER TABLE `team_reports`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `tournoi`
--
ALTER TABLE `tournoi`
  MODIFY `id_tournoi` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=46;

--
-- AUTO_INCREMENT pour la table `tournoi_match`
--
ALTER TABLE `tournoi_match`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=45;

--
-- AUTO_INCREMENT pour la table `tournoi_match_participant_result`
--
ALTER TABLE `tournoi_match_participant_result`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT pour la table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=62;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `candidature`
--
ALTER TABLE `candidature`
  ADD CONSTRAINT `FK_3E18E8ACA96FFD683` FOREIGN KEY (`equipe_id`) REFERENCES `equipe` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_E33BD3B8A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `chat_messages`
--
ALTER TABLE `chat_messages`
  ADD CONSTRAINT `FK_chat_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_chat_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
