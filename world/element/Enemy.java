package world.element;

import java.security.SecureRandom;

import helper.Config;
import helper.Key;
import helper.Logger;
import server.WorldServer;

public class Enemy extends Movable {
	public Enemy(Config config, Logger logger) {
		super(config, logger);
	}

	@Override
	public void move(WorldServer worldServer, long tickCount) {
		// move
		super.move(worldServer, tickCount);

		SecureRandom secureRandom = new SecureRandom();

		// decide to change direction
		if (secureRandom.nextDouble() > config.enemyKeyChangePossibility) {
			return;
		}

		// roll new direction
		for (int i = 0; i < Key.KeyType.KeyLength; i++) {
			keys[i] = false;
		}
		keys[secureRandom.nextInt(Key.KeyType.KeyLength)] = true;

	}
}
