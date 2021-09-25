package server;

import java.util.Timer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import engine.Collision;
import engine.Tick;
import helper.Auth;
import helper.AutoClosableLock;
import helper.Config;
import helper.Key;
import helper.Logger;
import helper.Position;
import network.Listen;
import user.User;
import user.UserManager;
import world.element.Movable;

public class Server {
	UserManager<UserServer> userManager = new UserManager<>();
	Lock mutex = new ReentrantLock();
	Config config;
	Logger logger;
	WorldServer worldServer;
	Timer timer = null;
	Listen listen = null;
	Tick tick;

	public Server(Config config, Logger logger) {
		this.logger = logger;
		this.config = config;
		this.worldServer = new WorldServer(config, logger); // not critical section
		Collision collision = new Collision(config, logger);
		this.tick = new Tick(worldServer, config, mutex, collision);
	}

	public void Listen(int port) {
		listen = new Listen(port);

		// tick start: world calc, connected user update
		timer = new Timer();
		timer.schedule(tick, config.tickRate);
		timer.wait();
	}

	// EventKey handles WorldServer saving
	static int EventKey(void* data, SDL_Event* sdl_event){
		if(
			sdl_event->type != SDL_KEYDOWN ||
			sdl_event->key.keysym.sym != SDLK_q
		){
			return 0;
		}

		try (AutoClosableLock autoClosableLock = new AutoClosableLock(mutex)) {
			Save();
		}

		return 0;
	}

	// ServerReceive gets updates from users
	// userServerUnsafe is not used after return
	void ServerReceive(UserServer userServerUnsafe) {
		if (stopped) {
			return;
		}

		try (AutoClosableLock autoClosableLock = new AutoClosableLock(mutex)) {
			// auth validate
			// auth's length validation
			// -
			if (userServerUnsafe.auth.length() != config.authLength) {
				return;
			}
			// auth's length validation
			// -
			UserServer userServer = userManager.findByAuth(userServerUnsafe.auth);
			if (userServer == null) {
				return;
			}
			Movable character = CharacterFind(userServer);

			// alive
			if (character == null) {
				return;
			}

			// name change
			// TODO java max 15 length
			if (!userServer.name.equals(userServerUnsafe.name)) {
				userServer.name = userServerUnsafe.name;
			}

			// keys's length validation
			// -

			// keys copy
			for (int i = 0; i < Key.KeyType.KeyLength; i++) {
				character.keys[i] = userServerUnsafe.keys[i];
			}
		}
	}

	// ServerStop clears server module
	void ServerStop() {
		if (!SDL_RemoveTimer(tickId)) {
			SDL_Log("ServerStop: SDL_RemoveTimer: %s", SDL_GetError());
			exit(1);
		}

		// wait timers to finish
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(mutex)) {
			// need to be called before NetworkServerStop as incoming message may already be
			// coming which
			// could get stuck if SDL_DestroyMutex happens before SDL_LockMutex
			stopped = true;

			NetworkServerStop();
		}
	}

	// ServerConnect register new connection user, returns it with auth
	// userServerUnsafe is not used after return
	void ServerConnect(UserServer userServerUnsafe) {
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(mutex)) {
			// userServer copy
			UserServer userServer = new UserServer();
			// TODO java max 15 length
			userServer.name = userServerUnsafe.name;
			userServer.state = User.State.Playing;

			// userServer insert
			userManager.add(userServer);

			// id generate
			while (true) {
				Auth auth = new Auth(config.authLength);

				// id exists
				if (userManager.findByAuth(auth) == null) {
					userServer.auth = auth;
					break;
				}
			}

			// spawn
			Position position = SpawnGet(worldServer, 3);

			// character insert
			Movable character = new Movable(config, logger);
			character.bombCount = 1;
			character.owner = userServer;
			character.position = new Position(position.y, position.x);
			character.type = Movable.CharacterType.CharacterTypeUser;
			character.velocity = config.velocity;
			worldServer.characterList.add(character);

			// reply
			userServerUnsafe.auth = userServer.auth;
		}
	}

}
