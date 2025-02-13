import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Classe représentant un client qui se connecte à un serveur de jeu.
 * Ce client permet à l'utilisateur d'interagir avec le serveur via des
 * commandes.
 */
public class Client {
    private Socket socket; // Connexion avec le serveur
    private PrintWriter out; // Flux de sortie pour envoyer des données au serveur
    private BufferedReader in; // Flux d'entrée pour recevoir des données du serveur
    private String pseudo; // Pseudo de l'utilisateur
    private Scanner scanner; // Scanner pour lire les entrées de l'utilisateur
    private int victoire;
    private int defaite;
    private int nul;

    /**
     * Méthode principale qui démarre l'application cliente.
     * Crée une instance de Client et appelle la méthode start pour commencer la
     * communication.
     */

     public static void main(String[] args) {
        String serverAddress = "localhost"; // Adresse par défaut
        int serverPort = 12345; // Port par défaut

        // Vérification des arguments de la ligne de commande
        if (args.length >= 1) {
            serverAddress = args[0]; // Récupère l'adresse IP
        }
        if (args.length >= 2) {
            try {
                serverPort = Integer.parseInt(args[1]); // Récupère le port
            } catch (NumberFormatException e) {
                System.err.println("Port invalide. Utilisation du port par défaut : " + serverPort);
            }
        }

        Client client = new Client();
        client.start(serverAddress, serverPort); // Passe l'adresse et le port à la méthode start
    }

    /**
     * Démarre le client, se connecte au serveur, et gère les interactions avec
     * l'utilisateur.
     * Cette méthode établit la connexion au serveur, envoie et reçoit des messages,
     * et lance un thread pour écouter les messages du serveur en temps réel.
     */
    public void start(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort); // Utilise les paramètres
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scanner = new Scanner(System.in);

            System.out.println("Connecté au serveur " + serverAddress + " sur le port " + serverPort);
            
            System.out.println("Entrez votre pseudo :");
            pseudo = scanner.nextLine();
            out.println(pseudo); // Envoi du pseudo au serveur
            System.out.println(
                "\n===================================" +
                "\n          *** MENU DES COMMANDES ***" +
                "\n===================================" +
                "\n\n" +
                "  [1] play [pseudo]      - Inviter un joueur à jouer." +
                "\n  [2] yes/no             - Accepter ou refuser une invitation." +
                "\n  [3] [numéro]           - Jouer dans la colonne spécifiée." +
                "\n  [4] stat               - stats du joueur." +
                "\n  [5] quit               - Quitter le serveur." +
                "\n\n" +
                "===================================" +
                "\n  ** Utilisez les commandes ci-dessus pour interagir avec le serveur. **" +
                "\n===================================" 
        );
        
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
                    // Envoi d'une défaite pour l'autre joueur si on quitte volontairement
                    out.println("defeat " + pseudo); // Mettre à jour les scores
                    break;
                } else if (command.startsWith("play ")) {
                    out.println(command);
                } else if (command.equalsIgnoreCase("stat")) {
                    out.println("stat");  // Envoie la commande 'stat' pour demander les stats
                }
                
                
                else if (command.equalsIgnoreCase("help")) {
                    out.println("help"); // Demander les commandes disponibles

                } else if (command.startsWith("column ")) {
                    try {
                        int column = Integer.parseInt(command.split(" ")[1]);
                        out.println("column " + column); // Jouer un coup dans la colonne spécifiée
                    } catch (NumberFormatException e) {
                        System.out.println("Veuillez entrer un numéro de colonne valide.");
                    }
                } else if (command.equalsIgnoreCase("victory")) {
                    out.println("victory " + pseudo); // Envoi de la victoire au serveur
                    victoire++;
                    System.out.println("Félicitations, vous avez gagné !");
                } else if (command.equalsIgnoreCase("defeat")) {
                    out.println("defeat " + pseudo); // Envoi de la défaite au serveur
                    defaite++;
                    System.out.println("Dommage, vous avez perdu.");
                } else if (command.equalsIgnoreCase("draw")) {
                    out.println("draw " + pseudo); // Envoi du match nul au serveur
                    nul++;
                    System.out.println("Match nul !");
                } else {
                    out.println(command);
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur de connexion au serveur.");
        }
    }

    /**
     * Vérifie si une chaîne de caractères représente un nombre entier.
     *
     * @param str la chaîne à vérifier
     * @return true si la chaîne est un nombre entier, false sinon
     */
    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
