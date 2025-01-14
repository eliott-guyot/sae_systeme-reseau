import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private Serveur server;
    private PrintWriter out;
    private BufferedReader in;
    private String pseudo;


    // Historique des parties
    private int totalParties=0;
    private int victoires=0;
    private int defaites=0;

    public ClientHandler(Socket clientSocket, Serveur server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

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

    private void handleMessage(String message) {
        if (message.startsWith("quit")) {
            disconnect();
        } else if (message.startsWith("play ")) {
            String targetPlayer = message.substring(5);
            server.sendInvitation(this, targetPlayer);
        } else if (message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("no")) {
            server.handleResponse(this, message);
        } else if (message.startsWith("column ")) {
            try {
                int column = Integer.parseInt(message.split(" ")[1]);
                server.handleMove(this, column); // Envoie le mouvement au serveur
            } catch (NumberFormatException e) {
                send("Numéro de colonne invalide.");
            }
        } else if (message.equalsIgnoreCase("help")) {
            send("Commandes disponibles : ");
            send("1. play [pseudo] - Inviter un joueur à jouer.");
            send("2. yes/no - Accepter ou refuser une invitation.");
            send("3. column [numéro] - Jouer dans la colonne spécifiée.");
            send("4. history - Afficher l'historique des parties.");
            send("5. quit - Quitter le serveur.");

        } else if (!message.isBlank()) {
            server.broadcastMessage(pseudo, message);
        }  else if (message.equalsIgnoreCase("history")) {
            showHistory();}
        else {
            send("Commande non reconnue.");
        }
    }

    private void disconnect() {
        try {
            server.unregisterClient(this); // Retirer le client du serveur
            clientSocket.close(); // Fermer la connexion socket
        } catch (IOException e) {
            System.out.println("Erreur lors de la déconnexion de " + pseudo);
        }
    }
    

    public void send(String message) {
        out.println(message);
    }

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
    // Méthode pour afficher l'historique des parties
    public void showHistory() {
        send("Historique des parties pour " + pseudo + ":");
        send("Total parties jouées : " + totalParties);
        send("Victoires : " + victoires);
        send("Défaites : " + defaites);
    }
}
