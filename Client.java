import java.io.*;
import java.net.*;

public class Client {
    private final String adresseServeur;
    private final int portServeur;

    public Client(String adresseServeur, int portServeur) {
        this.adresseServeur = adresseServeur;
        this.portServeur = portServeur;
    }

    public void demarrer() {
        try (Socket socket = new Socket(adresseServeur, portServeur);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connecté au serveur.");

            // Création d'un thread pour écouter les messages du serveur
            Thread ecouteur = new Thread(() -> {
                String messageServeur;
                try {
                    while ((messageServeur = in.readLine()) != null) {
                        System.out.println(messageServeur);
                    }
                } catch (IOException e) {
                    System.err.println("Déconnecté du serveur.");
                }
            });
            ecouteur.start();

            // Envoi des messages au serveur
            String messageUtilisateur;
            while ((messageUtilisateur = console.readLine()) != null) {
                out.println(messageUtilisateur);
                if (messageUtilisateur.equalsIgnoreCase("quit")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la connexion au serveur : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String adresseServeur = "localhost";
        int portServeur = 12345;
        Client client = new Client(adresseServeur, portServeur);
        client.demarrer();
    }
}
