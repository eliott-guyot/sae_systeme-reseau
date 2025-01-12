import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Serveur {
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clients = new HashMap<>();
    private Map<ClientHandler, ClientHandler> invitations = new HashMap<>();
    private Map<ClientHandler, Puissance4> games = new HashMap<>(); // Gérer les parties en cours

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

    public synchronized void registerClient(ClientHandler clientHandler, String pseudo) {
        clients.put(pseudo, clientHandler);
        broadcast("[" + pseudo + "] a rejoint le serveur.");
    }

    public synchronized void unregisterClient(ClientHandler clientHandler) {
        clients.remove(clientHandler.getPseudo());
        games.remove(clientHandler); // Supprimer le jeu si le client se déconnecte
        broadcast("[" + clientHandler.getPseudo() + "] a quitté le serveur.");
    }

    public synchronized void sendInvitation(ClientHandler sender, String targetPlayer) {
        ClientHandler target = clients.get(targetPlayer);
        if (target != null) {
            invitations.put(target, sender);
            target.send(sender.getPseudo()+" vous invite à jouer (yes pour accepter - no pour refuser)");
            sender.send("Invitation envoyée à " + targetPlayer);
        } else {
            sender.send("Le joueur " + targetPlayer + " n'est pas disponible.");
        }
    }

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

    private void startGame(ClientHandler player1, ClientHandler player2) {
        player1.send("La partie commence contre " + player2.getPseudo());
        player2.send("La partie commence contre " + player1.getPseudo());

        Puissance4 game = new Puissance4(player1, player2, this);
        games.put(player1, game);
        games.put(player2, game);

        game.displayBoard(); // Affiche le plateau de départ
    }

    public synchronized void handleMove(ClientHandler player, int column) {
        Puissance4 game = games.get(player);
        if (game != null) {
            boolean validMove = game.makeMove(player, column);
            if (validMove) {
                game.displayBoard(); // Affiche après chaque mouvement
                if (game.checkWin(player)) {
                    player.send("Vous avez gagné la partie!");
                    game.getOpponent(player).send("Le joueur " + player.getPseudo() + " a gagné.");
                    games.remove(player);
                    games.remove(game.getOpponent(player));
                }
            } else {
                player.send("Mouvement invalide, réessayez.");
            }
        }
    }

    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
            client.send(message);
        }
    }

    public synchronized void broadcastMessage(String sender, String message) {
        for (ClientHandler client : clients.values()) {
            client.send("[Chat] " + sender + ": " + message);
        }
    }
    
    public static void main(String[] args) {
        Serveur serveur = new Serveur();
        serveur.demarrer(12345);
    }
}
