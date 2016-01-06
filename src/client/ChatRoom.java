package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import service.JChatClientService;

public class ChatRoom extends JFrame {

	private JPanel					contentPane;
	private JChatClientService		client;
	private JTextPane				output;
	private JTree					clientList;
	private JTextArea				input;
	private DefaultMutableTreeNode	root;
	private JLabel					onLineNum;
	boolean							isPrivate	= false;
	JLabel							locationLbl;
	JLabel							username;
	JLabel							chatRoom;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				Login login = new Login();
				login.setModal(true);
				login.setVisible(true);
				if (login.getDlgRsult() == Login.result.SUCCESS) {
					ChatRoom frame = null;
					try {
						frame = new ChatRoom();
						frame.setVisible(true);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, e.getMessage());
						e.printStackTrace();
						return;
					}
					frame.client = login.getClient();
					frame.client.setOutput(frame.output, frame.clientList, frame.root, frame.username, frame.chatRoom,
							frame.onLineNum);
					frame.client.getWindow(frame);
					Thread thread = new Thread(frame.client);
					thread.start();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ChatRoom() {
		setTitle("JChat Client");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				sendMsg("disconnect", "");
			}
		});
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(300, 300, 800, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JSplitPane leftRight = new JSplitPane();
		leftRight.setResizeWeight(0.15);
		contentPane.add(leftRight);

		JPanel leftPanel = new JPanel();
		leftRight.setLeftComponent(leftPanel);
		leftPanel.setLayout(new BorderLayout(0, 0));

		root = new DefaultMutableTreeNode("所有聊天室");
		clientList = new JTree(root);
		clientList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int level;
					String selectName;
					try {
						level = clientList.getSelectionPath().getPathCount();
						selectName = clientList.getLastSelectedPathComponent().toString();
					} finally {

					}
					if (level != 1) {
						isPrivate = level == 3;
						locationLbl.setText(selectName);
						if (level == 2) {
							clientList.expandRow(clientList.getSelectionRows()[0]);
							sendMsg("enterChannel", selectName);
						}
					}
				}
			}
		});
		JScrollPane scrollPaneTree = new JScrollPane();
		scrollPaneTree.setViewportView(clientList);
		leftPanel.add(scrollPaneTree);

		Panel leftTopPanel = new Panel();
		leftPanel.add(leftTopPanel, BorderLayout.NORTH);
		leftTopPanel.setLayout(new GridLayout(0, 2, 0, 6));

		JLabel lblNewLabel = new JLabel("欢迎使用");
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		leftTopPanel.add(lblNewLabel);

		JLabel lblNewLabel_2 = new JLabel("JChat V1.0");
		lblNewLabel_2.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		leftTopPanel.add(lblNewLabel_2);

		JLabel lblNewLabel_4 = new JLabel("您的昵称");
		lblNewLabel_4.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		leftTopPanel.add(lblNewLabel_4);

		username = new JLabel("username");
		username.setForeground(Color.BLUE);
		username.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		leftTopPanel.add(username);

		JLabel lblNewLabel_6 = new JLabel("所在位置");
		lblNewLabel_6.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		leftTopPanel.add(lblNewLabel_6);

		chatRoom = new JLabel("默认聊天室");
		chatRoom.setForeground(Color.RED);
		chatRoom.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		leftTopPanel.add(chatRoom);

		JLabel lblNewLabel_5 = new JLabel("在线人数");
		lblNewLabel_5.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		leftTopPanel.add(lblNewLabel_5);

		onLineNum = new JLabel("1");
		onLineNum.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		leftTopPanel.add(onLineNum);

		JPanel rightPanel = new JPanel();
		leftRight.setRightComponent(rightPanel);
		rightPanel.setLayout(new BorderLayout(0, 0));

		JSplitPane topDown = new JSplitPane();
		topDown.setResizeWeight(0.9);
		topDown.setOrientation(JSplitPane.VERTICAL_SPLIT);
		rightPanel.add(topDown, BorderLayout.CENTER);

		output = new JTextPane();
		//output.setLineWrap(true);
		output.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		output.setEditable(false);
		JScrollPane scrollPaneOut = new JScrollPane();
		scrollPaneOut.setViewportView(output);
		//topDown.setLeftComponent(output);
		topDown.setLeftComponent(scrollPaneOut);

		JPanel rightDownPanel = new JPanel();
		topDown.setRightComponent(rightDownPanel);
		rightDownPanel.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		rightDownPanel.add(toolBar, BorderLayout.NORTH);

		JLabel lblNewLabel_1 = new JLabel("发送至 [ ");
		lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		toolBar.add(lblNewLabel_1);

		locationLbl = new JLabel("默认聊天室");
		locationLbl.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		locationLbl.setForeground(Color.BLUE);
		toolBar.add(locationLbl);

		JLabel lblNewLabel_3 = new JLabel(" ]   ");
		lblNewLabel_3.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		toolBar.add(lblNewLabel_3);

		JButton btnNewButton = new JButton("抖动");
		btnNewButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		btnNewButton.setForeground(Color.RED);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendMsg("vibrate", "");
			}
		});
		toolBar.add(btnNewButton);

		JButton btnNewButton_1 = new JButton("帮助");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(contentPane, "左侧列表可双击频道名或用户名来加入频道或私聊某人", "帮助",
						JOptionPane.WARNING_MESSAGE);
			}
		});
		btnNewButton_1.setForeground(Color.LIGHT_GRAY);
		btnNewButton_1.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		toolBar.add(btnNewButton_1);

		input = new JTextArea();
		input.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		input.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (!e.isControlDown()) {
						send();
					} else {
						input.append("\n");
					}
					e.consume();
				}
			}
		});
		input.setLineWrap(true);
		JScrollPane scrollPaneIn = new JScrollPane();
		scrollPaneIn.setViewportView(input);
		rightDownPanel.add(scrollPaneIn, BorderLayout.CENTER);

		JButton sendBtn = new JButton("发送");
		sendBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!input.getText().equals("")) {
					input.setText("");
					sendMsg("msg", input.getText());
				}
			}
		});
		rightDownPanel.add(sendBtn, BorderLayout.EAST);
	}

	protected void send() {
		if (!input.getText().equals("")) {
			String command;
			if (isPrivate) {
				command = "private " + locationLbl.getText();
			} else {
				command = "channel " + locationLbl.getText();
			}
			sendMsg(command, input.getText());
			input.setText("");
		}
	}

	protected void sendMsg(String command, String content) {
		client.send(command, content);
	}

	protected void updateList() {
		sendMsg("updateList", "");
	}

	public void windowVib() {
		int x = getX();
		int y = getY();
		int[] dx = new int[] { 1, 1, 0, -1, -1, -1, 0, 1 };
		int[] dy = new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };
		setLocation(x + 10, y + 10);
		try {
			int t = 15;
			int d = 5;
			for (int i = 0; i < 7; i++) {
				for (int j = 0; j < 8; j++) {
					setLocation(x + dx[j] * d, y + dy[j] * d);
					Thread.sleep(t);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		setLocation(x, y);
	}
}
