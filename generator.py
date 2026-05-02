import re
import os

sql_dump = """
CREATE TABLE `announcements` (
  `id` int(11) NOT NULL,
  `title` varchar(180) NOT NULL,
  `content` longtext DEFAULT NULL,
  `tag` varchar(60) NOT NULL,
  `link` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `media_type` varchar(255) NOT NULL,
  `media_filename` varchar(255) DEFAULT NULL
);

CREATE TABLE `candidature` (
  `id` int(11) NOT NULL,
  `niveau` varchar(50) NOT NULL,
  `motivation` longtext NOT NULL,
  `statut` varchar(20) NOT NULL,
  `date_candidature` datetime NOT NULL,
  `reason` varchar(255) NOT NULL,
  `play_style` varchar(100) NOT NULL,
  `equipe_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL
);

CREATE TABLE `categorie` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL
);

CREATE TABLE `chat_message` (
  `id` int(11) NOT NULL,
  `message` longtext NOT NULL,
  `created_at` datetime NOT NULL,
  `is_read` tinyint(1) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `equipe_id` int(11) NOT NULL
);

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
  `adresse_detail` varchar(500) DEFAULT NULL
);

CREATE TABLE `commentaires` (
  `id` int(11) NOT NULL,
  `author_id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `content` longtext NOT NULL,
  `created_at` datetime NOT NULL
);

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
  `manager_id` int(11) DEFAULT NULL
);

CREATE TABLE `event_participants` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL
);

CREATE TABLE `ligne_commande` (
  `id` int(11) NOT NULL,
  `quantite` int(11) NOT NULL,
  `prix` int(11) NOT NULL,
  `commande_id` int(11) NOT NULL,
  `produit_id` int(11) NOT NULL
);

CREATE TABLE `likes` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL
);

CREATE TABLE `manager_request` (
  `id` int(11) NOT NULL,
  `motivation` longtext NOT NULL,
  `status` varchar(20) NOT NULL,
  `created_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `nom` varchar(255) DEFAULT NULL,
  `experience` longtext DEFAULT NULL
);

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `recipient_id` int(11) NOT NULL,
  `type` varchar(80) NOT NULL,
  `title` varchar(180) NOT NULL,
  `message` longtext NOT NULL,
  `link` varchar(255) DEFAULT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL
);

CREATE TABLE `participation` (
  `tournoi_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
);

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
);

CREATE TABLE `password_reset_codes` (
  `id` int(11) NOT NULL,
  `email` varchar(180) NOT NULL,
  `code_hash` varchar(255) NOT NULL,
  `expires_at` datetime NOT NULL,
  `created_at` datetime NOT NULL
);

CREATE TABLE `payment` (
  `id` int(11) NOT NULL,
  `amount` double NOT NULL,
  `created_at` datetime NOT NULL,
  `status` varchar(255) NOT NULL,
  `commande_id` int(11) NOT NULL
);

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
);

CREATE TABLE `post_media` (
  `id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `type` varchar(20) NOT NULL,
  `path` varchar(255) NOT NULL,
  `position` int(11) NOT NULL
);

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
);

CREATE TABLE `recommendation` (
  `id` int(11) NOT NULL,
  `score` double DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `produit_id` int(11) NOT NULL
);

CREATE TABLE `recrutement` (
  `id` int(11) NOT NULL,
  `nom_rec` varchar(255) NOT NULL,
  `description` longtext NOT NULL,
  `status` varchar(50) NOT NULL,
  `date_publication` datetime NOT NULL,
  `equipe_id` int(11) NOT NULL
);

CREATE TABLE `resultat_tournoi` (
  `id_resultat` int(11) NOT NULL,
  `rank` int(11) NOT NULL,
  `score` double NOT NULL,
  `id_tournoi` int(11) NOT NULL
);

CREATE TABLE `team_reports` (
  `id` int(11) NOT NULL,
  `reason` longtext NOT NULL,
  `created_at` datetime NOT NULL,
  `equipe_id` int(11) NOT NULL,
  `reporter_id` int(11) NOT NULL
);

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
);

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
);

CREATE TABLE `tournoi_match_participant_result` (
  `id` int(11) NOT NULL,
  `match_id` int(11) NOT NULL,
  `participant_id` int(11) NOT NULL,
  `placement` varchar(20) NOT NULL,
  `points` int(11) NOT NULL,
  `created_at` datetime NOT NULL
);

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `email` varchar(180) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `role` varchar(255) NOT NULL,
  `pseudo` varchar(100) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `face_descriptor` longtext DEFAULT NULL
);

CREATE TABLE `user_saved_posts` (
  `user_id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL
);
"""

import math

def to_camel_case(s, first_letter_upper=False):
    s = s.replace('`', '')
    parts = s.split('_')
    res = parts[0] + ''.join(x.title() for x in parts[1:])
    if first_letter_upper:
        res = res[0].upper() + res[1:]
    return res

def sql_type_to_java(sql_type):
    sql_type = sql_type.lower()
    if 'int' in sql_type:
        if 'tinyint' in sql_type:
            return 'boolean'
        return 'int'
    if 'double' in sql_type or 'decimal' in sql_type or 'float' in sql_type:
        return 'double'
    if 'datetime' in sql_type or 'timestamp' in sql_type:
        return 'java.sql.Timestamp'
    return 'String'

def parse_tables(sql):
    tables = {}
    current_table = None
    for line in sql.split('\\n'):
        line = line.strip()
        if line.startswith('CREATE TABLE'):
            match = re.search(r'CREATE TABLE `([^`]+)`', line)
            if match:
                current_table = match.group(1)
                tables[current_table] = []
        elif line.startswith('`') and current_table:
            match = re.search(r'`([^`]+)`\s+([a-zA-Z0-9_]+)', line)
            if match:
                col_name = match.group(1)
                col_type = sql_type_to_java(match.group(2))
                tables[current_table].append({'col': col_name, 'java_name': to_camel_case(col_name), 'type': col_type})
    return tables

tables = parse_tables(sql_dump)

base_path_models = 'C:/Users/ilyes/Downloads/Workshop-JDBC-JavaFX/src/main/java/models'
base_path_services = 'C:/Users/ilyes/Downloads/Workshop-JDBC-JavaFX/src/main/java/services'

os.makedirs(base_path_models, exist_ok=True)
os.makedirs(base_path_services, exist_ok=True)

for table_name, columns in tables.items():
    class_name = to_camel_case(table_name, True)
    
    id_col = None
    for c in columns:
        if c['col'].startswith('id'):
            id_col = c
            break
            
    # Generate Model
    model_code = f"package models;\\n\\n"
    if any(c['type'] == 'java.sql.Timestamp' for c in columns):
        model_code += "import java.sql.Timestamp;\\n\\n"
    
    model_code += f"public class {class_name} {{\\n"
    
    for c in columns:
        model_code += f"    private {c['type']} {c['java_name']};\\n"
        
    model_code += "\\n    public " + class_name + "() {}\\n"
    
    model_code += "\\n    public " + class_name + "(" + ", ".join([f"{c['type']} {c['java_name']}" for c in columns]) + ") {\\n"
    for c in columns:
        model_code += f"        this.{c['java_name']} = {c['java_name']};\\n"
    model_code += "    }\\n"
    
    if id_col:
        cols_no_id = [c for c in columns if c != id_col]
        if cols_no_id:
            model_code += "\\n    public " + class_name + "(" + ", ".join([f"{c['type']} {c['java_name']}" for c in cols_no_id]) + ") {\\n"
            for c in cols_no_id:
                model_code += f"        this.{c['java_name']} = {c['java_name']};\\n"
            model_code += "    }\\n"
        
    for c in columns:
        model_code += f"\\n    public {c['type']} get{to_camel_case(c['col'], True)}() {{\\n        return {c['java_name']};\\n    }}\\n"
        model_code += f"\\n    public void set{to_camel_case(c['col'], True)}({c['type']} {c['java_name']}) {{\\n        this.{c['java_name']} = {c['java_name']};\\n    }}\\n"
        
    model_code += "\\n    @Override\\n    public String toString() {\\n"
    model_code += f'        return "{class_name}{{" +\\n'
    for iter_idx, c in enumerate(columns):
        sep = ' + ", ' if iter_idx > 0 else ' '
        quote = "\\'" if c['type'] == 'String' else ""
        model_code += f'                "{sep}{c["java_name"]}={quote}" + {c["java_name"]} + "{quote}" +\n'
    model_code += '                "\\'}";\\n    }\\n}\\n'
    
    with open(os.path.join(base_path_models, class_name + '.java'), 'w', encoding='utf-8') as f:
        f.write(model_code)

    # Generate Service
    service_code = f"package services;\\n\\n"
    service_code += f"import models.{class_name};\\n"
    service_code += f"import utils.MyDatabase;\\n\\n"
    service_code += f"import java.sql.*;\\n"
    service_code += f"import java.util.ArrayList;\\n"
    service_code += f"import java.util.List;\\n\\n"
    service_code += f"public class {class_name}Service implements IService<{class_name}> {{\\n\\n"
    service_code += f"    private Connection connection;\\n\\n"
    service_code += f"    public {class_name}Service() {{\\n"
    service_code += f"        connection = MyDatabase.getInstance().getConnection();\\n"
    service_code += f"    }}\\n\\n"
    
    # ajouter
    cols_insert = [c for c in columns if c != id_col] if id_col else columns
    col_names = ",".join([f"`{c['col']}`" for c in cols_insert])
    q_marks = ",".join(["?" for _ in cols_insert])
    service_code += f"    @Override\\n    public void ajouter({class_name} obj) throws SQLException {{\\n"
    service_code += f'        String sql = "insert into `{table_name}` ({col_names}) values({q_marks})";\\n'
    service_code += f"        PreparedStatement preparedStatement = connection.prepareStatement(sql);\\n"
    for i, c in enumerate(cols_insert):
        if c['type'] == 'int':
            service_code += f"        preparedStatement.setInt({i+1}, obj.get{to_camel_case(c['col'], True)}());\\n"
        elif c['type'] == 'double':
            service_code += f"        preparedStatement.setDouble({i+1}, obj.get{to_camel_case(c['col'], True)}());\\n"
        elif c['type'] == 'boolean':
            service_code += f"        preparedStatement.setBoolean({i+1}, obj.get{to_camel_case(c['col'], True)}());\\n"
        elif c['type'] == 'java.sql.Timestamp':
            service_code += f"        preparedStatement.setTimestamp({i+1}, obj.get{to_camel_case(c['col'], True)}());\\n"
        else:
            service_code += f"        preparedStatement.setString({i+1}, obj.get{to_camel_case(c['col'], True)}());\\n"
    service_code += f"        preparedStatement.executeUpdate();\\n"
    service_code += f"    }}\\n\\n"

    # modifier
    if id_col:
        set_clause = ", ".join([f"`{c['col']}` = ?" for c in cols_insert])
        service_code += f"    @Override\\n    public void modifier({class_name} obj) throws SQLException {{\\n"
        service_code += f'        String sql = "update `{table_name}` set {set_clause} where `{id_col["col"]}` = ?";\\n'
        service_code += f"        PreparedStatement preparedStatement = connection.prepareStatement(sql);\\n"
        for i, c in enumerate(cols_insert):
            if c['type'] == 'int':
                service_code += f"        preparedStatement.setInt({i+1}, obj.get{to_camel_case(c['col'], True)}());\\n"
            elif c['type'] == 'double':
                service_code += f"        preparedStatement.setDouble({i+1}, obj.get{to_camel_case(c['col'], True)}());\\n"
            elif c['type'] == 'boolean':
                service_code += f"        preparedStatement.setBoolean({i+1}, obj.get{to_camel_case(c['col'], True)}());\\n"
            elif c['type'] == 'java.sql.Timestamp':
                service_code += f"        preparedStatement.setTimestamp({i+1}, obj.get{to_camel_case(c['col'], True)}());\\n"
            else:
                service_code += f"        preparedStatement.setString({i+1}, obj.get{to_camel_case(c['col'], True)}());\\n"
        
        id_idx = len(cols_insert) + 1
        service_code += f"        preparedStatement.setInt({id_idx}, obj.get{to_camel_case(id_col['col'], True)}());\\n"
        service_code += f"        preparedStatement.executeUpdate();\\n"
        service_code += f"    }}\\n\\n"
        
        # supprimer
        service_code += f"    @Override\\n    public void supprimer(int id) throws SQLException {{\\n"
        service_code += f'        String sql = "delete from `{table_name}` where `{id_col["col"]}` = ?";\\n'
        service_code += f"        PreparedStatement preparedStatement = connection.prepareStatement(sql);\\n"
        service_code += f"        preparedStatement.setInt(1, id);\\n"
        service_code += f"        preparedStatement.executeUpdate();\\n"
        service_code += f"    }}\\n\\n"
    else:
        # Tables without explicit ID, implement dummy overrides
        service_code += f"    @Override\\n    public void modifier({class_name} obj) throws SQLException {{\\n"
        service_code += f'        // TODO: Implement modifier for table without explicit ID\\n'
        service_code += f"    }}\\n\\n"
        service_code += f"    @Override\\n    public void supprimer(int id) throws SQLException {{\\n"
        service_code += f'        // TODO: Implement supprimer for table without explicit ID\\n'
        service_code += f"    }}\\n\\n"

    # recuperer
    service_code += f"    @Override\\n    public List<{class_name}> recuperer() throws SQLException {{\\n"
    service_code += f'        String sql = "select * from `{table_name}`";\\n'
    service_code += f"        Statement statement = connection.createStatement();\\n"
    service_code += f"        ResultSet rs = statement.executeQuery(sql);\\n"
    service_code += f"        List<{class_name}> list = new ArrayList<>();\\n"
    service_code += f"        while (rs.next()) {{\\n"
    service_code += f"            {class_name} obj = new {class_name}();\\n"
    for c in columns:
        if c['type'] == 'int':
            service_code += f"            obj.set{to_camel_case(c['col'], True)}(rs.getInt(\"{c['col']}\"));\\n"
        elif c['type'] == 'double':
            service_code += f"            obj.set{to_camel_case(c['col'], True)}(rs.getDouble(\"{c['col']}\"));\\n"
        elif c['type'] == 'boolean':
            service_code += f"            obj.set{to_camel_case(c['col'], True)}(rs.getBoolean(\"{c['col']}\"));\\n"
        elif c['type'] == 'java.sql.Timestamp':
            service_code += f"            obj.set{to_camel_case(c['col'], True)}(rs.getTimestamp(\"{c['col']}\"));\\n"
        else:
            service_code += f"            obj.set{to_camel_case(c['col'], True)}(rs.getString(\"{c['col']}\"));\\n"
    service_code += f"            list.add(obj);\\n"
    service_code += f"        }}\\n"
    service_code += f"        return list;\\n"
    service_code += f"    }}\\n"
    
    service_code += f"}}\\n"

    with open(os.path.join(base_path_services, class_name + 'Service.java'), 'w', encoding='utf-8') as f:
        f.write(service_code)

print("Java files generated successfully.")
