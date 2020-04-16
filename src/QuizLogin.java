import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javafx.scene.media.*;
import javax.swing.border.*;
import javafx.embed.swing.*;

class QuizLogin extends JFrame implements ActionListener {
	Cursor cursor;
	Image mouse_Img;
	JPanel background, panel3, panel4, panel5, panel6;
	JLabel label_nickName, label_Ip, background_img;
	JTextField tf_nickName, tf_Ip;
	JButton btn_Connect, btn_Exit;
	// Container cp;
	GridLayout main_Layout;
	ImageIcon back, bte,bts;
	ImageIcon iic = new ImageIcon("img\\msm.png");

	public static String ip, nickName;
	MediaPlayer p;

	public QuizLogin() {
		super("User Defined Cursor");

		Toolkit tk = Toolkit.getDefaultToolkit();
		mouse_Img = tk.getImage("img\\m8.png");
		Point point = new Point(0, 0);
		cursor = tk.createCustomCursor(mouse_Img, point, "maple_mouse");
		setCursor(cursor);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(10, 10);
		setVisible(false);
	}

	void init() {
		loadImg();

		background = new JPanel() {
			public void paintComponent(Graphics g) {
				g.drawImage(back.getImage(), 0, 0, null);
				setOpaque(false); // 그림을 표시하게 설정,투명하게 조절
				super.paintComponent(g);
			}
		};

		background.setBorder(null);
		setContentPane(background);
		background.setLayout(null);

		panel3 = new JPanel();
		tf_nickName = new JTextField(10);
		panel3.setBounds(208, 186, 164, 30);
		tf_nickName.setBounds(208, 186, 164, 30);
		panel3.setOpaque(false);
		// tf_nickName.setOpaque(false);
		background.add(panel3);
		background.add(tf_nickName);
		panel3.setLayout(null);

		panel4 = new JPanel();
		tf_Ip = new JTextField(15);
		panel4.setBounds(208, 220, 164, 30);
		tf_Ip.setBounds(208, 220, 164, 30);
		panel4.setOpaque(false);
		background.add(panel4);
		background.add(tf_Ip);
		panel4.setLayout(null);

		panel5 = new JPanel();
		btn_Connect = new JButton(bts);
		btn_Connect.setFont(new Font("magic R", Font.BOLD, 25));
		btn_Connect.setForeground(Color.WHITE);
		btn_Connect.setFocusPainted(false);
		btn_Connect.setBorderPainted(false);
		btn_Connect.setContentAreaFilled(false);
		panel5.setBounds(126, 280, 270, 36);
		btn_Connect.setBounds(126, 280, 270, 36);
		panel5.setOpaque(false);
		background.add(panel5);
		background.add(btn_Connect);
		panel5.setLayout(null);

		panel6 = new JPanel();
		btn_Exit = new JButton(bte);
		btn_Exit.setFont(new Font("magic R", Font.BOLD, 25));
		btn_Exit.setForeground(Color.WHITE);
		btn_Exit.setFocusPainted(false);
		btn_Exit.setBorderPainted(false);
		btn_Exit.setContentAreaFilled(false);
		panel6.setBounds(126, 317, 270, 36);
		btn_Exit.setBounds(126, 317, 270, 36);
		panel6.setOpaque(false);
		background.add(panel6);
		background.add(btn_Exit);
		panel6.setLayout(null);

		btn_Connect.addActionListener(this);
		btn_Exit.addActionListener(this);

		setUI();
		bgm("//Play");
	}

	void loadImg() {
		try {
			back = new ImageIcon(ImageIO.read(new File("img/메이플10.png")));
			bts = new ImageIcon(ImageIO.read(new File("img/ENTER.png")));
			bte = new ImageIcon(ImageIO.read(new File("img/EXIT.png")));
		} catch (IOException ie) {
		}
	}

	void setUI() {
		setTitle("QuizQuiz Login");
		setSize(549, 463);
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void bgm(String play) { // BGM 재생 & 정지
		try {
			if (play.equals("//Play")) {
				JFXPanel panel = new JFXPanel();
				File f = new File("bgm\\stbgm.wav");
				Media bgm = new Media(f.toURI().toURL().toString());
				p = new MediaPlayer(bgm);
				p.play();
			} else if (play.equals("//Stop")) {
				p.stop();
				p.setMute(true);
				p.dispose();
			}
		} catch (Exception e) {
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btn_Connect) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
			}
			btn_Connect.setEnabled(false);
			if (tf_nickName.getText().equals("")) {
				JOptionPane.showMessageDialog(null, "닉네임을 입력해 주세요!", "ERROR!", JOptionPane.WARNING_MESSAGE, iic);
				btn_Connect.setEnabled(true);
			} else if (tf_nickName.getText().trim().length() > 5) {
				JOptionPane.showMessageDialog(null, "ID는 5글자만가능합니다!", "ERROR!", JOptionPane.WARNING_MESSAGE, iic);
				btn_Connect.setEnabled(true);
			} else {// 입력이 되었을 경우
				nickName = tf_nickName.getText().trim();
				String ipPattern = tf_Ip.getText();
				// 올바르게 입력된경우
				if (ipPattern.matches(
						"(^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$)")) {
					ip = ipPattern;
					JOptionPane.showMessageDialog(null, "로그인 성공!", "QuizServer LOGIN", JOptionPane.INFORMATION_MESSAGE, iic);
					btn_Connect.setEnabled(false);
					tf_nickName.setEnabled(false);
					tf_Ip.setEnabled(false);
					setVisible(false);// 로그인 시 안보이게함
					QuizClient qc = new QuizClient();// QuizClient 생성에서 - socket 연결
					bgm("//Stop");
				} else {//// 올바르게 입력안된 경우
					if (ipPattern.length() == 0) {// ip 아무것도 입력안했을 경우
						ip = "127.0.0.1";
						btn_Connect.setEnabled(false);
						tf_nickName.setEnabled(false);
						tf_Ip.setEnabled(false);
						setVisible(false);// 로그인 시 안보이게함
						QuizClient qc = new QuizClient();// QuizClient 생성에서 - socket 연결
						bgm("//Stop");
					} else {
						JOptionPane.showMessageDialog(null, "IP 주소를 정확하게 입력해 주세요! ", "ERROR!", JOptionPane.WARNING_MESSAGE, iic);
						btn_Connect.setEnabled(true);
					}
				}
			}
		} else if (e.getSource() == btn_Exit) {
			try {
				btn_Exit.setEnabled(false);
				Thread.sleep(10);
				int c = JOptionPane.showConfirmDialog(null, "종료하시나요?", "로그인종료", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, iic);
				if (c == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			} catch (InterruptedException ipe) {
			}
			btn_Exit.setEnabled(true);
		}
	}

	public static void main(String[] args) {
		QuizLogin ql = new QuizLogin();
		ql.init();
	}
}
