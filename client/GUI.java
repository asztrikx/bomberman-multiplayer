package client;

import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import client.KeyCapturePanel.KeyMap;
import di.DI;
import helper.Config;
import helper.Key;
import helper.Logger;

public class GUI {
	private static Config config = (Config) DI.get(Config.class);
	private static Logger logger = (Logger) DI.get(Logger.class);

	public Draw draw = new Draw();
	public JFrame jFrame;
	public KeyCapturePanel panel;

	public enum State {
		Lobby, Ingame,
	}

	private State state = State.Lobby;

	/**
	 * @formatter:off
	 * Initializes swing gui
	 * @param connect executed when connect popup is confirmed
	 * @param disconnect executed when disconnect menu is clicked
	 * @param send server update function which will be called on new key press
	 * @param keys array which will be updated to reflect currently pressed keys
	 * @formatter:on
	 */
	public GUI(final BooleanSupplier connect, final Runnable disconnect, final Runnable send, final boolean[] keys) {
		jFrame = new JFrame();
		// add back height used by menu
		jFrame.setSize(config.windowWidth, config.windowHeight + 50);
		jFrame.setResizable(false);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				logger.println("GUI closed, closing any open connection...");
				disconnect.run();
			}
		});
		jFrame.setIconImage(new ImageIcon("resource/icon.png").getImage());

		final List<KeyMap> keyMaps = new ArrayList<>();
		keyMaps.add(new KeyMap(KeyEvent.VK_W, "up", Key.KeyType.KeyUp.getValue()));
		keyMaps.add(new KeyMap(KeyEvent.VK_D, "right", Key.KeyType.KeyRight.getValue()));
		keyMaps.add(new KeyMap(KeyEvent.VK_S, "down", Key.KeyType.KeyDown.getValue()));
		keyMaps.add(new KeyMap(KeyEvent.VK_A, "left", Key.KeyType.KeyLeft.getValue()));
		keyMaps.add(new KeyMap(KeyEvent.VK_SPACE, "bomb", Key.KeyType.KeyBomb.getValue()));
		panel = new KeyCapturePanel(keyMaps, keys, send::run);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setSize(config.windowWidth, config.windowHeight);
		panel.active = false;

		jFrame.setJMenuBar(createMenu(connect, disconnect));
		jFrame.setVisible(true);
	}

	/**
	 * @formatter:off
	 * Creates JMenuBar based on supplied events
	 * @param connect executed when connect popup is confirmed
	 * @param disconnect executed when disconnect menu is clicked
	 * @return menu based on connect and disconnect events
	 * @formatter:on
	 */
	public JMenuBar createMenu(final BooleanSupplier connect, final Runnable disconnect) {
		// menu
		final JMenuBar jMenuBar = new JMenuBar();
		JMenu jMenu;
		JMenuItem jMenuItem;

		// game
		jMenu = new JMenu("Game");
		jMenuBar.add(jMenu);

		jMenuItem = new JMenuItem("Connect");
		jMenuItem.addActionListener(e -> {
			final String address = (String) JOptionPane.showInputDialog(jFrame, "Address (ip:port)", "Connect",
					JOptionPane.PLAIN_MESSAGE, null, null, String.format("%s:%d", config.ip, config.port));
			// cancelled
			if (address == null) {
				return;
			}

			final String[] cols = address.split(":");
			if (cols[0].length() == 0 || cols[1].length() == 0) {
				JOptionPane.showMessageDialog(jFrame, "Wrong format");
				return;
			}

			if (state == State.Ingame) {
				disconnect.run();
			}

			config.ip = cols[0];
			config.port = Integer.parseInt(cols[1]);
			if (config.name.equals("")) {
				config.name = Config.defaultName;
			}
			// save every modification
			Config.saveConfig();

			if (!connect.getAsBoolean()) {
				JOptionPane.showMessageDialog(jFrame,
						String.format("Could not connect to %s:%d", config.ip, config.port));
			}
		});
		jMenu.add(jMenuItem);

		final JCheckBoxMenuItem jCheckBoxMenuItem = new JCheckBoxMenuItem("Auto reconnect");
		jCheckBoxMenuItem.addActionListener(e -> {
			config.autoreconnect = jCheckBoxMenuItem.getState();

			// save every modification
			Config.saveConfig();
		});
		jMenu.add(jCheckBoxMenuItem);

		jMenuItem = new JMenuItem("Disconnect");
		jMenuItem.addActionListener(e -> {
			if (state != State.Ingame) {
				JOptionPane.showMessageDialog(jFrame, "Not in game");
				return;
			}

			disconnect.run();
		});
		jMenu.add(jMenuItem);

		// settings
		jMenu = new JMenu("Settings");
		jMenuBar.add(jMenu);

		jMenuItem = new JMenuItem("Open settings");
		jMenuItem.addActionListener(e -> {
			if (Desktop.isDesktopSupported()) {
				try {
					final File myFile = new File("config.json");
					Desktop.getDesktop().open(myFile);
				} catch (final IOException e2) {
					logger.println("Could not open config.json");
				}
			}
		});
		jMenu.add(jMenuItem);

		jMenuItem = new JMenuItem("Player name");
		jMenuItem.addActionListener(e -> {
			String name = (String) JOptionPane.showInputDialog(jFrame, "Name", "Connect", JOptionPane.PLAIN_MESSAGE,
					null, null, config.name);
			// cancelled
			if (name == null) {
				return;
			}

			config.name = name;
			// save every modification
			Config.saveConfig();
		});
		jMenu.add(jMenuItem);

		return jMenuBar;
	}

	public void setState(final State state) {
		switch (state) {
			case Ingame:
				jFrame.add(panel);
				jFrame.setVisible(true);

				// control handle
				// - only after connected
				panel.add(draw);
				panel.active = true;

				// show after all elements are added
				panel.setVisible(true);
				jFrame.setVisible(true);

				// jframe has to be visible before draw added
				draw.init();

				break;
			case Lobby:
				panel.active = false;
				panel.remove(draw);
				panel.setVisible(false);
				break;
			default:
				break;
		}

		this.state = state;
	}
}
