import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Player {

	private final String name;
	private int nbreJetons;
	private final List<Position> playedPositions;
	private final Symbol symbol;
	private final Board board;
	
	public Player(String name, Symbol symbol, Board board) {
		this.nbreJetons = 21;
		this.playedPositions = new ArrayList<>();
		this.name = name;
		this.symbol = symbol;
		this.board = board;
	}

	/**
	 * Ajouter un jeton dans la colonne, décrémenter le total de jetons
	 * et ajouter la position dans le tableau des positions jouées
	 * @param column La colonne ou déposer le jeton
	 */
	public void playAtColumn(int column){

		Position position = board.getEmptyPositionForColumn(column);

		board.setSymbolAtPosition(symbol, position);
		playedPositions.add(position);
		nbreJetons--;
	}
	
	/**
	 * Savoir si le joueur a gagné
	 * @return True si le joueur a 4 jetons consécutifs ou False sinon
	 */
	public boolean hasWin() {

		for(Position currentPosition: playedPositions) {
			if(isPositionWinnable(currentPosition, playedPositions))
				return true;
		}
		return false;
	}

	/**
	 * Teste si une position peut remporter la victoire
	 * @param position La position a tester
	 * @param positionsList La liste de positions à comparer
	 * @return True si la position est gagnante ou False sinon
	 */
	protected boolean isPositionWinnable(Position position, List<Position> positionsList){

		HashMap<String, List<List<Position>>> consecutivePositions = this.getConsecutivePositions(position, positionsList);

		for(String direction: consecutivePositions.keySet()){

			for(List<Position> positions: consecutivePositions.get(direction)){
				if(positions.size() >= 3){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Parcoure une liste de position et ajoute les positions consécutives dans une liste
	 * à celle passée en paramètre pour chaque direction
	 * @param positionToCompare La position à analyser
	 * @param positionsList La liste de positions où chercher les consecutives
	 * @return Une HashMap avec pour clef la direction et pour valeur une liste de liste de positions
	 */
	protected HashMap<String, List<List<Position>>> getConsecutivePositions(Position positionToCompare , List<Position> positionsList) {

		HashMap<String, List<List<Position>>> consecutivePositions = new HashMap<>();
		consecutivePositions.put("horizontal", new ArrayList<>());
		consecutivePositions.put("vertical", new ArrayList<>());
		consecutivePositions.put("diagonalUp", new ArrayList<>());
		consecutivePositions.put("diagonalDown", new ArrayList<>());


				for(String direction: consecutivePositions.keySet()){

					List<List<Position>> globalList = consecutivePositions.get(direction);
					List<Position> newPositions = new ArrayList<>();

						for (int i = 1; i <= 3; i++) {

							Position besidePosition = null;

							switch (direction) {
								case "horizontal":
									besidePosition = new Position(positionToCompare.x + i, positionToCompare.y);
									break;

								case "vertical":
									besidePosition = new Position(positionToCompare.x, positionToCompare.y + i);
									break;

								case "diagonalUp":
									besidePosition = new Position(positionToCompare.x + i, positionToCompare.y - i);
									break;

								case "diagonalDown":
									besidePosition = new Position(positionToCompare.x + i, positionToCompare.y + i);
									break;
							}

							if (positionsList.contains(besidePosition)) {
								newPositions.add(besidePosition);
							}
						}
						globalList.add(newPositions);
					    consecutivePositions.put(direction, globalList);
		}

		return consecutivePositions;
	}


	public String getName() {
		return name;
	}

	public int getNbreJetons() {
		return nbreJetons;
	}

	public List<Position> getPlayedPositions() {
		return playedPositions;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public Board getBoard() {
		return board;
	}

	public void decrementNumOfCoin(){
		this.nbreJetons--;
	}
}
