public class Puissance4 {
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private String[][] board;
    private ClientHandler player1;
    private ClientHandler player2;
    private Serveur server;
    private boolean player1Turn;

    public Puissance4(ClientHandler player1, ClientHandler player2, Serveur server) {
        this.player1 = player1;
        this.player2 = player2;
        this.server = server;
        this.board = new String[ROWS][COLS];
        this.player1Turn = true;

        // Initialisation du tableau de jeu
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = ".";
            }
        }
    }

    public boolean makeMove(ClientHandler player, int column) {
        if ((player == player1 && !player1Turn) || (player == player2 && player1Turn)) {
            player.send("Ce n'est pas votre tour !");
            return false;
        }
    
        if (column < 0 || column >= COLS) {
            player.send("Colonne invalide.");
            return false;
        }
    
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][column].equals(".")) {
                board[row][column] = player == player1 ? "X" : "O"; // X pour player1, O pour player2
                player1Turn = !player1Turn; // Changer de joueur après un coup valide
                return true;
            }
        }
    
        // Si la colonne est pleine
        player.send("Cette colonne est pleine, choisissez une autre colonne.");
        return false;
    }
    

    public void displayBoard() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    public boolean checkWin(ClientHandler player) {
        String token = player == player1 ? "X" : "O";
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

    public ClientHandler getOpponent(ClientHandler player) {
        return player == player1 ? player2 : player1;
    }
}
