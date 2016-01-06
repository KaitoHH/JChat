package service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextArea;

public class JChatServerService extends DatagramSocket {

	public enum Prefix {
		Info, Error, Warn, Custom
	}

	final static int	MAX_DATA_LENGTH	= 512;
	byte[]				buffer;
	static String		logFileName;
	static JTextArea	log;

	public JChatServerService(int port, JTextArea _log) throws SocketException {
		super(port);
		initLogFile();
		log = _log;
		printMsg(Prefix.Info, "Initializing buffer...");
		buffer = new byte[MAX_DATA_LENGTH];
		printMsg(Prefix.Info, "JChat Server created on " + getLocalPort());
	}

	private void initLogFile() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		logFileName = "log\\" + format.format(new Date()) + ".log";
		new File("log").mkdir();
	}

	public static void printMsg(String customize, String msg) {
		_printMsg(customize, msg);
	}

	public static void printMsg(Prefix p, String msg) {
		_printMsg(p.toString(), msg);
	}

	private static void _printMsg(String p, String msg) {
		String message = String.format("[%s %s] %s\n", p, new Date(), msg);
		//System.out.print(message);
		log.append(message);
		log.setCaretPosition(log.getDocument().getLength());
		try {
			FileWriter fWriter = new FileWriter(logFileName, true);
			fWriter.write(message);
			fWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param log
	 *            要设置的 log
	 */
	public void setLog(JTextArea _log) {
		log = _log;
	}

	@Override
	public void close() {
		printMsg(Prefix.Info, "Server closing...");
		super.close();
		printMsg(Prefix.Info, "Server closed.");
	}

}

