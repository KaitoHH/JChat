package service;

import java.awt.Color;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import client.ChatRoom;
import msgJson.JSONMsg;

public class JChatClientService extends DatagramSocket implements Runnable {
	final static int				MAX_BUFFER_SIZE	= 1024;
	String							nickname;
	InetAddress						ip;
	int								port;
	private JLabel					name;
	private JTextPane				output;
	private JTree					clientList;
	private JLabel					onlineNum;
	private JLabel					chatRoom;
	private DefaultMutableTreeNode	chnl			= null;
	private DefaultMutableTreeNode	root;
	DefaultTreeModel				treeModel;
	ChatRoom						window;

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setNickname(String nickname) throws IOException {
		if (nickname.isEmpty()) throw new IOException("昵称不能为空");
		nickname = nickname.replace(' ', '_');
		this.nickname = nickname;
	}

	public JChatClientService() throws SocketException {
		super();
	}

	public String tryLink() throws IOException {
		setSoTimeout(7000);
		byte[] buff = JSONMsg.toByte("connect", nickname);
		DatagramPacket sendPacket = new DatagramPacket(buff, buff.length, ip, port);
		send(sendPacket);
		byte[] buffer = new byte[MAX_BUFFER_SIZE];
		DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
		receive(receivePacket);
		String msg = new String(receivePacket.getData(), 0, receivePacket.getLength(), "utf-8");
		return JSONMsg.getContent(msg);
	}

	/**
	 * @param output
	 *            要设置的 output
	 */
	public void setOutput(JTextPane output, JTree clientList, DefaultMutableTreeNode root, JLabel name,
			JLabel chatRoom,JLabel onLineNum) {
		this.output = output;
		this.clientList = clientList;
		this.root = root;
		this.name = name;
		this.chatRoom = chatRoom;
		this.onlineNum = onLineNum;
		name.setText(nickname);
	}

	@Override
	public void run() {
		byte[] buffer = new byte[MAX_BUFFER_SIZE];
		DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
		try {
			setSoTimeout(0);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		while (true) {
			try {
				receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String msg = "";
			try {
				msg = new String(receivePacket.getData(), 0, receivePacket.getLength(), "utf-8") + '\n';
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			processMsg(JSONMsg.getCommand(msg), JSONMsg.getContent(msg));

		}
	}

	private void processMsg(String command, String content) {
		String[] args = command.split(" ", 2);
		switch (args[0]) {
			case "msgbox":
				JOptionPane.showMessageDialog(null, content);
				break;
			case "admin":
				printMsg("[Server]" + content + "\n", Color.red, true);
				break;
			case "infoLow":
				printMsg("[Server]" + content + "\n", Color.gray, false);
				break;
			case "infoMid":
				printMsg("[Server]" + content + "\n", Color.green, false);
				break;
			case "online":
				printMsg(content + " 上线了\n", Color.gray);
				break;
			case "offline":
				printMsg(content + " 下线了\n", Color.gray);
				break;
			case "message":
				recevMessage(args, content);
				break;
			case "vibrate":
				window.windowVib();
				break;
			// 更新列表
			case "addChnl":
				if (chnl != null) {
					treeModel.insertNodeInto(chnl, root, root.getChildCount());
				}
				chnl = new DefaultMutableTreeNode(content);
				break;
			case "addClient":
				treeModel.insertNodeInto(new DefaultMutableTreeNode(content), chnl, chnl.getChildCount());
				break;
			case "listBegin":
				root.removeAllChildren();
				treeModel = new DefaultTreeModel(root);
				chnl = null;
				break;
			case "listEnd":
				treeModel.insertNodeInto(chnl, root, root.getChildCount());
				clientList.expandRow(0);
				clientList.updateUI();
				break;
			// 以上
			case "name":
				name.setText(content);
				break;
			case "privateRec":
				printMsg("[悄悄话]", Color.red);
				recevMessage(args, content);
				break;
			case "privateSend":
				printMsg(String.format("[你悄悄对%s]", args[1]), Color.red);
				args[1] = name.getText();
				recevMessage(args, content);
				break;
			case "enterSucc":
				chatRoom.setText(content);
				printMsg("---您已加入 ", Color.gray);
				printMsg(content, Color.orange);
				printMsg(" ---\n", Color.gray);
				break;
			case "onlineNum":
				onlineNum.setText(content);
				break;
			case "updateList":
				send(command, content);
				send("onlineNum", content);
				break;
			default:
				send(command, content);
				break;
		}
	}

	private void recevMessage(String args[], String content) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//设置日期格式
		String curTime = df.format(new Date());
		Color color = Color.blue;
		if (args[1].equals(name.getText())) {
			color = new Color(0, 128, 64);
		}
		printMsg(String.format("[%s]%s:\n", args[1], curTime), color);
		printMsg(content + "\n", Color.black, false);
	}

	public void send(String command, String content) {
		byte[] buff = null;
		try {
			buff = JSONMsg.toByte(command, content);
		} catch (UnsupportedEncodingException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
		DatagramPacket sendPacket = new DatagramPacket(buff, buff.length, ip, port);
		try {
			send(sendPacket);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

	public void printMsg(String content, Color c) {
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, c);
		StyleConstants.setFontSize(set, 16);
		setAndPrint(content, set);
	}

	public void printMsg(String content, Color c, boolean bold) {
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, c);
		StyleConstants.setFontSize(set, 18);
		StyleConstants.setBold(set, bold);
		setAndPrint(content, set);
	}

	private void setAndPrint(String content, SimpleAttributeSet set) {
		Document docs = output.getDocument();
		try {
			docs.insertString(docs.getLength(), content, set);//对文本进行追加
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		output.setCaretPosition(output.getDocument().getLength());
	}

	public void getWindow(ChatRoom chatroom) {
		window = chatroom;
	}
}
