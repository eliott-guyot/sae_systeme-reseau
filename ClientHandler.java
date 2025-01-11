import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private Serveur server;
    private PrintWriter out;
    private BufferedReader in;
    private String pseudo;

    public ClientHandler(Socket clientSocket, Serveur server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("Entrez votre pseudo :");
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
            send("4. quit - Quitter le serveur.");
        } else if (!message.isBlank()) {
            server.broadcastMessage(pseudo, message);
        } else {
            send("Commande non reconnue.");
        }
    }

    private void disconnect() {
        try {
            server.unregisterClient(this);
            clientSocket.close();
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
}
