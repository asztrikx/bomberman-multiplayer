package helper;

import java.io.OutputStream;
import java.io.PrintWriter;

public class Logger extends PrintWriter {
	public Logger(OutputStream outputStream) {
		super(outputStream);
	}
}
