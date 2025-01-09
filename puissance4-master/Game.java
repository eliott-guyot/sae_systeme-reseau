

import java.util.Scanner;

public class Game {
	
	private Board board;
	private final Player[] players;
	private final Scanner sc;
	private Player playerA;
	private Player playerB;
	private boolean gameIsFinished = false;
	
	
	/**
	 * Sélectionner aléatoirement une valeur entière entre 0 et 1
	 * @return  0 ou 1
	 */
	private int randomize() {

		return (int) (Math.random() * 2);
	}
	
	/**
	 * Définir les conditions de fin de jeu
	 * @return True si le jeu est fini sinon False
	 */
	private boolean isFinished() {

		if(playerA.hasWin() || playerB.hasWin())
			return true;

		return playerA.getNbreJetons() == 0 && playerB.getNbreJetons() == 0;
	}
	
	/**
	 * Initialiser les couleurs, les symboles et les affecter aux joueurs aléatoirement
	 * @param nameA Nom du joueur A
	 * @param nameB Nom du joueur B
	 */
	private void initialize(String nameA, String nameB) {

		Symbol symbolA = randomize() == 0 ? Symbol.JAUNE : Symbol.ROUGE;
		Symbol symbolB = symbolA == Symbol.JAUNE ? Symbol.ROUGE :Symbol.JAUNE;

		int positionA = randomize();
		int positionB = positionA == 0 ? 1 : 0;

		players[positionA] = new Player(nameA, symbolA, this.board);
		players[positionB] = nameB.equals("Ordinateur") ? new Computer(nameB, symbolB, this.board) : new Player(nameB, symbolB, this.board);

		playerA = players[0];
		playerB = players[1];
	}
	
	/**
	 * Démarrer la partie et saisir les choix des utilisateurs
	 * @throws InterruptedException Du à l'utilisation de Thread.sleep
	 */
	private void start() throws InterruptedException {

		System.out.println("Bonjour ! \n1)Joueur contre Joueur\n2)Joueur contre Ordinateur");
		int choice = sc.nextInt();

		System.out.println("Entrez le nom du premier joueur:");
		sc.nextLine();

		String nameA =  sc.nextLine();
		if(choice == 1)
			System.out.println("Entrez le nom du second joueur:");

		String nameB = choice == 1 ? sc.nextLine() : "Ordinateur";

		initialize(nameA, nameB);
		jouerManche();
	}
	
	/**
	 * Jouer le tour d'un joueur
	 * @param j Le joueur qui doit jouer
	 * @throws InterruptedException Du à l'utilisation de Thread.sleep
	 */
	private void jouerTourSolo(Player j) throws InterruptedException{

		gameIsFinished = isFinished();

		if(!gameIsFinished) {

			System.out.println("Au tour de "+ j.getName());
			Thread.sleep(1500);

			if(!(j instanceof Computer)) {

				int column;

					do {

						System.out.println(j.getName() + " quelle colonne choisi tu (0 à 6)?");

						while(!sc.hasNextInt()) {
							System.out.println("Ce n'est pas un chiffre, merci de recommencer:");
							sc.next();
						}

						column = sc.nextInt();

					} while (column > 6 || column < 0 );

				while (board.isColumnFull(column)) {

					System.out.println("Cette colonne est pleine, merci d'en choisir une autre:");
					column = sc.nextInt();
				}

				j.playAtColumn(column);
			}

			else 
				((Computer)j).play();

			System.out.println(board);
		}
	}
	/**
	 * Déroulement de la partie
	 * @throws InterruptedException Du à l'utilisation de Thread.sleep
	 */
	private void jouerManche() throws InterruptedException{

		String colorA = playerA.getSymbol() == Symbol.JAUNE ? "jaune" : "rouge";
		String colorB = colorA.equals("jaune") ? "rouge" : "jaune";

		System.out.println(playerA.getName() + " commencera avec la couleur "  + colorA + " et le symbole " + playerA.getSymbol());
		System.out.println(playerB.getName() + " sera second avec la couleur " + colorB + " et le symbole " + playerB.getSymbol());

		Thread.sleep(1500);

		System.out.println(board);
		
		while(!gameIsFinished) {

			jouerTourSolo(playerA);
			jouerTourSolo(playerB);					
		}

		if(playerA.hasWin() || playerB.hasWin()) {

			String winner = " est le gagnant !";

			winner = playerA.hasWin() ? playerA.getName() + winner : playerB.getName() + winner;

			System.out.println(winner);
		}
		else
			System.out.println("Match Nul!");

		System.out.println("Voulez vous rejouez (O/N)?");
		sc.nextLine();
		String answer = sc.nextLine();

		if(answer.equalsIgnoreCase("o")) {

			gameIsFinished = false;
			board = new Board();
			start();
		}
		else
			System.out.println("Au revoir!");
	}
	
	public Game() {
		sc = new Scanner(System.in);
		board = new Board();
		players = new Player[2];
	}
	
	public static void main(String[] args) throws InterruptedException {

		Game game =  new Game();
		game.start();

	}	
}
