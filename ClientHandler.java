import java.io.*;
import java.net.*;

class ClientHandler implements Runnable {
    private final Socket socket;
    private final Serveur serveur;
    private String nom;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, Serveur serveur) {
        this.socket = socket;
        this.serveur = serveur;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Demande du nom du client
            out.println("Entrez votre nom :");
            nom = in.readLine();

            if (nom == null || nom.trim().isEmpty() || !serveur.ajouterClient(nom, this)) {
                out.println("Nom invalide ou déjà utilisé. Fermeture de la connexion.");
                return;
            }

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("quit")) {
                    out.println("Au revoir !");
                    break;
                } else if (message.startsWith("play")) {
                    String[] parts = message.split(" ", 2);
                    if (parts.length > 1) {
                        String nomAdversaire = parts[1];
                        ClientHandler adversaire = serveur.obtenirClient(nomAdversaire);
                        if (adversaire != null) {
                            // Envoie une invitation au joueur cible
                            serveur.envoyerInvitation(nom, nomAdversaire);
                        } else {
                            out.println("Joueur " + nomAdversaire + " introuvable.");
                        }
                    } else {
                        out.println("Commande 'play' invalide.");
                    }
                } else if (message.equalsIgnoreCase("accepter")) {
                    out.println("Vous avez accepté l'invitation !");
                    // Notifie l'envoyeur que l'invitation a été acceptée
                    serveur.informerEnvoyeur(nom, nom, true);
                } else if (message.equalsIgnoreCase("refuser")) {
                    out.println("Vous avez refusé l'invitation.");
                    // Notifie l'envoyeur que l'invitation a été refusée
                    serveur.informerEnvoyeur(nom, nom, false);
                } else {
                    serveur.diffuser(nom + " : " + message);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur avec le client " + nom + " : " + e.getMessage());
        } finally {
            if (nom != null) {
                serveur.supprimerClient(nom);
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du socket pour " + nom + " : " + e.getMessage());
            }
        }
    }

    public void envoyerMessage(String message) {
        out.println(message);
    }
}
