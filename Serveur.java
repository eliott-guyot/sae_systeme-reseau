import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Serveur {
    private final int port;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public Serveur(int port) {
        this.port = port;
    }

    public void demarrer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur démarré sur le port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket, this));
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du démarrage du serveur : " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    public synchronized boolean ajouterClient(String nom, ClientHandler handler) {
        if (clients.containsKey(nom)) {
            return false;
        }
        clients.put(nom, handler);
        diffuser("Client " + nom + " a rejoint la session.");
        return true;
    }

    public synchronized void supprimerClient(String nom) {
        clients.remove(nom);
        diffuser("Client " + nom + " a quitté la session.");
    }

    public synchronized void diffuser(String message) {
        for (ClientHandler client : clients.values()) {
            client.envoyerMessage(message);
        }
    }

    public synchronized ClientHandler obtenirClient(String nom) {
        return clients.get(nom);
    }

    public synchronized void envoyerInvitation(String nom, String adversaire) {
        ClientHandler client = clients.get(adversaire);
        if (client != null) {
            client.envoyerMessage(nom + " vous invite à jouer. Tapez 'accepter' pour accepter ou 'refuser' pour refuser.");
        }
    }

    public synchronized void informerEnvoyeur(String nomEnvoyeur, String nomInvité, boolean accepte) {
        ClientHandler client = clients.get(nomEnvoyeur); // envoyer à celui qui a envoyé l'invitation
        if (client != null) {
            if (accepte) {
                client.envoyerMessage(nomInvité + " a accepté l'invitation.");
            } else {
                client.envoyerMessage(nomInvité + " a refusé l'invitation.");
            }
        }
    }

    public static void main(String[] args) {
        int port = 12345;
        Serveur serveur = new Serveur(port);
        serveur.demarrer();
    }
}
