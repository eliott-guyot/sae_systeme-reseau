import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final Map<String, Player> players = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        int port = 5555;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur démarré sur le port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion détectée");
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur du serveur : " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private String playerName;
        private boolean waitingForResponse = false;
        private String waitingForPlayer = null;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
            ) {
                // Demander le nom du joueur
                output.write("Bienvenue ! Entrez votre nom :\n");
                output.flush();
                playerName = input.readLine().trim();

                // Vérification du nom
                if (playerName == null || playerName.isEmpty() || players.containsKey(playerName)) {
                    output.write("Nom invalide ou déjà utilisé. Déconnexion.\n");
                    output.flush();
                    return;
                }

                // Ajouter le joueur
                Player newPlayer = new Player(playerName, output, input);
                synchronized (players) {
                    players.put(playerName, newPlayer);
                }

                output.write("Bonjour, " + playerName + "! Voici les joueurs connectés :\n");
                sendPlayerList(output);

                // Boucle principale pour gérer les commandes
                while (true) {
                    if (waitingForResponse) {
                        output.write("Vous avez envoyé une invitation. Attendez la réponse de l'autre joueur...\n");
                        output.flush();
                        // Bloquer l'exécution jusqu'à ce que la réponse soit reçue
                        synchronized (this) {
                            wait();
                        }
                    }

                    output.write("Entrez une commande (nom_joueur pour inviter, 'quit' pour quitter) :\n");
                    output.flush();

                    String command = input.readLine();
                    if (command == null) {
                        break; // Client déconnecté
                    }

                    command = command.trim();
                    if (command.equalsIgnoreCase("quit")) {
                        output.write("Déconnexion. Au revoir !\n");
                        output.flush();
                        break;
                    }

                    handleCommand(command, output, input);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Erreur avec un client : " + e.getMessage());
            } finally {
                // Supprimer le joueur
                synchronized (players) {
                    players.remove(playerName);
                }
                System.out.println("Le joueur " + playerName + " s'est déconnecté.");
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
                }
            }
        }

        private void handleCommand(String command, BufferedWriter output, BufferedReader input) throws IOException {
            synchronized (players) {
                if (players.containsKey(command) && !command.equals(playerName)) {
                    Player invitedPlayer = players.get(command);
                    Player invitingPlayer = players.get(playerName);

                    // Vérifier si l'invitant attend une réponse
                    if (!waitingForResponse) {
                        // Envoyer l'invitation
                        waitingForResponse = true;
                        waitingForPlayer = command;

                        invitedPlayer.sendMessage("Vous avez reçu une invitation de " + playerName + ". Acceptez-vous ? (1 : Oui, 2 : Non)");

                        // Attendre la réponse de l'invité
                        String response = invitedPlayer.receiveResponse();
                        if ("1".equals(response)) {
                            invitingPlayer.sendMessage(command + " a accepté votre demande.");
                            invitedPlayer.sendMessage("Vous avez accepté l'invitation de " + playerName + ".");
                            invitingPlayer.sendMessage("Vous êtes maintenant en partie avec " + command);
                            invitedPlayer.sendMessage("Vous êtes maintenant en partie avec " + playerName);
                        } else if ("2".equals(response)) {
                            invitingPlayer.sendMessage(command + " a refusé votre invitation.");
                            invitedPlayer.sendMessage("Vous avez refusé l'invitation.");
                        } else {
                            invitingPlayer.sendMessage(command + " n'a pas répondu correctement.");
                        }

                        // Libérer les deux joueurs
                        synchronized (this) {
                            waitingForResponse = false;
                            notifyAll();
                        }
                    } else {
                        output.write("Vous êtes déjà en attente d'une réponse à une invitation.\n");
                        output.flush();
                    }
                } else {
                    output.write("Joueur introuvable ou invalide.\n");
                    output.flush();
                }
            }
        }

        private void sendPlayerList(BufferedWriter output) throws IOException {
            synchronized (players) {
                for (Player player : players.values()) {
                    output.write(player.toString() + "\n");
                }
                output.write("\n");
                output.flush();
            }
        }
    }

    // Classe représentant un joueur
    private static class Player {
        private final String name;
        private final BufferedWriter output;
        private final BufferedReader input;

        public Player(String name, BufferedWriter output, BufferedReader input) {
            this.name = name;
            this.output = output;
            this.input = input;
        }

        public void sendMessage(String message) throws IOException {
            output.write(message + "\n");
            output.flush();
        }

        public String receiveResponse() throws IOException {
            return input.readLine();
        }

        @Override
        public String toString() {
            return name + " - Disponible pour jouer.";
        }
    }
}
