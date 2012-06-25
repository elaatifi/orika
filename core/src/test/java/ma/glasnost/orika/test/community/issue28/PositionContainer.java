package ma.glasnost.orika.test.community.issue28;

import java.util.Collection;

public abstract class PositionContainer<T extends PositionContainer> {
	private Collection<Position<T>> positions;

	public Collection<Position<T>> getPositions() {
		return positions;
	}

	public void setPositions(Collection<Position<T>> positions) {
		this.positions = positions;
	}
}