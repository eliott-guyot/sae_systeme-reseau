import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ScoreManager {

    private static final String FILE_NAME = "scores.json";

    // Méthode pour lire le contenu d'un fichier
    private String readFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        reader.close();
        return content.toString();
    }

    // Méthode pour sauvegarder dans un fichier
    private void saveFile(String fileName, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(content);
        writer.close();
    }

    // Méthode pour transformer une chaîne de Map en JSON (très basique)
    private String toJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof Map) {
                json.append(toJson((Map) entry.getValue()));
            } else {
                json.append("\"").append(entry.getValue()).append("\"");
            }
            json.append(",");
        }
        if (json.length() > 1) {
            json.deleteCharAt(json.length() - 1); // Enlever la dernière virgule
        }
        json.append("}");
        return json.toString();
    }

    // Méthode pour parser un JSON en une Map basique (très basique)
    private Map<String, Object> parseJson(String json) {
        Map<String, Object> map = new HashMap<>();
        json = json.substring(1, json.length() - 1); // Enlever les crochets {}
        String[] entries = json.split(",");
        for (String entry : entries) {
            String[] keyValue = entry.split(":");
            String key = keyValue[0].replace("\"", "").trim();
            String value = keyValue[1].replace("\"", "").trim();
            Map<String, Integer> playerData = new HashMap<>();
            String[] playerDataValues = value.split(",");
            for (String data : playerDataValues) {
                String[] scoreData = data.split(":");
                String scoreKey = scoreData[0].replace("\"", "").trim();
                int scoreValue = Integer.parseInt(scoreData[1].trim());
                playerData.put(scoreKey, scoreValue);
            }
            map.put(key, playerData);
        }
        return map;
    }

    // Méthode pour mettre à jour les scores après un match
    public void updateScores(String player1Name, String player2Name, String result) {
        // Résultat : "victoire", "defaite", ou "nul"
        int player1Victories = 0, player1Defeats = 0;
        int player2Victories = 0, player2Defeats = 0;

        // Charger les scores existants
        File file = new File(FILE_NAME);
        Map<String, Object> scores;
        try {
            if (file.exists()) {
                String content = readFile(FILE_NAME);
                scores = parseJson(content);
            } else {
                scores = new HashMap<>();
            }

            // Récupérer les scores actuels des joueurs
            if (scores.containsKey(player1Name)) {
                Map<String, Integer> player1Data = (Map<String, Integer>) scores.get(player1Name);
                player1Victories = player1Data.getOrDefault("victories", 0);
                player1Defeats = player1Data.getOrDefault("defeats", 0);
            } else {
                scores.put(player1Name, new HashMap<>());
            }

            if (scores.containsKey(player2Name)) {
                Map<String, Integer> player2Data = (Map<String, Integer>) scores.get(player2Name);
                player2Victories = player2Data.getOrDefault("victories", 0);
                player2Defeats = player2Data.getOrDefault("defeats", 0);
            } else {
                scores.put(player2Name, new HashMap<>());
            }

            // Mettre à jour les scores en fonction du résultat
            if (result.equals("victoire")) {
                player1Victories++;
                player2Defeats++;
            } else if (result.equals("defaite")) {
                player1Defeats++;
                player2Victories++;
            } else if (result.equals("nul")) {
                // Pas de changement de victoires ou défaites, mais vous pouvez ajouter des "draws" si vous voulez
            }

            // Sauvegarder les scores mis à jour dans la Map
            Map<String, Integer> player1Score = new HashMap<>();
            player1Score.put("victories", player1Victories);
            player1Score.put("defeats", player1Defeats);
            scores.put(player1Name, player1Score);

            Map<String, Integer> player2Score = new HashMap<>();
            player2Score.put("victories", player2Victories);
            player2Score.put("defeats", player2Defeats);
            scores.put(player2Name, player2Score);

            // Sauvegarder les modifications dans le fichier
            saveFile(FILE_NAME, toJson(scores));

        } catch (IOException e) {
            System.out.println("Erreur lors de la gestion des scores : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ScoreManager scoreManager = new ScoreManager();

        // Exemple de mise à jour après une victoire de Player1 contre Player2
        scoreManager.updateScores("Player1", "Player2", "victoire"); // Player1 gagne
        scoreManager.updateScores("Player1", "Player2", "defaite"); // Player2 gagne
        scoreManager.updateScores("Player1", "Player2", "nul");    // Match nul
    }
}
