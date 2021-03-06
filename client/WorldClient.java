package client;

import user.User.State;
import world.World;
import world.element.movable.Movable;
import world.element.movable.Player;

public class WorldClient extends World {
	public State state;

	/**
	 * @formatter:off
	 * Finds the player which corresponds to us
	 * @return
	 * @formatter:on
	 */
	public Player findMe() {
		for (final Movable movable : movables) {
			if (movable instanceof Player) {
				final Player player = (Player) movable;
				if (player.you) {
					return player;
				}
			}
		}

		return null;
	}
}
