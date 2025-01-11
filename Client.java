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
                if (command.equals("quit")) {
                    out.println("quit");
                    break;
                } else if (command.startsWith("play ")) {
                    out.println(command); // Envoi de la commande "play" pour inviter un joueur
                } else if (command.equalsIgnoreCase("help")) {
                    out.println("help"); // Demander les commandes disponibles
                } else if (command.startsWith("column ")) {
                    try {
                        int column = Integer.parseInt(command.split(" ")[1]);
                        out.println("column " + column); // Jouer un coup dans la colonne spécifiée
                    } catch (NumberFormatException e) {
                        System.out.println("Veuillez entrer un numéro de colonne valide.");
                    }
                } else {
                    // Si le message n'est pas une commande spéciale, l'envoyer comme un message de chat
                    out.println(command);
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur de connexion au serveur.");
        }
    }
}
