
public class Position {

	final int x;
	final int y;
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "Position [x=" + x + ", y=" + y + "]";
	}
	
	@Override
	public boolean equals(Object o) {
		if(o.getClass() != this.getClass())
			return false;
		Position p = (Position)o;
		return this.x == p.x && this.y == p.y;
	}
}
