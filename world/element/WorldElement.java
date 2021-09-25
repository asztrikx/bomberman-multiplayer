package world.element;

import helper.Config;
import helper.Logger;
import helper.Position;
import server.WorldServer;

public abstract class WorldElement {
	public Position position = new Position(0, 0);
	public Animation animation = new Animation(0, 0, 0);
	public long createdTick = -1;
	public long destroyTick = -1;

	public boolean shouldDestroy(long tickCount) {
		return tickCount == destroyTick;
	}

	public abstract void destroy(Config config, Logger logger, WorldServer worldServer);

	public abstract void tick(Config config, Logger logger, WorldServer worldServer);

}
