import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Generator {
    static String sqlDump = "CREATE TABLE `announcements` (\n`id` int(11) NOT NULL,\n`title` varchar(180) NOT NULL,\n`content` longtext DEFAULT NULL,\n`tag` varchar(60) NOT NULL,\n`link` varchar(255) DEFAULT NULL,\n`created_at` datetime NOT NULL,\n`media_type` varchar(255) NOT NULL,\n`media_filename` varchar(255) DEFAULT NULL\n);\n" +
            "CREATE TABLE `candidature` (\n`id` int(11) NOT NULL,\n`niveau` varchar(50) NOT NULL,\n`motivation` longtext NOT NULL,\n`statut` varchar(20) NOT NULL,\n`date_candidature` datetime NOT NULL,\n`reason` varchar(255) NOT NULL,\n`play_style` varchar(100) NOT NULL,\n`equipe_id` int(11) NOT NULL,\n`user_id` int(11) DEFAULT NULL\n);\n" +
            "CREATE TABLE `categorie` (\n`id` int(11) NOT NULL,\n`nom` varchar(255) NOT NULL\n);\n" +
            "CREATE TABLE `chat_message` (\n`id` int(11) NOT NULL,\n`message` longtext NOT NULL,\n`created_at` datetime NOT NULL,\n`is_read` tinyint(1) NOT NULL,\n`user_id` int(11) DEFAULT NULL,\n`equipe_id` int(11) NOT NULL\n);\n" +
            "CREATE TABLE `commande` (\n`id` int(11) NOT NULL,\n`nom` varchar(255) DEFAULT NULL,\n`prenom` varchar(255) DEFAULT NULL,\n`adresse` varchar(255) DEFAULT NULL,\n`quantite` int(11) DEFAULT NULL,\n`numtel` int(11) DEFAULT NULL,\n`statut` varchar(255) NOT NULL,\n`pays` varchar(255) DEFAULT NULL,\n`gouvernerat` varchar(255) DEFAULT NULL,\n`code_postal` varchar(20) DEFAULT NULL,\n`adresse_detail` varchar(500) DEFAULT NULL\n);\n" +
            "CREATE TABLE `commentaires` (\n`id` int(11) NOT NULL,\n`author_id` int(11) NOT NULL,\n`post_id` int(11) NOT NULL,\n`content` longtext NOT NULL,\n`created_at` datetime NOT NULL\n);\n" +
            "CREATE TABLE `equipe` (\n`id` int(11) NOT NULL,\n`nom_equipe` varchar(255) NOT NULL,\n`logo` varchar(255) DEFAULT NULL,\n`description` longtext NOT NULL,\n`date_creation` datetime NOT NULL,\n`classement` varchar(50) NOT NULL,\n`tag` varchar(255) DEFAULT NULL,\n`region` varchar(100) DEFAULT NULL,\n`max_members` int(11) NOT NULL DEFAULT 5,\n`is_private` tinyint(1) NOT NULL DEFAULT 0,\n`is_active` tinyint(1) NOT NULL DEFAULT 1,\n`manager_id` int(11) DEFAULT NULL\n);\n" +
            "CREATE TABLE `event_participants` (\n`id` int(11) NOT NULL,\n`user_id` int(11) NOT NULL,\n`post_id` int(11) NOT NULL,\n`created_at` datetime NOT NULL\n);\n" +
            "CREATE TABLE `ligne_commande` (\n`id` int(11) NOT NULL,\n`quantite` int(11) NOT NULL,\n`prix` int(11) NOT NULL,\n`commande_id` int(11) NOT NULL,\n`produit_id` int(11) NOT NULL\n);\n" +
            "CREATE TABLE `likes` (\n`id` int(11) NOT NULL,\n`user_id` int(11) NOT NULL,\n`post_id` int(11) NOT NULL,\n`created_at` datetime NOT NULL\n);\n" +
            "CREATE TABLE `manager_request` (\n`id` int(11) NOT NULL,\n`motivation` longtext NOT NULL,\n`status` varchar(20) NOT NULL,\n`created_at` datetime NOT NULL,\n`user_id` int(11) NOT NULL,\n`nom` varchar(255) DEFAULT NULL,\n`experience` longtext DEFAULT NULL\n);\n" +
            "CREATE TABLE `notifications` (\n`id` int(11) NOT NULL,\n`recipient_id` int(11) NOT NULL,\n`type` varchar(80) NOT NULL,\n`title` varchar(180) NOT NULL,\n`message` longtext NOT NULL,\n`link` varchar(255) DEFAULT NULL,\n`is_read` tinyint(1) NOT NULL DEFAULT 0,\n`created_at` datetime NOT NULL\n);\n" +
            "CREATE TABLE `participation` (\n`tournoi_id` int(11) NOT NULL,\n`user_id` int(11) NOT NULL\n);\n" +
            "CREATE TABLE `participation_request` (\n`id` int(11) NOT NULL,\n`user_id` int(11) DEFAULT NULL,\n`tournoi_id` int(11) NOT NULL,\n`status` varchar(20) NOT NULL,\n`message` longtext DEFAULT NULL,\n`player_level` varchar(20) DEFAULT NULL,\n`rules_accepted` tinyint(1) NOT NULL,\n`applicant_name` varchar(255) DEFAULT NULL,\n`applicant_email` varchar(255) DEFAULT NULL,\n`created_at` datetime NOT NULL\n);\n" +
            "CREATE TABLE `password_reset_codes` (\n`id` int(11) NOT NULL,\n`email` varchar(180) NOT NULL,\n`code_hash` varchar(255) NOT NULL,\n`expires_at` datetime NOT NULL,\n`created_at` datetime NOT NULL\n);\n" +
            "CREATE TABLE `payment` (\n`id` int(11) NOT NULL,\n`amount` double NOT NULL,\n`created_at` datetime NOT NULL,\n`status` varchar(255) NOT NULL,\n`commande_id` int(11) NOT NULL\n);\n" +
            "CREATE TABLE `posts` (\n`id` int(11) NOT NULL,\n`content` longtext DEFAULT NULL,\n`media_type` varchar(255) NOT NULL DEFAULT 'text',\n`media_filename` varchar(255) DEFAULT NULL,\n`created_at` datetime NOT NULL,\n`image_path` varchar(255) DEFAULT NULL,\n`video_url` varchar(255) DEFAULT NULL,\n`is_event` tinyint(1) NOT NULL DEFAULT 0,\n`event_title` varchar(180) DEFAULT NULL,\n`event_date` datetime DEFAULT NULL,\n`event_location` varchar(255) DEFAULT NULL,\n`max_participants` int(11) DEFAULT NULL,\n`author_id` int(11) DEFAULT NULL\n);\n" +
            "CREATE TABLE `post_media` (\n`id` int(11) NOT NULL,\n`post_id` int(11) NOT NULL,\n`type` varchar(20) NOT NULL,\n`path` varchar(255) NOT NULL,\n`position` int(11) NOT NULL\n);\n" +
            "CREATE TABLE `produit` (\n`id` int(11) NOT NULL,\n`nom` varchar(255) NOT NULL,\n`prix` int(11) NOT NULL,\n`stock` int(11) NOT NULL,\n`description` varchar(255) NOT NULL,\n`image` varchar(255) NOT NULL,\n`active` tinyint(1) NOT NULL,\n`statut` varchar(50) NOT NULL DEFAULT 'disponible',\n`owner_user_id` int(11) DEFAULT NULL,\n`owner_equipe_id` int(11) DEFAULT NULL,\n`categorie_id` int(11) DEFAULT NULL,\n`video_url` varchar(255) DEFAULT NULL,\n`technical_specs` text DEFAULT NULL,\n`install_difficulty` varchar(50) DEFAULT NULL\n);\n" +
            "CREATE TABLE `recommendation` (\n`id` int(11) NOT NULL,\n`score` double DEFAULT NULL,\n`created_at` datetime NOT NULL,\n`user_id` int(11) NOT NULL,\n`produit_id` int(11) NOT NULL\n);\n" +
            "CREATE TABLE `recrutement` (\n`id` int(11) NOT NULL,\n`nom_rec` varchar(255) NOT NULL,\n`description` longtext NOT NULL,\n`status` varchar(50) NOT NULL,\n`date_publication` datetime NOT NULL,\n`equipe_id` int(11) NOT NULL\n);\n" +
            "CREATE TABLE `resultat_tournoi` (\n`id_resultat` int(11) NOT NULL,\n`rank` int(11) NOT NULL,\n`score` double NOT NULL,\n`id_tournoi` int(11) NOT NULL\n);\n" +
            "CREATE TABLE `team_reports` (\n`id` int(11) NOT NULL,\n`reason` longtext NOT NULL,\n`created_at` datetime NOT NULL,\n`equipe_id` int(11) NOT NULL,\n`reporter_id` int(11) NOT NULL\n);\n" +
            "CREATE TABLE `tournoi` (\n`id_tournoi` int(11) NOT NULL,\n`creator_id` int(11) NOT NULL,\n`name` varchar(255) NOT NULL,\n`type_tournoi` varchar(50) NOT NULL,\n`type_game` varchar(50) NOT NULL,\n`game` varchar(255) NOT NULL,\n`start_date` datetime NOT NULL,\n`end_date` datetime NOT NULL,\n`status` varchar(50) NOT NULL,\n`prize_won` double NOT NULL,\n`max_places` int(11) DEFAULT NULL\n);\n" +
            "CREATE TABLE `tournoi_match` (\n`id` int(11) NOT NULL,\n`tournoi_id` int(11) NOT NULL,\n`player_a_id` int(11) DEFAULT NULL,\n`player_b_id` int(11) DEFAULT NULL,\n`scheduled_at` datetime DEFAULT NULL,\n`status` varchar(30) NOT NULL,\n`score_a` int(11) DEFAULT NULL,\n`score_b` int(11) DEFAULT NULL,\n`created_at` datetime NOT NULL,\n`home_name` varchar(255) DEFAULT NULL,\n`away_name` varchar(255) DEFAULT NULL\n);\n" +
            "CREATE TABLE `tournoi_match_participant_result` (\n`id` int(11) NOT NULL,\n`match_id` int(11) NOT NULL,\n`participant_id` int(11) NOT NULL,\n`placement` varchar(20) NOT NULL,\n`points` int(11) NOT NULL,\n`created_at` datetime NOT NULL\n);\n" +
            "CREATE TABLE `user` (\n`id` int(11) NOT NULL,\n`email` varchar(180) NOT NULL,\n`password` varchar(255) NOT NULL,\n`nom` varchar(100) NOT NULL,\n`role` varchar(255) NOT NULL,\n`pseudo` varchar(100) DEFAULT NULL,\n`avatar` varchar(255) DEFAULT NULL,\n`face_descriptor` longtext DEFAULT NULL\n);\n" +
            "CREATE TABLE `user_saved_posts` (\n`user_id` int(11) NOT NULL,\n`post_id` int(11) NOT NULL\n);\n";

    public static String toCamelCase(String s, boolean firstLetterUpper) {
        String[] parts = s.replace("`", "").split("_");
        StringBuilder res = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            res.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
        }
        if (firstLetterUpper) {
            return res.substring(0, 1).toUpperCase() + res.substring(1);
        }
        return res.toString();
    }

    public static String sqlTypeToJava(String sqlType) {
        sqlType = sqlType.toLowerCase();
        if (sqlType.contains("int")) {
            if (sqlType.contains("tinyint")) return "boolean";
            return "int";
        }
        if (sqlType.contains("double") || sqlType.contains("decimal") || sqlType.contains("float")) return "double";
        if (sqlType.contains("datetime") || sqlType.contains("timestamp")) return "java.sql.Timestamp";
        return "String";
    }

    public static class Column {
        public String col;
        public String javaName;
        public String type;

        public Column(String col, String javaName, String type) {
            this.col = col;
            this.javaName = javaName;
            this.type = type;
        }
    }

    public static void main(String[] args) throws IOException {
        Map<String, List<Column>> tables = new HashMap<>();
        String currentTable = null;
        for (String line : sqlDump.split("\n")) {
            line = line.trim();
            if (line.startsWith("CREATE TABLE")) {
                Matcher m = Pattern.compile("CREATE TABLE `([^`]+)`").matcher(line);
                if (m.find()) {
                    currentTable = m.group(1);
                    tables.put(currentTable, new ArrayList<>());
                }
            } else if (line.startsWith("`") && currentTable != null) {
                Matcher m = Pattern.compile("`([^`]+)`\\s+([a-zA-Z0-9_]+)").matcher(line);
                if (m.find()) {
                    String colName = m.group(1);
                    String colType = sqlTypeToJava(m.group(2));
                    tables.get(currentTable).add(new Column(colName, toCamelCase(colName, false), colType));
                }
            }
        }

        String basePathModels = "src/main/java/models";
        String basePathServices = "src/main/java/services";

        new File(basePathModels).mkdirs();
        new File(basePathServices).mkdirs();

        for (Map.Entry<String, List<Column>> entry : tables.entrySet()) {
            String tableName = entry.getKey();
            List<Column> columns = entry.getValue();
            String className = toCamelCase(tableName, true);

            Column idCol = null;
            for (Column c : columns) {
                if (c.col.startsWith("id")) {
                    idCol = c;
                    break;
                }
            }

            StringBuilder modelCode = new StringBuilder("package models;\n\n");
            boolean hasTimestamp = columns.stream().anyMatch(c -> c.type.equals("java.sql.Timestamp"));
            if (hasTimestamp) modelCode.append("import java.sql.Timestamp;\n\n");

            modelCode.append("public class ").append(className).append(" {\n");

            for (Column c : columns) {
                modelCode.append("    private ").append(c.type).append(" ").append(c.javaName).append(";\n");
            }

            modelCode.append("\n    public ").append(className).append("() {}\n");

            modelCode.append("\n    public ").append(className).append("(");
            List<String> allParams = new ArrayList<>();
            for (Column c : columns) allParams.add(c.type + " " + c.javaName);
            modelCode.append(String.join(", ", allParams)).append(") {\n");
            for (Column c : columns) {
                modelCode.append("        this.").append(c.javaName).append(" = ").append(c.javaName).append(";\n");
            }
            modelCode.append("    }\n");

            if (idCol != null && columns.size() > 1) {
                List<String> noIdParams = new ArrayList<>();
                for (Column c : columns) if (c != idCol) noIdParams.add(c.type + " " + c.javaName);
                if (!noIdParams.isEmpty()) {
                    modelCode.append("\n    public ").append(className).append("(");
                    modelCode.append(String.join(", ", noIdParams)).append(") {\n");
                    for (Column c : columns) {
                        if (c != idCol) modelCode.append("        this.").append(c.javaName).append(" = ").append(c.javaName).append(";\n");
                    }
                    modelCode.append("    }\n");
                }
            }

            for (Column c : columns) {
                modelCode.append("\n    public ").append(c.type).append(" get").append(toCamelCase(c.col, true)).append("() {\n        return ").append(c.javaName).append(";\n    }\n");
                modelCode.append("\n    public void set").append(toCamelCase(c.col, true)).append("(").append(c.type).append(" ").append(c.javaName).append(") {\n        this.").append(c.javaName).append(" = ").append(c.javaName).append(";\n    }\n");
            }

            modelCode.append("\n    @Override\n    public String toString() {\n");
            modelCode.append("        return \"").append(className).append("{\" +\n");
            for (int i = 0; i < columns.size(); i++) {
                Column c = columns.get(i);
                String sep = (i > 0) ? ", " : " ";
                String quote = c.type.equals("String") ? "\\'" : "";
                modelCode.append("                \"").append(sep).append(c.javaName).append("=").append(quote).append("\" + ").append(c.javaName).append(" + \"").append(quote).append("\" +\n");
            }
            modelCode.append("                \"}\";\n    }\n}\n");

            try (FileWriter w = new FileWriter(new File(basePathModels, className + ".java"))) {
                w.write(modelCode.toString());
            }

            // Service
            StringBuilder srv = new StringBuilder("package services;\n\n");
            srv.append("import models.").append(className).append(";\n");
            srv.append("import utils.MyDatabase;\n\n");
            srv.append("import java.sql.*;\n");
            srv.append("import java.util.ArrayList;\n");
            srv.append("import java.util.List;\n\n");
            srv.append("public class ").append(className).append("Service implements IService<").append(className).append("> {\n\n");
            srv.append("    private Connection connection;\n\n");
            srv.append("    public ").append(className).append("Service() {\n");
            srv.append("        connection = MyDatabase.getInstance().getConnection();\n");
            srv.append("    }\n\n");

            List<Column> colsInsert = new ArrayList<>();
            for (Column c : columns) if (c != idCol) colsInsert.add(c);
            if (colsInsert.isEmpty()) colsInsert = columns;

            List<String> colNames = new ArrayList<>();
            List<String> qMarks = new ArrayList<>();
            for (Column c : colsInsert) { colNames.add("`" + c.col + "`"); qMarks.add("?"); }

            srv.append("    @Override\n    public void ajouter(").append(className).append(" obj) throws SQLException {\n");
            srv.append("        String sql = \"insert into `").append(tableName).append("` (");
            srv.append(String.join(",", colNames)).append(") values(").append(String.join(",", qMarks)).append(")\";\n");
            srv.append("        PreparedStatement ps = connection.prepareStatement(sql);\n");
            int j = 1;
            for (Column c : colsInsert) {
                if (c.type.equals("int")) srv.append("        ps.setInt(").append(j++).append(", obj.get").append(toCamelCase(c.col, true)).append("());\n");
                else if (c.type.equals("double")) srv.append("        ps.setDouble(").append(j++).append(", obj.get").append(toCamelCase(c.col, true)).append("());\n");
                else if (c.type.equals("boolean")) srv.append("        ps.setBoolean(").append(j++).append(", obj.get").append(toCamelCase(c.col, true)).append("());\n");
                else if (c.type.equals("java.sql.Timestamp")) srv.append("        ps.setTimestamp(").append(j++).append(", obj.get").append(toCamelCase(c.col, true)).append("());\n");
                else srv.append("        ps.setString(").append(j++).append(", obj.get").append(toCamelCase(c.col, true)).append("());\n");
            }
            srv.append("        ps.executeUpdate();\n    }\n\n");

            if (idCol != null) {
                srv.append("    @Override\n    public void modifier(").append(className).append(" obj) throws SQLException {\n");
                List<String> sets = new ArrayList<>();
                for (Column c : colsInsert) sets.add("`" + c.col + "` = ?");
                srv.append("        String sql = \"update `").append(tableName).append("` set ").append(String.join(", ", sets)).append(" where `").append(idCol.col).append("` = ?\";\n");
                srv.append("        PreparedStatement ps = connection.prepareStatement(sql);\n");
                j = 1;
                for (Column c : colsInsert) {
                    if (c.type.equals("int")) srv.append("        ps.setInt(").append(j++).append(", obj.get").append(toCamelCase(c.col, true)).append("());\n");
                    else if (c.type.equals("double")) srv.append("        ps.setDouble(").append(j++).append(", obj.get").append(toCamelCase(c.col, true)).append("());\n");
                    else if (c.type.equals("boolean")) srv.append("        ps.setBoolean(").append(j++).append(", obj.get").append(toCamelCase(c.col, true)).append("());\n");
                    else if (c.type.equals("java.sql.Timestamp")) srv.append("        ps.setTimestamp(").append(j++).append(", obj.get").append(toCamelCase(c.col, true)).append("());\n");
                    else srv.append("        ps.setString(").append(j++).append(", obj.get").append(toCamelCase(c.col, true)).append("());\n");
                }
                srv.append("        ps.setInt(").append(j).append(", obj.get").append(toCamelCase(idCol.col, true)).append("());\n");
                srv.append("        ps.executeUpdate();\n    }\n\n");

                srv.append("    @Override\n    public void supprimer(int id) throws SQLException {\n");
                srv.append("        String sql = \"delete from `").append(tableName).append("` where `").append(idCol.col).append("` = ?\";\n");
                srv.append("        PreparedStatement ps = connection.prepareStatement(sql);\n        ps.setInt(1, id);\n        ps.executeUpdate();\n    }\n\n");
            } else {
                srv.append("    @Override\n    public void modifier(").append(className).append(" obj) throws SQLException {\n        // TODO\n    }\n\n");
                srv.append("    @Override\n    public void supprimer(int id) throws SQLException {\n        // TODO\n    }\n\n");
            }

            srv.append("    @Override\n    public List<").append(className).append("> recuperer() throws SQLException {\n");
            srv.append("        String sql = \"select * from `").append(tableName).append("`\";\n");
            srv.append("        Statement statement = connection.createStatement();\n");
            srv.append("        ResultSet rs = statement.executeQuery(sql);\n");
            srv.append("        List<").append(className).append("> list = new ArrayList<>();\n");
            srv.append("        while (rs.next()) {\n");
            srv.append("            ").append(className).append(" obj = new ").append(className).append("();\n");
            for (Column c : columns) {
                if (c.type.equals("int")) srv.append("            obj.set").append(toCamelCase(c.col, true)).append("(rs.getInt(\"").append(c.col).append("\"));\n");
                else if (c.type.equals("double")) srv.append("            obj.set").append(toCamelCase(c.col, true)).append("(rs.getDouble(\"").append(c.col).append("\"));\n");
                else if (c.type.equals("boolean")) srv.append("            obj.set").append(toCamelCase(c.col, true)).append("(rs.getBoolean(\"").append(c.col).append("\"));\n");
                else if (c.type.equals("java.sql.Timestamp")) srv.append("            obj.set").append(toCamelCase(c.col, true)).append("(rs.getTimestamp(\"").append(c.col).append("\"));\n");
                else srv.append("            obj.set").append(toCamelCase(c.col, true)).append("(rs.getString(\"").append(c.col).append("\"));\n");
            }
            srv.append("            list.add(obj);\n        }\n        return list;\n    }\n}\n");

            try (FileWriter w = new FileWriter(new File(basePathServices, className + "Service.java"))) {
                w.write(srv.toString());
            }
        }
        System.out.println("Java generation completed.");
    }
}
