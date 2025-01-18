/**
 * Classe représentant une partie de Puissance 4 entre deux joueurs.
 * Gère l'état de la grille, les mouvements des joueurs, et les conditions de victoire.
 */
public class Puissance4 {
    private int[][] grille; // Grille de jeu
    private static final int ROWS = 6;  // Nombre de lignes de la grille
    private static final int COLS = 7;  // Nombre de colonnes de la grille
    private String[][] board;  // Grille de jeu
    private ClientHandler player1;  // Joueur 1
    private ClientHandler player2;  // Joueur 2
    private Serveur server;  // Serveur auquel la partie est liée
    private boolean player1Turn;  // Indique si c'est le tour du joueur 1

    /**
     * Constructeur de la classe Puissance4 qui initialise la partie avec les deux joueurs et le serveur.
     * Initialise également la grille de jeu avec des cases vides.
     * 
     * @param player1 Le premier joueur
     * @param player2 Le second joueur
     * @param server Le serveur qui gère la partie
     */
    public Puissance4(ClientHandler player1, ClientHandler player2, Serveur server) {
        this.grille = new int[6][7]; // 6 lignes x 7 colonnes
        this.player1 = player1;
        this.player2 = player2;
        this.server = server;
        this.board = new String[ROWS][COLS];
        this.player1Turn = true;

        // Initialisation du tableau de jeu avec des cases vides
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = ".";
            }
        }
    }

    /**
     * Effectue un mouvement dans une colonne donnée par le joueur.
     * Vérifie si le joueur joue à son tour, si la colonne est valide, et si elle n'est pas pleine.
     * 
     * @param player Le joueur qui effectue le mouvement
     * @param column La colonne dans laquelle le joueur veut jouer
     * @return true si le mouvement est valide, false sinon
     */
    public boolean makeMove(ClientHandler player, String column) {
        // Vérifier si c'est le tour du joueur
        if ((player == player1 && !player1Turn) || (player == player2 && player1Turn)) {
            player.send("Ce n'est pas votre tour !");
            return false;
        }

        Integer num_colonne;
        try {
            num_colonne = Integer.valueOf(column);  // Convertir la colonne en entier
        } catch (Exception e) {
            player.send("Ce n'est pas un numéro");
            return false;
        }

        // Vérifier si la colonne est valide
        if (num_colonne < 0 || num_colonne >= COLS) {
            player.send("Colonne invalide.");
            return false;
        }

        // Chercher la première ligne vide de la colonne
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][num_colonne].equals(".")) {
                board[row][num_colonne] = player == player1 ? "X" : "O";  // "X" pour player1, "O" pour player2
                player1Turn = !player1Turn;  // Changer de tour

                displayBoard();  // Afficher la grille après chaque mouvement

                // Indiquer à chaque joueur qui doit jouer
                if (player1Turn) {
                    player1.send("C'est à vous de jouer.");
                    player2.send("C'est à " + player1.getPseudo() + " de jouer.");
                } else {
                    player2.send("C'est à vous de jouer.");
                    player1.send("C'est à " + player2.getPseudo() + " de jouer.");
                }

                return true;
            }
        }

        player.send("Cette colonne est pleine, choisissez une autre colonne.");
        return false;
    }

    /**
     * indique si la grille est full
     * @return boolean true si full et false sinon
     */
    public boolean isGridFull() {
        for (int col = 0; col < grille[0].length; col++) {
            if (grille[0][col] == 0) { // Si la première ligne de la colonne est vide
                return false;
            }
        }
        return true;
    }

    /**
     * Affiche la grille de jeu actuelle aux deux joueurs.
     * La grille est envoyée sous forme de texte.
     */
    public void displayBoard() {
        StringBuilder boardString = new StringBuilder();
    
        // Affichage des indices de colonnes
        boardString.append("   1   2   3   4   5   6   7\n");
    
        // Affichage de la grille ligne par ligne
        for (int i = 0; i < ROWS; i++) {
            boardString.append("  +---+---+---+---+---+---+---+\n");
            for (int j = 0; j < COLS; j++) {
                // Si la case est vide, afficher un espace, sinon afficher X ou O
                String cell = (board[i][j] == null) ? " " : board[i][j];
                boardString.append("  " + cell + " ");
                if (j < COLS - 1) {
                    boardString.append("|");  // Séparateur entre les cases
                }
            }
            boardString.append("\n");
        }
    
        // Affichage de la bordure inférieure
        boardString.append("  +---+---+---+---+---+---+---+\n");
    
        // Envoie la grille aux deux joueurs
        player1.send(boardString.toString());
        player2.send(boardString.toString());
    }
    
    /**
     * Vérifie si un joueur a gagné en vérifiant les alignements horizontaux, verticaux et diagonaux.
     * 
     * @param player Le joueur à vérifier
     * @return true si le joueur a gagné, false sinon
     */
    public boolean checkWin(ClientHandler player) {
        String token = player == player1 ? "X" : "O";  // Choisir le symbole du joueur (X ou O)
        
        // Vérification horizontale, verticale et diagonale
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (board[row][col].equals(token)) {
                    // Horizontal
                    if (col + 3 < COLS &&
                        board[row][col + 1].equals(token) &&
                        board[row][col + 2].equals(token) &&
                        board[row][col + 3].equals(token)) {
                        return true;
                    }
                    // Vertical
                    if (row + 3 < ROWS &&
                        board[row + 1][col].equals(token) &&
                        board[row + 2][col].equals(token) &&
                        board[row + 3][col].equals(token)) {
                        return true;
                    }
                    // Diagonale
                    if (row + 3 < ROWS && col + 3 < COLS &&
                        board[row + 1][col + 1].equals(token) &&
                        board[row + 2][col + 2].equals(token) &&
                        board[row + 3][col + 3].equals(token)) {
                        return true;
                    }
                    // Diagonale inverse
                    if (row - 3 >= 0 && col + 3 < COLS &&
                        board[row - 1][col + 1].equals(token) &&
                        board[row - 2][col + 2].equals(token) &&
                        board[row - 3][col + 3].equals(token)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Récupère l'adversaire d'un joueur.
     * 
     * @param player Le joueur dont on veut connaître l'adversaire
     * @return L'adversaire du joueur
     */
    public ClientHandler getOpponent(ClientHandler player) {
        return player == player1 ? player2 : player1;
    }

    /**
     * Récupère le joueur qui doit jouer actuellement.
     * 
     * @return Le joueur dont c'est le tour
     */
    public ClientHandler getCurrentPlayer() {
        return player1Turn ? player1 : player2;
    }
    
    public void endGame(ClientHandler winner) {
        if (winner == player1) {
            player1.incrementVictoire();
            player2.incrementDefaite();
        } else {
            player1.incrementDefaite();
            player2.incrementVictoire();
        }
        
        player1.incrementPartie();
        player2.incrementPartie();
    }
}
