import java.io.*;
import java.net.Socket;

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
            send("Commandes disponibles : ");
            send("1. play [pseudo] - Inviter un joueur à jouer.");
            send("2. yes/no - Accepter ou refuser une invitation.");
            send("3. [numéro] - Jouer dans la colonne spécifiée.");
            send("4. quit - Quitter le serveur.");

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
            server.unregisterClient(this);
            clientSocket.close();
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
}
