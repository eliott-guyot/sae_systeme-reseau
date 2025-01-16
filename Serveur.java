import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Serveur {
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clients = new HashMap<>();
    private Map<ClientHandler, ClientHandler> invitations = new HashMap<>();
    private Map<ClientHandler, Puissance4> games = new HashMap<>();
    private static Map<String, int[]> scores = new HashMap<>();
    private static final String FILE_NAME = "scores.json";

    /**
     * Démarre le serveur sur un port spécifié.
     * Accepte les connexions entrantes et crée un handler pour chaque client.
     * 
     * @param port le port sur lequel le serveur écoute les connexions.
     */
    public void demarrer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Serveur lancé sur le port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau client connecté.");
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    /**
     * Enregistre un client dans le serveur.
     * 
     * @param clientHandler le handler du client à enregistrer.
     * @param pseudo le pseudo du client.
     */
    public synchronized void registerClient(ClientHandler clientHandler, String pseudo) {
        clients.put(pseudo, clientHandler);
        sendAvailablePlayers(clientHandler);
        broadcast("[" + pseudo + "] a rejoint le serveur.");
    }

    /**
     * Désenregistre un client du serveur et le retire des jeux en cours.
     * 
     * @param clientHandler le handler du client à désenregistrer.
     */
    public synchronized void unregisterClient(ClientHandler clientHandler) {
        clients.remove(clientHandler.getPseudo());
        games.remove(clientHandler); // Supprimer le jeu si le client se déconnecte
        broadcast("[" + clientHandler.getPseudo() + "] a quitté le serveur.");
    }

    /**
     * Envoie une invitation de jeu à un autre joueur.
     * 
     * @param sender le joueur qui envoie l'invitation.
     * @param targetPlayer le pseudo du joueur cible pour l'invitation.
     */
    public synchronized void sendInvitation(ClientHandler sender, String targetPlayer) {
        ClientHandler target = clients.get(targetPlayer);
        if (target != null) {
            invitations.put(target, sender);
            target.send(sender.getPseudo() + " vous invite à jouer (yes pour accepter - no pour refuser)");
            sender.send("Invitation envoyée à " + targetPlayer);
        } else {
            sender.send("Le joueur " + targetPlayer + " n'est pas disponible.");
        }
    }

    public static synchronized  Map<String, int[]> getScores() {
        return scores;
    }
    public static synchronized void setScores(Map<String, int[]> newScores) {
        scores = newScores;
    }


    /**
     * Gère la réponse à une invitation de jeu (acceptation ou refus).
     * 
     * @param responder le joueur qui répond à l'invitation.
     * @param response la réponse du joueur ("yes" ou "no").
     */
    public synchronized void handleResponse(ClientHandler responder, String response) {
        ClientHandler inviter = invitations.get(responder);
        if (inviter != null) {
            if (response.equalsIgnoreCase("yes")) {
                inviter.send("Le joueur " + responder.getPseudo() + " a accepté votre invitation.");
                responder.send("Vous avez accepté l'invitation de " + inviter.getPseudo() + ".");
                startGame(inviter, responder);
            } else if (response.equalsIgnoreCase("no")) {
                inviter.send("Le joueur " + responder.getPseudo() + " a refusé votre invitation.");
                responder.send("Vous avez refusé l'invitation de " + inviter.getPseudo() + ".");
            }
            invitations.remove(responder);
        } else {
            responder.send("Aucune invitation en attente.");
        }
    }

    /**
     * Démarre une nouvelle partie de Puissance 4 entre deux joueurs.
     * 
     * @param player1 le premier joueur.
     * @param player2 le deuxième joueur.
     */
    private void startGame(ClientHandler player1, ClientHandler player2) {
        player1.send("La partie commence contre " + player2.getPseudo());
        player2.send("La partie commence contre " + player1.getPseudo());
        player1.send("C'est a vous de jouer !");
        player2.send("C'est à " + player1.getPseudo() + " de jouer !");

        Puissance4 game = new Puissance4(player1, player2, this);
        games.put(player1, game);
        games.put(player2, game);

        game.displayBoard();
    }

    /**
     * Gère un mouvement effectué par un joueur pendant une partie.
     * Si le joueur choisit de quitter, l'adversaire est déclaré gagnant.
     * 
     * @param player le joueur effectuant le mouvement.
     * @param column la colonne où le joueur veut jouer.
     */
    public synchronized void handleMove(ClientHandler player, String column) {
        if (column.equalsIgnoreCase("ff")) {
            Puissance4 game = this.games.get(player);
            if (game != null) {
                ClientHandler opponent = game.getOpponent(player);
                if (opponent != null) {
                    player.send("Vous avez abandonné la partie. " + opponent.getPseudo() + " gagne !");
                    opponent.send("L'adversaire " + player.getPseudo() + " a abandonné. Vous avez gagné !");
                    this.updateScores(player.getPseudo(), opponent.getPseudo(), "defeat");
                    this.games.remove(player);
                    this.games.remove(opponent);
                }
            }
            return;
        }
    
        Puissance4 game = this.games.get(player);
        if (game != null) {
            boolean isValidMove = game.makeMove(player, column);
            if (isValidMove) {
                // Vérifie si le joueur a gagné
                if (game.checkWin(player)) {
                    player.send("Vous avez gagné la partie!");
                    game.getOpponent(player).send("Le joueur " + player.getPseudo() + " a gagné.");
                    this.updateScores(player.getPseudo(), game.getOpponent(player).getPseudo(), "victory");
                    this.games.remove(player);
                    this.games.remove(game.getOpponent(player));
                }
                // Vérifie si la grille est pleine
                else if (game.isGridFull()) {
                    player.send("La grille est pleine ! Match nul.");
                    game.getOpponent(player).send("La grille est pleine ! Match nul.");
                    this.updateScores(player.getPseudo(), game.getOpponent(player).getPseudo(), "draw");
                    this.games.remove(player);
                    this.games.remove(game.getOpponent(player));
                }
            } else {
                player.send("Mouvement invalide, réessayez.");
            }
        }
    }
    


    /**
     * Envoie un message à tous les clients connectés.
     * 
     * @param message le message à diffuser.
     */
    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
            client.send(message);
        }
    }

    /**
     * Envoie un message de chat à tous les clients connectés.
     * 
     * @param sender le nom du joueur qui envoie le message.
     * @param message le message de chat à diffuser.
     */
    public synchronized void broadcastMessage(String sender, String message) {
        for (ClientHandler client : clients.values()) {
            client.send("[Chat] " + sender + ": " + message);
        }
    }
    
    // Nouvelle méthode pour envoyer la liste des joueurs connectés mais pas encore en jeu
private void sendAvailablePlayers(ClientHandler clientHandler) {
    StringBuilder playerList = new StringBuilder("Joueurs connectés : ");

    // On filtre les joueurs : ceux qui sont dans clients mais pas dans games
    List<String> availablePlayers = new ArrayList<>();
    for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
        ClientHandler client = entry.getValue();
        if (!games.containsKey(client)) { // Si le client n'est pas dans un jeu
            availablePlayers.add(entry.getKey());
        }
    }

    // Ajouter les joueurs disponibles à la liste
    for (String player : availablePlayers) {
        playerList.append(player).append(" ");
    }

    clientHandler.send(playerList.toString()); // Envoi au client
}
    private static void saveScores() {
    StringBuilder fileContent = new StringBuilder();

    // Lire le fichier existant
    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
        String line;
        while ((line = reader.readLine()) != null) {
            fileContent.append(line);
        }
    } catch (FileNotFoundException e) {
        System.out.println("Fichier non trouvé, un nouveau sera créé.");
    } catch (IOException e) {
        System.out.println("Erreur lors de la lecture du fichier JSON.");
    }

    // Charger les données existantes (si disponibles)
    Map<String, int[]> existingScores = new HashMap<>();
    if (fileContent.length() > 0) {
        existingScores = parseJson(fileContent.toString());
    }

    // Mettre à jour les scores existants
    for (Map.Entry<String, int[]> entry : scores.entrySet()) {
        existingScores.put(entry.getKey(), entry.getValue());
    }

    // Générer le JSON final
    String updatedJson = generateJson(existingScores);

    // Écrire le contenu mis à jour dans le fichier
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
        writer.write(updatedJson);
    } catch (IOException e) {
        System.out.println("Erreur lors de la sauvegarde des scores.");
    }
}

private static Map<String, int[]> parseJson(String json) {
    Map<String, int[]> parsedScores = new HashMap<>();
    json = json.trim();
    if (json.startsWith("{") && json.endsWith("}")) {
        json = json.substring(1, json.length() - 1).trim(); // Supprimer les accolades

        String[] entries = json.split("},");
        for (String entry : entries) {
            entry = entry.trim();
            if (!entry.endsWith("}")) {
                entry += "}";
            }

            String[] keyValue = entry.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim();

                // Extraire les valeurs du tableau [defaites, nul, victoires]
                value = value.replace("{", "").replace("}", "").trim();
                String[] stats = value.split(",");
                int[] intStats = new int[3];
                for (int i = 0; i < stats.length; i++) {
                    String[] statPair = stats[i].split(":");
                    if (statPair.length == 2) {
                        intStats[i] = Integer.parseInt(statPair[1].trim());
                    }
                }

                parsedScores.put(key, intStats);
            }
        }
    }
    return parsedScores;
}

private static String generateJson(Map<String, int[]> scores) {
    StringBuilder jsonBuilder = new StringBuilder("{\n");
    for (Map.Entry<String, int[]> entry : scores.entrySet()) {
        String pseudo = entry.getKey();
        int[] stats = entry.getValue();

        jsonBuilder.append("  \"").append(pseudo).append("\": {");
        jsonBuilder.append("\"defaites\": ").append(stats[0]).append(", ");
        jsonBuilder.append("\"nul\": ").append(stats[1]).append(", ");
        jsonBuilder.append("\"victoires\": ").append(stats[2]).append("},\n");
    }

    // Supprimer la dernière virgule et ajouter une accolade fermante
    if (jsonBuilder.length() > 2) {
        jsonBuilder.setLength(jsonBuilder.length() - 2); // Supprimer la dernière virgule
    }
    jsonBuilder.append("\n}");

    return jsonBuilder.toString();
}
public static void main(String[] args) {
    Serveur serveur = new Serveur();

    // Vérifier si le fichier de scores existe, sinon le créer
    File scoreFile = new File(FILE_NAME);
    if (!scoreFile.exists()) {
        try {
            if (scoreFile.createNewFile()) {
                System.out.println("Fichier de scores créé : " + FILE_NAME);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
                    writer.write("{}"); // Contenu JSON vide
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la création du fichier de scores : " + e.getMessage());
        }
    }

    // Charger les scores à partir du fichier
    StringBuilder fileContent = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
        String line;
        while ((line = reader.readLine()) != null) {
            fileContent.append(line);
        }

        // Charger les scores depuis le JSON
        if (fileContent.length() > 0) {
            Map<String, int[]> loadedScores = parseJson(fileContent.toString());
            setScores(loadedScores); // Mettre à jour les scores en mémoire
            System.out.println("Scores chargés avec succès.");
        } else {
            System.out.println("Aucun score existant à charger.");
        }
    } catch (IOException e) {
        System.out.println("Erreur lors de la lecture du fichier de scores : " + e.getMessage());
    }

    // Ajouter un hook de fermeture pour sauvegarder les scores avant la sortie
    Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
            System.out.println("Exécution du hook de fermeture...");
            Serveur.saveScores();  // Sauvegarde avant la fermeture
        }
    });

    
    // Lancer le serveur
    serveur.demarrer(12345);
}

        // Mettre à jour les scores
        private void updateScores(String player, String opponent, String result) {
            // Récupérer ou initialiser les scores
            int[] playerScores = scores.getOrDefault(player, new int[]{0, 0, 0});
            int[] opponentScores = scores.getOrDefault(opponent, new int[]{0, 0, 0});
        
            // Mettre à jour les scores selon le résultat
            switch (result) {
                case "victory":
                    playerScores[2]++; // Victoires pour le joueur
                    opponentScores[0]++; // Défaites pour l'adversaire
                    break;
                case "defeat":
                    playerScores[0]++; // Défaites pour le joueur
                    opponentScores[2]++; // Victoires pour l'adversaire
                    break;
                case "draw":
                    playerScores[1]++; // Matchs nuls pour le joueur
                    opponentScores[1]++; // Matchs nuls pour l'adversaire
                    break;
            }
        
            // Mettre à jour les scores dans la mémoire
            scores.put(player, playerScores);
            scores.put(opponent, opponentScores);
        
            // Sauvegarder les scores dans le fichier JSON
            saveScores();
        }
        

    }

