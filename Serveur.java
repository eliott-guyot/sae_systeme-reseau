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

    public synchronized void handleMove(ClientHandler var1, String var2) {
        // Si la commande est "ff", abandonner et donner la victoire à l'adversaire
        if (var2.equalsIgnoreCase("ff")) {
            Puissance4 var3 = (Puissance4)this.games.get(var1);
            if (var3 != null) {
                ClientHandler adversaire = var3.getOpponent(var1);
                if (adversaire != null) {
                    var1.send("Vous avez abandonné la partie. " + adversaire.getPseudo() + " gagne !");
                    adversaire.send("L'adversaire " + var1.getPseudo() + " a abandonné. Vous avez gagné !");
                    this.games.remove(var1);
                    this.games.remove(adversaire);
                }
            }
            return; // Sortir de la méthode pour ne pas traiter un mouvement classique
        }

        // Si ce n'est pas "ff", gérer un mouvement classique
        Puissance4 var3 = (Puissance4)this.games.get(var1);
        if (var3 != null) {
            boolean var4 = var3.makeMove(var1, var2);
            if (var4) {
                var3.displayBoard();
                if (var3.checkWin(var1)) {
                    var1.send("Vous avez gagné la partie!");
                    var3.getOpponent(var1).send("Le joueur " + var1.getPseudo() + " a gagné.");
                    this.games.remove(var1);
                    this.games.remove(var3.getOpponent(var1));
                }
            } else {
                var1.send("Mouvement invalide, réessayez.");
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
