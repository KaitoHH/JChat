package server;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import msgJson.JSONMsg;
import service.JChatServerService;
import service.JChatServerService.Prefix;

public class JChatServer {

	final static int						MAX_BUFFER_SIZE		= 1024;
	final static int						MAX_ERROR_COUNT		= 10;
	final static String						DEFAULT_CHATROOM	= "默认聊天室";

	static Map<SocketAddress, Client>		onLine				= new HashMap<SocketAddress, Client>();

	// 昵称->地址
	static Map<String, SocketAddress>		onName				= new HashMap<String, SocketAddress>();

	// 昵称->频道名
	static Map<String, String>				onChnl				= new HashMap<String, String>();

	// 频道名->List<昵称>
	static Map<String, ArrayList<String>>	chnlList			= new HashMap<String, ArrayList<String>>();

	static int								onLineNum			= 0;
	static JChatServerService				server				= null;

	public static void main(String[] args) throws UnsupportedEncodingException {
		ServerFrame srvrfrmJchatServer = new ServerFrame();
		srvrfrmJchatServer.setTitle("JChat Server 1.0");
		srvrfrmJchatServer.setVisible(true);
		FileInputStream inputStream;
		Scanner scan;
		int port = 2417;
		try {
			inputStream = new FileInputStream("port.ini");
			scan = new Scanner(inputStream);
			port = scan.nextInt();
			scan.close();
		} catch (Exception e) {
		}


		// 绑定端口
		try {
			server = new JChatServerService(port, srvrfrmJchatServer.getLog());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "port binded error");
			JOptionPane.showMessageDialog(null, e.getStackTrace());
			System.exit(0);
		}

		chnlList.put(DEFAULT_CHATROOM, new ArrayList<String>());

		// 监听消息
		byte[] buffer = new byte[MAX_BUFFER_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		JChatServerService.printMsg(Prefix.Info, "server begins to listen...");
		int errCnt = 0;
		while (errCnt <= MAX_ERROR_COUNT) {
			// 接收消息
			try {
				server.receive(packet);
				errCnt = 0;
			} catch (IOException e) {
				JChatServerService.printMsg(Prefix.Error, "an error occurs when listening port.");
				JChatServerService.printMsg(Prefix.Error, e.getMessage());
				errCnt++;
			}

			// 处理当前消息
			String msg = new String(packet.getData(), 0, packet.getLength(), "utf-8");
			//System.out.println(msg);
			try {
				processMsg(JSONMsg.getCommand(msg), JSONMsg.getContent(msg), packet.getSocketAddress());
			} catch (Exception e) {
				JChatServerService.printMsg(Prefix.Error, "an error occurs when processing msg.");
				JChatServerService.printMsg(Prefix.Error, e.getMessage());
			}

		}
		JChatServerService.printMsg(Prefix.Error, "server stoped due to too many errors.");
	}

	static void processMsg(String command, String content, SocketAddress addr) throws Exception {
		String[] args = command.split(" ", 2);
		String temp;
		switch (args[0]) {
			case "connect":
				String id = String.format("%s(%s)", content, addr);
				JChatServerService.printMsg(Prefix.Info, id + " is connecting...");
				String ret = "";
				while (onName.containsKey(content)) {
					ret = "由于网络上有重名，已修改您的昵称\n";
					JChatServerService.printMsg(Prefix.Warn, "Find a same name " + content);
					content += '#';
					JChatServerService.printMsg(Prefix.Warn, "Rename to " + content);
				}
				id = String.format("%s(%s)", content, addr);
				onLine.put(addr, new Client(content));
				onName.put(content, addr);
				onChnl.put(content, DEFAULT_CHATROOM);
				chnlList.get(DEFAULT_CHATROOM).add(content);
				onLineNum++;
				JChatServerService.printMsg(Prefix.Info, id + " connected.");
				sendMsg("msg", ret + "连接成功", addr);
				sendMsg("name", content, addr);
				sendAll("online", content);
				sendAll("updateList", "");
				break;

			// 在线人数
			case "onlineNum":
				sendMsg(command, onLineNum + "", addr);
				break;
			// 频道消息
			case "channel":
				sendByChannel(args[1], "message " + whois(addr), content);
				temp = String.format("[%s]%s says: %s", args[1], getFullName(addr), content);
				JChatServerService.printMsg(Prefix.Info, temp);
				break;

			// 私聊消息
			case "private":
				sendMsg("privateRec " + whois(addr), content, onName.get(args[1]));
				sendMsg("privateSend " + args[1], content, addr);
				temp = String.format("%s says %s to[%s]", getFullName(addr), content, args[1]);
				JChatServerService.printMsg(Prefix.Info, temp);
				break;

			case "enterChannel": {
				String name = whois(addr);
				String chnl = onChnl.get(name);
				chnlList.get(chnl).remove(name);
				onChnl.put(name, content);
				chnlList.get(content).add(name);
				sendMsg("enterSucc", content, addr);
				sendAll("updateList", "");
			}
				JChatServerService.printMsg(Prefix.Info, getFullName(addr) + " entered into " + content);
				break;
			case "vibrate":
				sendAll("infoMid", "---" + whois(addr) + "发送了一个窗口抖动---");
				sendAll("vibrate", content);
				JChatServerService.printMsg(Prefix.Info, getFullName(addr) + " issued vibrate.");
				break;
			case "updateList":
				getList(addr);
				break;
			case "disconnect":
				removeClient(addr);
				break;
			default:
				JChatServerService.printMsg(Prefix.Warn, "Unknown command: " + command + " From " + getFullName(addr));
				break;
		}
	}

	private static void removeClient(SocketAddress addr) throws IOException {
		sendMsg("infoLow", "您已下线", addr);
		String name = whois(addr);
		onLine.remove(addr);
		onName.remove(name);
		String chnl = onChnl.get(name);
		onChnl.remove(name);
		chnlList.get(chnl).remove(name);
		onLineNum--;
		JChatServerService.printMsg(Prefix.Info, String.format("%s(%s) disconnected.", name, addr));
		sendAll("offline", name);
		sendAll("updateList", "");
	}
	private static String getFullName(SocketAddress addr) {
		return String.format("%s(%s)", whois(addr), addr);
	}


	private static void sendByChannel(String channelName, String command, String content) throws IOException {
		ArrayList<String> l = chnlList.get(channelName);
		for (String name : l) {
			sendMsg(command, content, onName.get(name));
		}
	}

	// 取得指定addr的昵称
	static String whois(SocketAddress addr) throws NullPointerException {
		return onLine.get(addr).name;
	}

	static SocketAddress whatis(String name) {
		return onName.get(name);
	}

	static void sendAll(String command, String content) throws IOException {
		for (SocketAddress key : onLine.keySet()) {
			sendMsg(command, content, key);
		}
	}

	static void sendMsg(String command, String content, SocketAddress addr) throws IOException {
		byte[] buff = JSONMsg.toByte(command, content);
		DatagramPacket sendPacket = new DatagramPacket(buff, buff.length, addr);
		server.send(sendPacket);
	}

	static void getList(SocketAddress addr) throws IOException {
		sendMsg("listBegin", "", addr);
		for (String key : chnlList.keySet()) {
			sendMsg("addChnl", key, addr);
			ArrayList<String> l = chnlList.get(key);
			for (String name : l) {
				sendMsg("addClient", name, addr);
			}
		}
		sendMsg("listEnd", "", addr);
	}

	static void createChannel(String name) throws IOException {
		if (!chnlList.containsKey(name)) {
			chnlList.put(name, new ArrayList<String>());
			JChatServerService.printMsg(Prefix.Info, name + " channel created.");
			sendAll("updateList", "");
		} else {
			JChatServerService.printMsg(Prefix.Error, "a same channel name existed!");
		}

	}

	static void removeChannel(String name) throws IOException {
		if (chnlList.containsKey(name) && !name.equals(DEFAULT_CHATROOM)) {
			chnlList.remove(name);
			sendAll("updateList", "");
			JChatServerService.printMsg(Prefix.Info, name + " channel deleted.");
		} else {
			JChatServerService.printMsg(Prefix.Error, "no channel called " + name);
		}
	}

	static void remove(String name) throws Exception {
		if (!onName.containsKey(name)) throw new Exception("does not cotains such client " + name);
		removeClient(onName.get(name));
	}
}

class Client {
	String	name;
	String	chnlName;

	public Client(String _name) {
		name = _name;
		chnlName = JChatServer.DEFAULT_CHATROOM;
	}

}

class ServerFrame extends JFrame {
	JTextArea log;

	public ServerFrame() {
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		JTextArea console = new JTextArea();
		console.setRows(5);
		console.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		console.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String cmd = console.getText();
					if (!cmd.equals("")) {
						console.setText("");
						processConsoleMsg(cmd);
					}
					e.consume();
				}
			}
		});

		getContentPane().add(console, BorderLayout.SOUTH);
		log = new JTextArea();
		log.setEditable(false);
		log.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		log.setLineWrap(true);
		getContentPane().add(log, BorderLayout.CENTER);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(log);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
	}

	protected void processConsoleMsg(String cmd) {
		JChatServerService.printMsg(Prefix.Info, "console issued " + cmd);
		String args[] = cmd.split(" ", 3);
		try {
			switch (args[0]) {
				case "createChannel":
					JChatServer.createChannel(args[1]);
					break;
				case "removeChannel":
					JChatServer.removeChannel(args[1]);
					break;
				case "remove":
					JChatServer.remove(args[1]);
					break;
				default:
					if (args.length == 3) {
						JChatServer.sendMsg(args[0], args[1], JChatServer.whatis(args[2]));
					} else {
						JChatServer.sendAll(args[0], args[1]);
					}

					break;
			}
		} catch (Exception e) {
			JChatServerService.printMsg(Prefix.Error, "an error occurs from console command.");
			JChatServerService.printMsg(Prefix.Error, e.getMessage());
		}

	}

	/**
	 * @return log
	 */
	public JTextArea getLog() {
		return log;
	}

}
