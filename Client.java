import java.io.*;
import java.net.*;

public class Client {
    private static boolean invitationAccepted = false;  // Indicateur pour savoir si l'invitation a été acceptée

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage : java Client <adresse_ip> <port> <nom_joueur>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String playerName = args[2];

        try (Socket socket = new Socket(host, port);
             BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter serverOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            // Recevoir message de bienvenue et entrer le nom
            System.out.println(serverInput.readLine()); // "Bienvenue ! Entrez votre nom :"
            serverOutput.write(playerName);
            serverOutput.newLine();
            serverOutput.flush();

            // Réponse du serveur après l'enregistrement
            String response = serverInput.readLine();
            System.out.println(response);

            if (response.contains("Déconnexion")) {
                return;
            }

            // Liste des joueurs connectés
            System.out.println("Joueurs disponibles :");
            while (!(response = serverInput.readLine()).isEmpty()) {
                System.out.println(response);
            }

            // Thread pour écouter les messages du serveur
            Thread listenerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverInput.readLine()) != null) {
                        System.out.println(serverMessage);
                        // Vérifier si l'invitation est acceptée ou refusée
                        if (serverMessage.contains("a accepté votre invitation")) {
                            invitationAccepted = true;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connexion perdue avec le serveur.");
                }
            });

            listenerThread.start();

            // Boucle principale pour envoyer des commandes
            while (true) {
                System.out.print("Entrez une commande (nom_joueur pour inviter, 'quit' pour quitter) : ");
                String command = userInput.readLine();

                serverOutput.write(command);
                serverOutput.newLine();
                serverOutput.flush();

                if (command.equalsIgnoreCase("quit")) {
                    break;
                }

                // Si une invitation est envoyée
                if (command.matches("\\w+")) {
                    System.out.println("Invitation envoyée. Attente d'une réponse...");
                    // Attendre que l'invitation soit acceptée ou refusée
                    while (!invitationAccepted) {
                        // On peut afficher un message ou juste attendre que le message de réponse arrive
                        Thread.sleep(100); // Eviter un blocage total, on laisse le thread d'écoute répondre
                    }

                    if (invitationAccepted) {
                        System.out.println("L'invitation a été acceptée. Vous êtes maintenant en partie !");
                    } else {
                        System.out.println("L'invitation a été refusée.");
                    }

                    // Réinitialiser l'état pour la prochaine invitation
                    invitationAccepted = false;
                }
            }

            listenerThread.join();
        } catch (IOException | InterruptedException e) {
            System.err.println("Erreur de communication avec le serveur : " + e.getMessage());
        }
    }
}
