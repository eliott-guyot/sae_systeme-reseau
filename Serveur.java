import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Serveur {
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clients = new HashMap<>();
    private Map<ClientHandler, ClientHandler> invitations = new HashMap<>();
    private Map<ClientHandler, Puissance4> games = new HashMap<>();

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
                if (game.checkWin(player)) {
                    player.send("Vous avez gagné la partie!");
                    game.getOpponent(player).send("Le joueur " + player.getPseudo() + " a gagné.");
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

    /**
     * Point d'entrée du programme, démarre le serveur sur le port spécifié.
     * 
     * @param args arguments en ligne de commande (non utilisés ici).
     */
    public static void main(String[] args) {
        Serveur serveur = new Serveur();
        serveur.demarrer(12345);
    }
}
