import java.util.Arrays;

public class Board {
	
	private final Symbol[][] grid;
	
	public Board() {
		this.grid = new Symbol[6][7];
		initializeGrid();
	}
	
	/**
	 * Remplir la grille du symbole case vide
	 */
	private void initializeGrid() {

		for (Symbol[] symbols : grid)
			Arrays.fill(symbols, Symbol.VIDE);

	}

	/**
	 * Verifier si une colonne est pleine
	 * @param x Le numéro de la colonne
	 * @return Vrai ou Faux
	 */
	public boolean isColumnFull(int x) {

		return grid[0][x] != Symbol.VIDE;
	}


	/**
	 * Obtenir la position libre dans une colonne
	 * @param x Le numéro de la colonne
	 * @return Une position
	 */
	public Position getEmptyPositionForColumn(int x) {

		int y = grid.length - 1;

		while(grid[y][x] != Symbol.VIDE)
			y--;

		return new Position(x, y);
	}

	/**
	 * Assigner un symbol à une position dans la grille
	 * @param symbol le symbole à placer
	 * @param pos la position ou se trouvera le symbole
	 */
	public void setSymbolAtPosition(Symbol symbol, Position pos){

		this.grid[pos.y][pos.x] = symbol;

	}

	public Symbol[][] getGrid(){
		return this.grid;
	}

	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(" 0 1 2 3 4 5 6\n");

		for (Symbol[] symbols : grid) {

			for (Symbol symbol : symbols) {
				sb.append("|").append(symbol);
			}

			sb.append("|\n");
		}
		return sb.toString();
	}

}
