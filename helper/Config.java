package helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import di.DI;

public class Config {
	// TODO should be same between server and client
	public int squaresize = 50;
	public int velocityPlayer = 6;
	public int velocityEnemy = 1;
	// TODO make position etc for others, optimize after like offset variable
	public int windowHeight = 480;
	public int windowWidth = 640;
	public int worldHeight = 9;
	public int worldWidth = 13;
	public double boxRatio = 0.25;
	public double enemyRatio = 0.05;
	public double enemyKeyChangePossibility = 0.0015;
	public long tickRate = 1000 / 58;
	public long tickSecond = 1000 / tickRate; // tick count in one second
	public int authLength = 26;
	public int nameMaxLength = 15;
	public int bombCountStart = 1;
	public int spawnSquareDistanceFromOthers = 3;
	public int spawnPlayerSquareFreeSpace = 3;
	public boolean autoreconnect = false;
	public String ip;
	public int port;
	public String name;
	public boolean debug;

	public static String defaultIP = "127.0.0.1";
	public static int defaultPort = 32469;
	public static String defaultName = "player";
	public static String configFileName = "config.json";

	public static Config getConfig() throws IOException {
		Config config = new Config();
		if (config.ip == null) {
			config.ip = Config.defaultIP;
		}
		if (config.name == null) {
			config.name = Config.defaultName;
		}

		// if file doesnt exists create
		final File configFile = new File(configFileName);
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if (!configFile.exists()) {
			final String configJson = gson.toJson(config);
			final FileWriter fileWriter = new FileWriter(configFile);
			fileWriter.write(configJson);
			fileWriter.close();

		} else {
			final String json = Files.readString(Path.of(configFileName));
			config = gson.fromJson(json, Config.class);
		}
		return config;
	}

	public static void saveConfig() {
		final Config config = (Config) DI.get(Config.class);
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();

		final String configJson = gson.toJson(config);
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(configFileName);
			fileWriter.write(configJson);
			fileWriter.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
