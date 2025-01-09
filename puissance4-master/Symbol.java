
public enum Symbol {

	JAUNE("X"),
	ROUGE("O"),
	VIDE(".");
	
	private final String name;
	
	Symbol(String name) {
		this.name = name;
	}
	
	public String toString() {
		return this.name;
	}
}
