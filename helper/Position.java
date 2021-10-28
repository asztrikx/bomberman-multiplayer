package helper;

import java.io.Serializable;

import di.DI;

public class Position implements Serializable {
	public int y, x;
	private static Config config = (Config) DI.services.get(Config.class);

	public Position(final int y, final int x) {
		this.y = y;
		this.x = x;
	}

	public Position(final Position position) {
		this.y = position.y;
		this.x = position.x;
	}

	public Position getSquare() {
		return new Position(y % config.squaresize, x % config.squaresize);
	}

	public Position sub(final Position position) {
		return new Position(y - position.y, x - position.x);
	}

	public Position shift(final Position offset) {
		return new Position(y + offset.y, x + offset.x);
	}

	@Override
	public boolean equals(final Object object) {
		if (!(object instanceof Position)) {
			throw new RuntimeException();
		}

		final Position position = (Position) object;

		return x == position.x && y == position.y;
	}
}
