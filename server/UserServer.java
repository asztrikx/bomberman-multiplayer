package server;

import network.Network.Connection;
import user.User;

public class UserServer extends User {
	public Connection connection;

	public UserServer(final Connection connection) {
		this.connection = connection;
	}
}
