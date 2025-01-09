import javax.sound.midi.SysexMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Computer extends Player {

	private List<Position> playablePositions;
	private List<Position> oppositePositions;

	public Computer(String nom, Symbol symbol, Board board) {
		super(nom, symbol, board);
	}

	/**
	 * Définir les tableaux :
	 * - Positions jouables: playablePositions
	 * - Positions adverses jouées: oppositePositions
	 */
	private void definePositions() {

		List<Position> playables = new ArrayList<>();
		List<Position> opposites = new ArrayList<>();
		Symbol[][] grid = getBoard().getGrid();

		// Parcourir la grille de jeu
		for (int y = 0; y < grid.length; y++) {

			for (int x = 0; x < grid[y].length; x++) {

				Position position = new Position(x, y);

				// si la position n'est ni vide ni le code couleur de l'ordinateur
				if (grid[y][x] != Symbol.VIDE && grid[y][x] != getSymbol())
					opposites.add(position); // on l'ajoute aux positions adverses

				// si la position est vide
				if (grid[y][x] == Symbol.VIDE) {

					// si la postion du dessous n'est pas vide
					if (y < 5 && grid[y + 1][x] != Symbol.VIDE)
						playables.add(position); // on l'ajoute aux positions jouables

						// si la positon est la dernière de la colonne
					else if (y == 5)
						playables.add(position); // on l'ajoute aux positions jouables
				}
			}
		}
		this.playablePositions = playables;
		this.oppositePositions = opposites;
	}

	/**
	 * Analyse une position jouable et une liste de positions
	 * afin de savoir si le joueur a deux jetons consecutifs au centre
	 * @param emptyPosition
	 * @param positionList
	 * @return
	 */
	private boolean hasTwoCentralPositions(Position emptyPosition, List<Position> positionList){

		for(Position currentPosition: positionList){
			int countPositive = 0, countNegative = 0;

			for(int i = 1; i <= 2; i++) {

				if (emptyPosition.x + i == currentPosition.x)
					countPositive++;

				else if (emptyPosition.x - i == currentPosition.x)
					countNegative++;
			}
			if(countPositive == 2 || countNegative == 2){
				if(playablePositions.contains(new Position(emptyPosition.x + 3, emptyPosition.y))
						|| playablePositions.contains(new Position(emptyPosition.x -3 , emptyPosition.y))){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Analyse les positions pour définir laquelle jouer en priorité
	 * @return La position à jouer
	 */
	public Position getPositionToPlay(){

		for(Position playable: playablePositions){
			if(isPositionWinnable(playable, getPlayedPositions()) || isPositionWinnable(playable, oppositePositions)){
				System.out.println("Winnable!");
				return playable;
			}
			else if(hasTwoCentralPositions(playable, oppositePositions) || hasTwoCentralPositions(playable, getPlayedPositions())){
				return playable;
			}
		}
		return getRandomPosition();
	}
	/**
	 * Gestion du comportement de jeu de l'IA
	 */
	public void play() {

		// Initialiser les tableaux de positions
		definePositions();

		if(oppositePositions.size() == 0){
			playAtPosition(new Position(3,5));
		}
		else if(getPlayedPositions().size() == 0){
			playAtPosition(getRandomPosition());
		}
		else
			playAtPosition(getPositionToPlay());

	}

	/**
	 * Obtenir une position aléatoire
	 */
	private Position getRandomPosition() {
		int randomIndex = (int) (Math.random() * getBoard().getGrid()[0].length);
		return getBoard().getEmptyPositionForColumn(randomIndex);
	}

	/**
	 * Jouer sur une position définie
	 * @param position La position à jouer
	 */
	private void playAtPosition(Position position) {

		getBoard().setSymbolAtPosition(getSymbol(), position);
		getPlayedPositions().add(position);
		decrementNumOfCoin();

	}

}
