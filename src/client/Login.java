package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import service.JChatClientService;

public class Login extends JDialog {

	public static enum result {
		EXIT, SUCCESS;
	}

	private static final long	serialVersionUID	= 3207761751002742650L;
	private final JPanel		contentPanel		= new JPanel();
	private JTextField			nickname;
	private JTextField			server;
	private JTextField			port;
	private result				dlgRsult			= result.EXIT;
	JChatClientService			client;
	/**
	 * Create the dialog.
	 */
	public Login() {
		setResizable(false);
		setTitle("JChat V1.0");
		setBounds(100, 100, 372, 278);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel middle_panel = new JPanel();
			contentPanel.add(middle_panel, BorderLayout.CENTER);
			middle_panel.setLayout(null);

			nickname = new JTextField();
			nickname.addKeyListener(new KeyAdapter() {
				@Override

				public void keyTyped(KeyEvent e) {
					if (e.getKeyChar() == ' ') {
						e.consume();
					}
				}

			});
			nickname.setFont(new Font("微软雅黑", Font.PLAIN, 20));
			nickname.setBounds(150, 26, 156, 27);
			middle_panel.add(nickname);
			nickname.setColumns(10);
			{
				server = new JTextField();
				server.setFont(new Font("微软雅黑", Font.PLAIN, 20));
				server.setColumns(10);
				server.setBounds(150, 63, 156, 27);
				middle_panel.add(server);
			}

			JLabel lblNewLabel = new JLabel("昵称");
			lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
			lblNewLabel.setBounds(89, 27, 46, 27);
			middle_panel.add(lblNewLabel);

			JLabel label = new JLabel("服务器地址");
			label.setFont(new Font("微软雅黑", Font.PLAIN, 20));
			label.setBounds(35, 64, 100, 27);
			middle_panel.add(label);

			JLabel lblNewLabel_1 = new JLabel("端口");
			lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 20));
			lblNewLabel_1.setBounds(89, 100, 46, 27);
			middle_panel.add(lblNewLabel_1);

			port = new JTextField();
			port.setFont(new Font("微软雅黑", Font.PLAIN, 20));
			port.setColumns(10);
			port.setBounds(150, 100, 156, 27);
			middle_panel.add(port);
		}

		JPanel panel = new JPanel();
		contentPanel.add(panel, BorderLayout.NORTH);

		JLabel lblNewLabel_2 = new JLabel("欢迎使用JChat");
		lblNewLabel_2.setFont(new Font("微软雅黑", Font.PLAIN, 24));
		panel.add(lblNewLabel_2);

		JLabel lblNewLabel_3 = new JLabel("");
		lblNewLabel_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showMessageDialog(contentPanel, "JChat V1.0\n===========\nBy Huang hui 2016", "JChat",
						JOptionPane.WARNING_MESSAGE);
			}
		});
		lblNewLabel_3.setIcon(new ImageIcon("img/login.png"));
		panel.add(lblNewLabel_3);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("连接");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						connect();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("取消");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	public void connect() {
		try {
			client = new JChatClientService();
			client.setNickname(nickname.getText());
			client.setIp(InetAddress.getByName(server.getText()));
			client.setPort(Integer.parseInt(port.getText()));
			JOptionPane.showMessageDialog(contentPanel, client.tryLink());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(contentPanel, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}
		setDlgRsult(result.SUCCESS);
		setVisible(false);
	}

	/**
	 * @return dlgRsult
	 */
	public result getDlgRsult() {
		return dlgRsult;
	}

	/**
	 * @param dlgRsult
	 *            要设置的 dlgRsult
	 */
	public void setDlgRsult(result dlgRsult) {
		this.dlgRsult = dlgRsult;
	}

	/**
	 * @return client
	 */
	public JChatClientService getClient() {
		return client;
	}

}
