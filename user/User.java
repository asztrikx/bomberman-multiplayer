package user;

import java.io.Serializable;

import helper.Auth;
import helper.Key;

public class User implements Serializable {
	public boolean[] keys = new boolean[Key.KeyType.KeyLength];
	public String name;
	public Auth auth;
	public State state;

	public enum State {
		Playing, Dead, Won,
	}
}
