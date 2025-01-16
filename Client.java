import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String pseudo;
    private Scanner scanner;

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    public void start() {
        try {
            socket = new Socket("localhost", 12345); // Connexion au serveur (adresse et port à ajuster)
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scanner = new Scanner(System.in);

            // Demander le pseudo
            System.out.println("Entrez votre pseudo :");
            pseudo = scanner.nextLine();
            out.println(pseudo); // Envoi du pseudo au serveur
            System.out.println("Commandes disponibles : \n 1. play [pseudo] - Inviter un joueur à jouer. \n 2. yes/no - Accepter ou refuser une invitation. \n 3. [numéro] - Jouer dans la colonne spécifiée. \n 4. quit - Quitter le serveur.");

            // Démarrer un thread pour écouter les messages du serveur
            Thread listener = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage); // Affiche les messages reçus du serveur
                    }
                } catch (IOException e) {
                    System.out.println("Connexion au serveur perdue.");
                }
            });
            listener.start();

            // Lire les commandes de l'utilisateur et les envoyer au serveur
            while (true) {
                String command = scanner.nextLine();

                // Si la commande est "quit", quitter la partie
                if (command.equals("quit")) {
                    out.println("quit");
                    break;
                } else if (command.startsWith("play ")) {
                    out.println(command); // Envoi de la commande "play" pour inviter un joueur
                } else if (command.equalsIgnoreCase("help")) {
                    out.println("help"); // Demander les commandes disponibles
                } else if (command.equalsIgnoreCase("yes") || command.equalsIgnoreCase("no")) {
                    out.println(command); // Réponse à une invitation
                } else if (isNumeric(command)) {
                    // Si le message est un numéro valide, c'est un numéro de colonne
                    out.println(command); // Envoi du numéro de colonne directement
                } else {
                    // Si ce n'est pas un nombre, envoyer comme message normal
                    out.println(command);
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur de connexion au serveur.");
        }
    }

    // Méthode pour vérifier si une chaîne de caractères est un nombre entier valide
    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str); // Essayer de convertir la chaîne en entier
            return true; // Si la conversion réussit, c'est un nombre
        } catch (NumberFormatException e) {
            return false; // Si une exception est levée, ce n'est pas un nombre
        }
    }
}
