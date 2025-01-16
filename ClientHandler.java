import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Classe qui gère la communication avec un client connecté au serveur.
 * Chaque client est traité dans un thread distinct pour permettre la gestion concurrente des clients.
 */
public class ClientHandler extends Thread {
    private Socket clientSocket;  // Socket de communication avec le client
    private Serveur server;  // Référence au serveur
    private PrintWriter out;  // Flux de sortie pour envoyer des messages au client
    private BufferedReader in;  // Flux d'entrée pour lire les messages du client
    private String pseudo;  // Pseudo du client
    
    // Historique des parties
    private int totalParties=0;
    private int victoires=0;
    private int defaites=0;


    /**
     * Constructeur pour initialiser le gestionnaire de client avec la socket et le serveur.
     * 
     * @param clientSocket La socket du client
     * @param server La référence au serveur
     */
    public ClientHandler(Socket clientSocket, Serveur server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    /**
     * Méthode qui est exécutée lorsque le thread démarre. 
     * Elle établit la communication avec le client, reçoit les messages, 
     * et appelle la méthode appropriée pour chaque type de message.
     */
    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            pseudo = in.readLine();
            server.registerClient(this, pseudo);
            Map<String, int[]> currentScores = server.getScores();  

            // Initialiser les scores du joueur si inexistant
            if (!currentScores.containsKey(pseudo)) {
                currentScores.put(pseudo, new int[]{0, 0, 0}); // [defaites, nuls, victoires]
                System.out.println("Nouveau joueur ajouté avec des scores par défaut.");
            }
            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Erreur avec le client " + pseudo);
        } finally {
            disconnect();
        }
    }

    /**
     * Gère les différents types de messages envoyés par le client.
     * 
     * @param message Le message reçu du client
     */
    private void handleMessage(String message) {
        if (message.startsWith("quit")) {
            disconnect();

        } else if (message.startsWith("play ")) {
            String targetPlayer = message.substring(5);
            server.sendInvitation(this, targetPlayer);

        } else if (message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("no")) {
            server.handleResponse(this, message);

        } else if (message.matches("\\d+")) {  // Si le message est un numéro, c'est un mouvement
            server.handleMove(this, message);  // Envoi du mouvement au serveur

        } else if (message.startsWith("ff")) {
            server.handleMove(this, "ff");

        } else if (message.equalsIgnoreCase("help")) {
            send("\n===================================");
            send("          *** MENU DES COMMANDES ***");
            send("===================================");
        
            send("\n[1] play [pseudo]     - Inviter un joueur à jouer.");
            send("[2] yes/no             - Accepter ou refuser une invitation.");
            send("[3] [numéro]           - Jouer dans la colonne spécifiée.");
            send("[4] history            - Afficher l'historique des parties.");
            send("[5] quit               - Quitter le serveur.");
        
            send("\n===================================");
            send("  ** Utilisez les commandes ci-dessus pour interagir avec le serveur. **");
            send("===================================\n");

        } else if (!message.isBlank()) {
            server.broadcastMessage(pseudo, message);
            
        } else {
            send("Commande non reconnue.");
        }
    }

    /**
     * Déconnecte le client du serveur et ferme la connexion.
     */
    private void disconnect() {
        try {
            server.unregisterClient(this); // Retirer le client du serveur
            clientSocket.close(); // Fermer la connexion socket
        } catch (IOException e) {
            System.out.println("Erreur lors de la déconnexion de " + pseudo);
        }
    }

    /**
     * Envoie un message au client via le flux de sortie.
     * 
     * @param message Le message à envoyer
     */
    public void send(String message) {
        out.println(message);
    }

    /**
     * Récupère le pseudo du client.
     * 
     * @return Le pseudo du client
     */
    public String getPseudo() {
        return pseudo;
    }
     public void incrementPartie() {
        totalParties++;
    }

    public void incrementVictoire() {
        victoires++;
    }

    public void incrementDefaite() {
        defaites++;
    }
    // Afficher l'historique des scores pour le joueur actuel
    public void showHistory() {
        Map<String, int[]> scores = server.getScores();
        int[] playerScores = scores.getOrDefault(pseudo, new int[]{0, 0, 0});
        send("Historique des parties pour " + pseudo + ":");
        send("Défaites : " + playerScores[0]);
        send("Matchs nuls : " + playerScores[1]);
        send("Victoires : " + playerScores[2]);
    }
}
