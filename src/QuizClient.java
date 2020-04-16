import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javafx.embed.swing.*;
import javafx.scene.media.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javafx.embed.swing.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.util.*;

public class QuizClient extends JFrame implements ActionListener {
	JPanel contentPane, panel_Main;
	JLabel label_background;
	JButton btn_Ready, btn_Ready_Cancel, btn_o, btn_x, btn_Exit;
	JLabel label_Timer;
	JLabel label_Client1, label_Client2, label_Client3, label_Client4;
	JLabel label_Client1_Image, label_Client2_Image, label_Client3_Image, label_Client4_Image;

	JTextArea chat_Area, quiz_Area;
	JTextField chat_Field;
	JScrollPane scrollPane_chat_Area, scrollPane_quiz_Area;
	Clip clip;
	Cursor cursor;
	Image mouse_img;
	ImageIcon iic = new ImageIcon("img\\msm.png");
	int port = 5000;
	int tmpCnt;// ����Ȯ���Ҷ� ����
	String playerName, playerScore, playerIdx; // Ŭ���̾�Ʈ �̸�,����, �ε��� ����
	boolean gameStart;
	Vector<String> v_Result_Quizs = new Vector<String>();// ���¹����� ��´�.
	String result_NickName;
	String temp_quiz;
	String question_Answer[], answer_o, answer_x; // ����_��, ��ư��
	int check_Cnt;
	// question_Answer[0] = ����
	// question_Answer[1] = ��

	void setUI() {
		// �⺻ GUI ����
		setFont(new Font("�����ٸ����", Font.PLAIN, 13));
		setVisible(true);
		setResizable(false);
		setTitle("Quiz Client");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1300, 700);
		setLocationRelativeTo(null);
	}

	public QuizClient() {
		// ���콺 Ŀ��
		super("User Defined Cursor");

		Toolkit tk = Toolkit.getDefaultToolkit();
		mouse_img = tk.getImage("img\\m8.png");
		Point point = new Point(0, 0);
		cursor = tk.createCustomCursor(mouse_img, point, "maple");
		setCursor(cursor);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(10, 10);
		setVisible(false);

		setUI();

		// ���̽� �г�
		contentPane = new JPanel();
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

		panel_Main = new JPanel() {
			public void paintComponent(Graphics g) {
				g.drawImage(new ImageIcon("img\\���2.png").getImage(), 0, 0, null);
				setOpaque(false); // �׸��� ǥ���ϰ� ����,�����ϰ� ����
				super.paintComponent(g);
			}
		};
		contentPane.add(panel_Main);
		panel_Main.setLayout(null);

		// ������ ���
		label_Client1 = new JLabel("[�г��� & ����]");
		label_Client1.setBounds(90, 578, 150, 40);
		label_Client1.setFont(new Font("MapleStory", Font.BOLD, 14));
		label_Client1_Image = new JLabel(new ImageIcon("img\\p0.png"));
		label_Client1_Image.setBounds(62, 370, 180, 200);
		label_Client1.setBackground(Color.WHITE);
		label_Client1.setHorizontalAlignment(JLabel.CENTER);
		label_Client1.setOpaque(true);
		label_Client1_Image.setOpaque(false);

		label_Client2 = new JLabel("[�г��� & ����]");
		label_Client2.setBounds(275, 578, 150, 40);
		label_Client2.setFont(new Font("MapleStory", Font.BOLD, 14));
		label_Client2_Image = new JLabel(new ImageIcon("img\\p0.png"));
		label_Client2_Image.setBounds(260, 370, 180, 200);
		label_Client2.setBackground(Color.WHITE);
		label_Client2.setHorizontalAlignment(JLabel.CENTER);
		label_Client2.setOpaque(true);
		label_Client2_Image.setOpaque(false);

		label_Client3 = new JLabel("[�г��� & ����]");
		label_Client3.setBounds(475, 578, 150, 40);
		label_Client3.setFont(new Font("MapleStory", Font.BOLD, 14));
		label_Client3_Image = new JLabel(new ImageIcon("img\\p0.png"));
		label_Client3_Image.setBounds(460, 370, 180, 200);
		label_Client3.setBackground(Color.WHITE);
		label_Client3.setHorizontalAlignment(JLabel.CENTER);
		label_Client3.setOpaque(true);
		label_Client3_Image.setOpaque(false);

		label_Client4 = new JLabel("[�г��� & ����]");
		label_Client4.setBounds(675, 578, 150, 40);
		label_Client4.setFont(new Font("MapleStory", Font.BOLD, 14));
		label_Client4_Image = new JLabel(new ImageIcon("img\\p0.png"));
		label_Client4_Image.setBounds(660, 370, 180, 200);
		label_Client4.setBackground(Color.WHITE);
		label_Client4.setHorizontalAlignment(JLabel.CENTER);
		label_Client4.setOpaque(true);
		label_Client4_Image.setOpaque(false);

		// ���� ��ư
		btn_Ready = new JButton(new ImageIcon("img\\R.png"));
		btn_Ready.setBounds(900, 30, 170, 40);
		btn_Ready.setFocusPainted(false);
		btn_Ready.setBorderPainted(false);
		btn_Ready.setContentAreaFilled(false);

		btn_Ready_Cancel = new JButton(new ImageIcon("img\\C.png"));
		btn_Ready_Cancel.setBounds(1070, 30, 170, 40);
		btn_Ready_Cancel.setEnabled(false);
		btn_Ready_Cancel.setFocusPainted(false);
		btn_Ready_Cancel.setBorderPainted(false);
		btn_Ready_Cancel.setContentAreaFilled(false);

		btn_Exit = new JButton(new ImageIcon("img\\E.png"));
		btn_Exit.setFocusPainted(false);
		btn_Exit.setBorderPainted(false);
		btn_Exit.setContentAreaFilled(false);
		btn_Exit.setBounds(900, 577, 340, 40);
		// ox��ư
		btn_o = new JButton(new ImageIcon("img\\O.png"));
		btn_o.setEnabled(false);// o��Ȱ��ȭ
		btn_o.setBounds(675, 67, 200, 150);
		btn_o.setFocusPainted(false);
		btn_o.setBorderPainted(false);
		btn_o.setContentAreaFilled(false);

		btn_x = new JButton(new ImageIcon("img\\X.png"));
		btn_x.setEnabled(false);// x��Ȱ��ȭ
		btn_x.setBounds(675, 217, 200, 150);
		btn_x.setFocusPainted(false);
		btn_x.setBorderPainted(false);
		btn_x.setContentAreaFilled(false);

		// ä��â
		chat_Area = new JTextArea();
		chat_Area.setFont(new Font("MapleStory", Font.BOLD, 15));
		chat_Area.setBackground(new Color(245, 232, 226));
		chat_Area.setForeground(Color.BLACK);
		chat_Area.setEditable(false);
		chat_Area.setBorder(new LineBorder(new Color(181, 97, 61), 2, false));

		scrollPane_chat_Area = new JScrollPane(chat_Area);
		scrollPane_chat_Area.setBounds(900, 86, 340, 450);

		chat_Field = new JTextField();
		chat_Field.setBounds(900, 538, 340, 30);
		chat_Field.setBorder(new LineBorder(new Color(181, 97, 61), 2, false));
		chat_Field.setBackground(new Color(245, 232, 226));

		// Ÿ�̸�
		label_Timer = new JLabel("00 : 00");
		label_Timer.setFont(new Font("Snap ITC", Font.BOLD, 30));
		label_Timer.setForeground(Color.WHITE);
		label_Timer.setBounds(420, 10, 160, 50);
		label_Timer.setVisible(false);

		// ���� ���� ����
		quiz_Area = new JTextArea();
		quiz_Area.setFont(new Font("�����ٸ����", Font.BOLD, 20));      
		quiz_Area.setForeground(Color.BLACK);
		quiz_Area.setBackground(new Color(245, 232, 226));
		quiz_Area.setEditable(false);

		scrollPane_quiz_Area = new JScrollPane(quiz_Area);
		scrollPane_quiz_Area.setBounds(111, 92, 490, 244);

		// panel_Main�� component�� �߰�

		panel_Main.add(label_Client1_Image);
		panel_Main.add(label_Client2_Image);
		panel_Main.add(label_Client3_Image);
		panel_Main.add(label_Client4_Image);
		panel_Main.add(label_Client1);
		panel_Main.add(label_Client2);
		panel_Main.add(label_Client3);
		panel_Main.add(label_Client4);

		panel_Main.add(btn_Ready);
		panel_Main.add(btn_Ready_Cancel);
		panel_Main.add(btn_Exit);
		panel_Main.add(btn_o);
		panel_Main.add(btn_x);
		panel_Main.add(scrollPane_chat_Area);
		panel_Main.add(chat_Field);
		panel_Main.add(scrollPane_quiz_Area);
		panel_Main.add(label_Timer);

		btn_Ready.addActionListener(this);
		btn_Exit.addActionListener(this);
		btn_o.addActionListener(this);
		btn_x.addActionListener(this);

		startChat();

	}

	public void bgm() {
		try {
			File file = new File("bgm\\ebgm.wav"); // https://online-audio-converter.com/ko/
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(file.toURL()));
			clip.start();
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			Thread.sleep(clip.getMicrosecondLength() / 1000);
		} catch (Exception e) {
		}
	}


	public void startChat() {
		String nickName = QuizLogin.nickName;// QuizLoginŬ������ �Ӽ�
		String ip = QuizLogin.ip;// QuizLoginŬ������ �Ӽ�
		try {
			Socket s = new Socket(ip, port);
			JOptionPane.showMessageDialog(null, "ȣ��Ʈ�� ���� �Ǿ����ϴ�.", "HOST CONNECT", JOptionPane.INFORMATION_MESSAGE, iic);
			Sender sender = new Sender(s, nickName);
			Listener listener = new Listener(s);
			new Thread(sender).start();
			new Thread(listener).start();
			chat_Field.addKeyListener(new Sender(s, nickName));
			btn_Ready.addActionListener(new Sender(s, nickName));
			btn_Ready_Cancel.addActionListener(new Sender(s, nickName));
			btn_o.addActionListener(new Sender(s, nickName));
			btn_x.addActionListener(new Sender(s, nickName));
		} catch (UnknownHostException uhe) {
			JOptionPane.showMessageDialog(null, "ȣ��Ʈ�� ã�� �� �����ϴ�", "HOST ERROR", JOptionPane.ERROR_MESSAGE, iic);
		} catch (IOException ie) {
			JOptionPane.showMessageDialog(null, "���� ���� ����!\n������ ���� �ִ� �� �����ϴ�.", "ERROR", JOptionPane.WARNING_MESSAGE, iic);
			System.exit(0);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btn_Exit) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {}
			btn_Exit.setEnabled(false);
			int c = JOptionPane.showConfirmDialog(null, "�����Ͻó���?", "���α׷�����", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, iic);
			if (c == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
			btn_Exit.setEnabled(true);
		}
		
	}

	class Sender extends Thread implements KeyListener, ActionListener {
		DataOutputStream dos;
		Socket s;
		String nickName;

		Sender(Socket s, String nickName) {
			this.s = s;
			try {
				this.nickName = nickName;
				dos = new DataOutputStream(this.s.getOutputStream());
			} catch (IOException io) {
			}
		}

		public void run() {
			try {
				dos.writeUTF(nickName);
				bgm();
			} catch (IOException io) {
			}
		}

		public void actionPerformed(ActionEvent e) {// btn_Ready, btn_o, btn_x
			Object obj = e.getSource();
			JButton btn = (JButton) obj;
			if (btn == btn_Ready) {
				try {
					dos.writeUTF("//Chat " + "[ " + nickName + " �� �غ� �Ϸ� ! ]");
					// Chat�� ����Ŭ������ filter �޼ҵ忡�� �˿�
					dos.flush();
					dos.writeUTF("//Ready");
					dos.flush();
					btn_Ready.setEnabled(false);
					btn_Ready_Cancel.setEnabled(true);
				} catch (IOException ie) {
				}
			} else if (btn == btn_Ready_Cancel) {
				try {
					dos.writeUTF("//Chat " + "[ " + nickName + " �� �غ� ��� ! ]");
					//// Chat�� ����Ŭ������ filter �޼ҵ忡�� �˿�
					dos.flush();
					dos.writeUTF("//Cance");
					dos.flush();
					btn_Ready_Cancel.setEnabled(false);
					btn_Ready.setEnabled(true);
				} catch (IOException ie2) {
				}
			} else if (btn == btn_o) {// ���׶�� ��ư Ŭ����
				answer_o = "O";
				btn_Exit.setEnabled(false);
				btn_o.setEnabled(false);// ��ư Ŭ���� ��Ȱ��ȭ
				if (answer_o.equalsIgnoreCase(question_Answer[1])) {// ���� o�� ���
					try {
						dos.writeUTF("//OKiii" + nickName);// ����� �����ø�
						dos.flush();
					} catch (IOException io) {
					}
				} else if (!answer_o.equalsIgnoreCase(question_Answer[1])) {// ���� o�� �ƴ� ���
					try {
						dos.writeUTF("//NOiii" + nickName);// Ʋ���� �����ȿø�
						dos.flush();
					} catch (IOException io) {
					}
				}
			} else if (btn == btn_x) {// ������ư Ŭ����
				answer_x = "X";
				btn_Exit.setEnabled(false);
				btn_x.setEnabled(false);// ��ư Ŭ���� ��Ȱ��ȭ
				if (answer_x.equalsIgnoreCase(question_Answer[1])) {// ���� x�� ���
					try {
						dos.writeUTF("//OKiii" + nickName);
						dos.flush();
					} catch (IOException io) {
					}
				} else if (!answer_x.equalsIgnoreCase(question_Answer[1])) {// ���� x�� �ƴ� ���
					try {
						dos.writeUTF("//NOiii" + nickName);
						dos.flush();
					} catch (IOException io) {
					}
				}
			}
		}

		public void keyReleased(KeyEvent e) {//chat_Field �� ä�� �Է½�
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				String chat = chat_Field.getText();
				chat_Field.setText("");
				try {
					dos.writeUTF("//Chat " + nickName + " : " + chat);
					dos.flush();
				} catch (IOException ie) {
				}
			}
		}

		public void keyTyped(KeyEvent e) {
		}

		public void keyPressed(KeyEvent e) {
		}
	}

	class Listener extends Thread {
		Socket s;
		DataInputStream dis;

		Listener(Socket s) {
			this.s = s;
			try {
				dis = new DataInputStream(this.s.getInputStream());
			} catch (IOException io) {
			}
		}

		public void run() {
			while (dis != null) {
				try {
					String msg = dis.readUTF();
					if (msg.startsWith("//CList")) {// CList�� ���۽�
						playerName = msg.substring(7, msg.indexOf(" "));
						playerScore = msg.substring(msg.indexOf(" ") + 1, msg.indexOf("#"));
						playerIdx = msg.substring(msg.indexOf("#") + 1);
						updateClientList();
					} else if (msg.startsWith("//Ready")) {
						chat_Area.append(msg + "\n");
					} else if (msg.startsWith("//Cancel")) {
						chat_Area.append(msg + "\n");
					} else if (msg.startsWith("//Start")) {
						gameStart = true;
						if(gameStart == true) {
						label_Timer.setVisible(true);// ���� ���� �� Ÿ�̸� Ȱ��ȭ
						}
						btn_Ready.setEnabled(false);
						btn_Ready_Cancel.setEnabled(false);
						btn_Exit.setEnabled(false);
						btn_o.setEnabled(true);// btn_o Ȱ��ȭ
						btn_x.setEnabled(true);// btn_x Ȱ��ȭ
					} else if (msg.startsWith("//Timer")) {// Ÿ�̸���ġ
						label_Timer.setText(msg.substring(7));
					} else if (msg.startsWith("//FQuiz")) {// ���� ù�ǽ�
						// msg(����, ��) split
						temp_quiz = msg.substring(7);// RQuiz���� rQuizStr(����Ŭ����) ����
						question_Answer = temp_quiz.split("@");// question_Answer[]
						quiz_Area.append(question_Answer[0] + "\n");// ������ ���´�.
						//System.out.println(question_Answer[0].toString()+" " + question_Answer[1].toString());
						String quiz_answer = question_Answer[0].toString()+" " + question_Answer[1].toString();
						//v_Result_Quizs.add(temp_quiz);// ù��°�������� ~������ ����.
						v_Result_Quizs.add(quiz_answer);// ù��°�������� ~������ ����.
					} else if (msg.startsWith("//AllNo")) {// ������� ����� ���� ���
						quiz_Area.append("������ ���� ����� ���� !!");
						btn_o.setEnabled(false);//��δ� ���� �������� o��ư ��Ȱ��ȭ
						btn_x.setEnabled(false);//��δ� ���� �������� x��ư ��Ȱ��ȭ
					} else if (msg.startsWith("//Check")) {// ������ ������ �̸��� ����.
						result_NickName = msg.substring(7);
						quiz_Area.append(result_NickName + "�� ���� !!");
						btn_o.setEnabled(false);// ������ ������ o��ư ��Ȱ��ȭ
						btn_x.setEnabled(false);// ������ ������ x��ư ��Ȱ��ȭ
					} else if (msg.startsWith("//RQuiz")) {// �ι�°����~������ //btn�� Ȱ��ȭ
						// msg(����, ��) split
						temp_quiz = msg.substring(7);// //RQuiz���� rQuizStr(����Ŭ����) ����
						question_Answer = temp_quiz.split("@");// question_Answer[]

						try {
							Thread.sleep(1000);//������ �̸� ������ �� �� �������� ������.
						} catch (InterruptedException ipe) {}

						quiz_Area.setText("");// sleep �� ȭ�� �ʱ�ȭ���ֱ�
						quiz_Area.append(question_Answer[0] + "\n");// ������ ���´�.
						
						String quiz_answer = question_Answer[0].toString()+" " + question_Answer[1].toString();
						//v_Result_Quizs.add(temp_quiz);//�ι�°����~ ���������� ��´�.
						v_Result_Quizs.add(quiz_answer);
						btn_o.setEnabled(true);// ���������� �ٽ� o��ư �������ְ� �Ѵ�.
						btn_x.setEnabled(true);// ���������� �ٽ� x��ư �������ְ� �Ѵ�.

					} else if (msg.startsWith("//GmEnd")) {// ���� �����&����������
						gameStart = false;
						btn_o.setEnabled(false);// btn_o ��Ȱ��ȭ
						btn_x.setEnabled(false);// btn_x ��Ȱ��ȭ
						quiz_Area.append("[ Ÿ�ӿ��� !! ]\n");
						if(gameStart == false) {
						label_Timer.setVisible(false);// ���� ���� �� Ÿ�̸� ��Ȱ��ȭ
						}
						quiz_Area.append("[ 3���� ���� ���� !! ]\n");
						try {
							quiz_Area.append("3\n");
							Thread.sleep(1000);
							quiz_Area.append("2\n");
							Thread.sleep(1000);
							quiz_Area.append("1\n");
						} catch (InterruptedException e) {}
						for (String tmp_Result : v_Result_Quizs) {
							quiz_Area.append(tmp_Result + "\n");
						}
							quiz_Area.append("5�ʰ� ���� ���� !!\n");
						try {
							Thread.sleep(5000);// ������� �ð�
						} catch (InterruptedException iep) {}
						v_Result_Quizs.clear();//����&���交�� �ʱ�ȭ
						quiz_Area.setText("");//  ����ȭ�� �ʱ�ȭ

						btn_Ready.setEnabled(true);//�������� �� ���� ��ư Ȱ��ȭ
						btn_Exit.setEnabled(true);//�������� �� ������ ��ư Ȱ��ȭ

					} else if (msg.startsWith("//QuEnd")) {// ���� �� Ǯ���� ��
						btn_o.setEnabled(false);// btn_o ��Ȱ��ȭ
						btn_x.setEnabled(false);// btn_x ��Ȱ��ȭ
						quiz_Area.setText("");
						quiz_Area.append("�־��� ������ �� Ǯ�����ϴ�!! \n");
						quiz_Area.append("�ð����� �� ������ ���Ϳ�!! \n");
					} else if(msg.startsWith("//Tstop")) {
						btn_Ready_Cancel.setEnabled(false); //���� �� 3,2,1 ī��Ʈ �� ��ҹ�ư ��Ȱ��ȭ
					} else if(msg.startsWith("/idOver")) {
						String usrId = msg.substring(7);
						if(usrId.equals(playerName)){
							errorOccurred();
						}
					} else {// �Ϲ�ä�� ���
						chat_Area.append(msg + "\n");
						scrollPane_chat_Area.getVerticalScrollBar()
								.setValue(scrollPane_chat_Area.getVerticalScrollBar().getMaximum());
					}
				} catch (IOException io) {
					errorOccurred();
				}
			}
		}

		public void errorOccurred(){
			chat_Area.append("[ �������� ������ ���������ϴ�. �г��� �ߺ�, ���� ���� �ʰ�, ���� �������� ��� ������ �źε˴ϴ�. ]\n[ 3�� �� ���α׷��� �����մϴ� .. ]");
			try {
				Thread.sleep(3000);
				System.exit(0);
			} catch (InterruptedException it) {
			}
		}

		public void updateClientList() {
			ImageIcon ii;
			if (Integer.parseInt(playerIdx) == 0) {
				// ("//ClientList" + keys[i] + " " + values[i] + "#" + i);
				ii = new ImageIcon("img\\u1.png");
				ii.getImage().flush();
				label_Client1_Image.setIcon(ii);
				label_Client1.setText("[" + playerName + " / " + "����: " + playerScore + "]");
				label_Client1.setBackground(Color.WHITE);
				label_Client1.setHorizontalAlignment(JLabel.CENTER);
				deleteClientList();
			} else if (Integer.parseInt(playerIdx) == 1) {
				ii = new ImageIcon("img\\u2.png");
				ii.getImage().flush();
				label_Client2_Image.setIcon(ii);
				label_Client2.setText("[" + playerName + " / " + "����: " + playerScore + "]");
				label_Client2.setBackground(Color.WHITE);
				label_Client2.setHorizontalAlignment(JLabel.CENTER);
				deleteClientList();
			} else if (Integer.parseInt(playerIdx) == 2) {
				ii = new ImageIcon("img\\u3.png");
				ii.getImage().flush();
				label_Client3_Image.setIcon(ii);
				label_Client3.setText("[" + playerName + " / " + "����: " + playerScore + "]");
				label_Client3.setBackground(Color.WHITE);
				label_Client3.setHorizontalAlignment(JLabel.CENTER);
				deleteClientList();
			} else if (Integer.parseInt(playerIdx) == 3) {
				ii = new ImageIcon("img\\u4.png");
				ii.getImage().flush();
				label_Client4_Image.setIcon(ii);
				label_Client4.setText("[" + playerName + " / " + "����: " + playerScore + "]");
				label_Client4.setBackground(Color.WHITE);
				label_Client4.setHorizontalAlignment(JLabel.CENTER);
				deleteClientList();
			}
		}

		public void deleteClientList() {
			ImageIcon ii2;
			ii2 = new ImageIcon("img\\p0.png");
			if (Integer.parseInt(playerIdx) == 0) {
				label_Client2_Image.setIcon(ii2);
				label_Client2.setText("[�г��� / ����]");
				label_Client3_Image.setIcon(ii2);
				label_Client3.setText("[�г��� / ����]");
				label_Client4_Image.setIcon(ii2);
				label_Client4.setText("[�г��� / ����]");
			} else if (Integer.parseInt(playerIdx) == 1) {
				label_Client3_Image.setIcon(ii2);
				label_Client3.setText("[�г��� / ����]");
				label_Client4_Image.setIcon(ii2);
				label_Client4.setText("[�г��� / ����]");
			} else if (Integer.parseInt(playerIdx) == 2) {
				label_Client4_Image.setIcon(ii2);
				label_Client4.setText("[�г��� / ����]");
			}
		}
	}
}