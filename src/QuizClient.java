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
	int tmpCnt;// 정답확인할때 변수
	String playerName, playerScore, playerIdx; // 클라이언트 이름,점수, 인덱스 관리
	boolean gameStart;
	Vector<String> v_Result_Quizs = new Vector<String>();// 나온문제를 담는다.
	String result_NickName;
	String temp_quiz;
	String question_Answer[], answer_o, answer_x; // 문제_답, 버튼답
	int check_Cnt;
	// question_Answer[0] = 문제
	// question_Answer[1] = 답

	void setUI() {
		// 기본 GUI 설정
		setFont(new Font("나눔바른고딕", Font.PLAIN, 13));
		setVisible(true);
		setResizable(false);
		setTitle("Quiz Client");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1300, 700);
		setLocationRelativeTo(null);
	}

	public QuizClient() {
		// 마우스 커서
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

		// 베이스 패널
		contentPane = new JPanel();
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

		panel_Main = new JPanel() {
			public void paintComponent(Graphics g) {
				g.drawImage(new ImageIcon("img\\배경2.png").getImage(), 0, 0, null);
				setOpaque(false); // 그림을 표시하게 설정,투명하게 조절
				super.paintComponent(g);
			}
		};
		contentPane.add(panel_Main);
		panel_Main.setLayout(null);

		// 접속자 목록
		label_Client1 = new JLabel("[닉네임 & 점수]");
		label_Client1.setBounds(90, 578, 150, 40);
		label_Client1.setFont(new Font("MapleStory", Font.BOLD, 14));
		label_Client1_Image = new JLabel(new ImageIcon("img\\p0.png"));
		label_Client1_Image.setBounds(62, 370, 180, 200);
		label_Client1.setBackground(Color.WHITE);
		label_Client1.setHorizontalAlignment(JLabel.CENTER);
		label_Client1.setOpaque(true);
		label_Client1_Image.setOpaque(false);

		label_Client2 = new JLabel("[닉네임 & 점수]");
		label_Client2.setBounds(275, 578, 150, 40);
		label_Client2.setFont(new Font("MapleStory", Font.BOLD, 14));
		label_Client2_Image = new JLabel(new ImageIcon("img\\p0.png"));
		label_Client2_Image.setBounds(260, 370, 180, 200);
		label_Client2.setBackground(Color.WHITE);
		label_Client2.setHorizontalAlignment(JLabel.CENTER);
		label_Client2.setOpaque(true);
		label_Client2_Image.setOpaque(false);

		label_Client3 = new JLabel("[닉네임 & 점수]");
		label_Client3.setBounds(475, 578, 150, 40);
		label_Client3.setFont(new Font("MapleStory", Font.BOLD, 14));
		label_Client3_Image = new JLabel(new ImageIcon("img\\p0.png"));
		label_Client3_Image.setBounds(460, 370, 180, 200);
		label_Client3.setBackground(Color.WHITE);
		label_Client3.setHorizontalAlignment(JLabel.CENTER);
		label_Client3.setOpaque(true);
		label_Client3_Image.setOpaque(false);

		label_Client4 = new JLabel("[닉네임 & 점수]");
		label_Client4.setBounds(675, 578, 150, 40);
		label_Client4.setFont(new Font("MapleStory", Font.BOLD, 14));
		label_Client4_Image = new JLabel(new ImageIcon("img\\p0.png"));
		label_Client4_Image.setBounds(660, 370, 180, 200);
		label_Client4.setBackground(Color.WHITE);
		label_Client4.setHorizontalAlignment(JLabel.CENTER);
		label_Client4.setOpaque(true);
		label_Client4_Image.setOpaque(false);

		// 우상단 버튼
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
		// ox버튼
		btn_o = new JButton(new ImageIcon("img\\O.png"));
		btn_o.setEnabled(false);// o비활성화
		btn_o.setBounds(675, 67, 200, 150);
		btn_o.setFocusPainted(false);
		btn_o.setBorderPainted(false);
		btn_o.setContentAreaFilled(false);

		btn_x = new JButton(new ImageIcon("img\\X.png"));
		btn_x.setEnabled(false);// x비활성화
		btn_x.setBounds(675, 217, 200, 150);
		btn_x.setFocusPainted(false);
		btn_x.setBorderPainted(false);
		btn_x.setContentAreaFilled(false);

		// 채팅창
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

		// 타이머
		label_Timer = new JLabel("00 : 00");
		label_Timer.setFont(new Font("Snap ITC", Font.BOLD, 30));
		label_Timer.setForeground(Color.WHITE);
		label_Timer.setBounds(420, 10, 160, 50);
		label_Timer.setVisible(false);

		// 문제 출제 영역
		quiz_Area = new JTextArea();
		quiz_Area.setFont(new Font("나눔바른고딕", Font.BOLD, 20));      
		quiz_Area.setForeground(Color.BLACK);
		quiz_Area.setBackground(new Color(245, 232, 226));
		quiz_Area.setEditable(false);

		scrollPane_quiz_Area = new JScrollPane(quiz_Area);
		scrollPane_quiz_Area.setBounds(111, 92, 490, 244);

		// panel_Main에 component들 추가

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
		String nickName = QuizLogin.nickName;// QuizLogin클래스의 속성
		String ip = QuizLogin.ip;// QuizLogin클래스의 속성
		try {
			Socket s = new Socket(ip, port);
			JOptionPane.showMessageDialog(null, "호스트에 접속 되었습니다.", "HOST CONNECT", JOptionPane.INFORMATION_MESSAGE, iic);
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
			JOptionPane.showMessageDialog(null, "호스트를 찾을 수 없습니다", "HOST ERROR", JOptionPane.ERROR_MESSAGE, iic);
		} catch (IOException ie) {
			JOptionPane.showMessageDialog(null, "서버 접속 실패!\n서버가 닫혀 있는 것 같습니다.", "ERROR", JOptionPane.WARNING_MESSAGE, iic);
			System.exit(0);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btn_Exit) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {}
			btn_Exit.setEnabled(false);
			int c = JOptionPane.showConfirmDialog(null, "종료하시나요?", "프로그램종료", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, iic);
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
					dos.writeUTF("//Chat " + "[ " + nickName + " 님 준비 완료 ! ]");
					// Chat은 서버클래스에 filter 메소드에서 검열
					dos.flush();
					dos.writeUTF("//Ready");
					dos.flush();
					btn_Ready.setEnabled(false);
					btn_Ready_Cancel.setEnabled(true);
				} catch (IOException ie) {
				}
			} else if (btn == btn_Ready_Cancel) {
				try {
					dos.writeUTF("//Chat " + "[ " + nickName + " 님 준비 취소 ! ]");
					//// Chat은 서버클래스에 filter 메소드에서 검열
					dos.flush();
					dos.writeUTF("//Cance");
					dos.flush();
					btn_Ready_Cancel.setEnabled(false);
					btn_Ready.setEnabled(true);
				} catch (IOException ie2) {
				}
			} else if (btn == btn_o) {// 동그라미 버튼 클릭시
				answer_o = "O";
				btn_Exit.setEnabled(false);
				btn_o.setEnabled(false);// 버튼 클릭시 비활성화
				if (answer_o.equalsIgnoreCase(question_Answer[1])) {// 답이 o일 경우
					try {
						dos.writeUTF("//OKiii" + nickName);// 정답시 점수올림
						dos.flush();
					} catch (IOException io) {
					}
				} else if (!answer_o.equalsIgnoreCase(question_Answer[1])) {// 답이 o가 아닐 경우
					try {
						dos.writeUTF("//NOiii" + nickName);// 틀릴시 점수안올림
						dos.flush();
					} catch (IOException io) {
					}
				}
			} else if (btn == btn_x) {// 엑스버튼 클릭시
				answer_x = "X";
				btn_Exit.setEnabled(false);
				btn_x.setEnabled(false);// 버튼 클릭시 비활성화
				if (answer_x.equalsIgnoreCase(question_Answer[1])) {// 답이 x일 경우
					try {
						dos.writeUTF("//OKiii" + nickName);
						dos.flush();
					} catch (IOException io) {
					}
				} else if (!answer_x.equalsIgnoreCase(question_Answer[1])) {// 답이 x가 아닐 경우
					try {
						dos.writeUTF("//NOiii" + nickName);
						dos.flush();
					} catch (IOException io) {
					}
				}
			}
		}

		public void keyReleased(KeyEvent e) {//chat_Field 에 채팅 입력시
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
					if (msg.startsWith("//CList")) {// CList로 시작시
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
						label_Timer.setVisible(true);// 게임 시작 시 타이머 활성화
						}
						btn_Ready.setEnabled(false);
						btn_Ready_Cancel.setEnabled(false);
						btn_Exit.setEnabled(false);
						btn_o.setEnabled(true);// btn_o 활성화
						btn_x.setEnabled(true);// btn_x 활성화
					} else if (msg.startsWith("//Timer")) {// 타이머위치
						label_Timer.setText(msg.substring(7));
					} else if (msg.startsWith("//FQuiz")) {// 문제 첫판시
						// msg(문제, 답) split
						temp_quiz = msg.substring(7);// RQuiz이후 rQuizStr(서버클래스) 추출
						question_Answer = temp_quiz.split("@");// question_Answer[]
						quiz_Area.append(question_Answer[0] + "\n");// 문제가 나온다.
						//System.out.println(question_Answer[0].toString()+" " + question_Answer[1].toString());
						String quiz_answer = question_Answer[0].toString()+" " + question_Answer[1].toString();
						//v_Result_Quizs.add(temp_quiz);// 첫번째문제부터 ~끝까지 담긴다.
						v_Result_Quizs.add(quiz_answer);// 첫번째문제부터 ~끝까지 담긴다.
					} else if (msg.startsWith("//AllNo")) {// 정답맞춘 사람이 없을 경우
						quiz_Area.append("정답을 맞춘 사람이 없음 !!");
						btn_o.setEnabled(false);//모두다 정답 못맞춘경우 o버튼 비활성화
						btn_x.setEnabled(false);//모두다 정답 못맞춘경우 x버튼 비활성화
					} else if (msg.startsWith("//Check")) {// 정답을 맞춘사람 이름을 띄운다.
						result_NickName = msg.substring(7);
						quiz_Area.append(result_NickName + "님 정답 !!");
						btn_o.setEnabled(false);// 정답자 나오면 o버튼 비활성화
						btn_x.setEnabled(false);// 정답자 나오면 x버튼 비활성화
					} else if (msg.startsWith("//RQuiz")) {// 두번째문제~끝문제 //btn들 활성화
						// msg(문제, 답) split
						temp_quiz = msg.substring(7);// //RQuiz이후 rQuizStr(서버클래스) 추출
						question_Answer = temp_quiz.split("@");// question_Answer[]

						try {
							Thread.sleep(1000);//정답자 이름 나오고 초 후 다음문제 나오게.
						} catch (InterruptedException ipe) {}

						quiz_Area.setText("");// sleep 후 화면 초기화해주기
						quiz_Area.append(question_Answer[0] + "\n");// 문제가 나온다.
						
						String quiz_answer = question_Answer[0].toString()+" " + question_Answer[1].toString();
						//v_Result_Quizs.add(temp_quiz);//두번째문제~ 끝문제까지 담는다.
						v_Result_Quizs.add(quiz_answer);
						btn_o.setEnabled(true);// 문제나오면 다시 o버튼 누를수있게 한다.
						btn_x.setEnabled(true);// 문제나오면 다시 x버튼 누를수있게 한다.

					} else if (msg.startsWith("//GmEnd")) {// 게임 종료시&유저나갈시
						gameStart = false;
						btn_o.setEnabled(false);// btn_o 비활성화
						btn_x.setEnabled(false);// btn_x 비활성화
						quiz_Area.append("[ 타임오버 !! ]\n");
						if(gameStart == false) {
						label_Timer.setVisible(false);// 게임 종료 시 타이머 비활성화
						}
						quiz_Area.append("[ 3초후 정답 공개 !! ]\n");
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
							quiz_Area.append("5초간 정답 공개 !!\n");
						try {
							Thread.sleep(5000);// 정답공개 시간
						} catch (InterruptedException iep) {}
						v_Result_Quizs.clear();//퀴즈&정답벡터 초기화
						quiz_Area.setText("");//  퀴즈화면 초기화

						btn_Ready.setEnabled(true);//게임종료 후 레디 버튼 활성화
						btn_Exit.setEnabled(true);//게임종료 후 나가기 버튼 활성화

					} else if (msg.startsWith("//QuEnd")) {// 퀴즈 다 풀었을 시
						btn_o.setEnabled(false);// btn_o 비활성화
						btn_x.setEnabled(false);// btn_x 비활성화
						quiz_Area.setText("");
						quiz_Area.append("주어진 문제를 다 풀었습니다!! \n");
						quiz_Area.append("시간종료 후 정답이 나와요!! \n");
					} else if(msg.startsWith("//Tstop")) {
						btn_Ready_Cancel.setEnabled(false); //레디 후 3,2,1 카운트 중 취소버튼 비활성화
					} else if(msg.startsWith("/idOver")) {
						String usrId = msg.substring(7);
						if(usrId.equals(playerName)){
							errorOccurred();
						}
					} else {// 일반채팅 출력
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
			chat_Area.append("[ 서버와의 연결이 끊어졌습니다. 닉네임 중복, 서버 정원 초과, 게임 진행중인 경우 연결이 거부됩니다. ]\n[ 3초 후 프로그램을 종료합니다 .. ]");
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
				label_Client1.setText("[" + playerName + " / " + "점수: " + playerScore + "]");
				label_Client1.setBackground(Color.WHITE);
				label_Client1.setHorizontalAlignment(JLabel.CENTER);
				deleteClientList();
			} else if (Integer.parseInt(playerIdx) == 1) {
				ii = new ImageIcon("img\\u2.png");
				ii.getImage().flush();
				label_Client2_Image.setIcon(ii);
				label_Client2.setText("[" + playerName + " / " + "점수: " + playerScore + "]");
				label_Client2.setBackground(Color.WHITE);
				label_Client2.setHorizontalAlignment(JLabel.CENTER);
				deleteClientList();
			} else if (Integer.parseInt(playerIdx) == 2) {
				ii = new ImageIcon("img\\u3.png");
				ii.getImage().flush();
				label_Client3_Image.setIcon(ii);
				label_Client3.setText("[" + playerName + " / " + "점수: " + playerScore + "]");
				label_Client3.setBackground(Color.WHITE);
				label_Client3.setHorizontalAlignment(JLabel.CENTER);
				deleteClientList();
			} else if (Integer.parseInt(playerIdx) == 3) {
				ii = new ImageIcon("img\\u4.png");
				ii.getImage().flush();
				label_Client4_Image.setIcon(ii);
				label_Client4.setText("[" + playerName + " / " + "점수: " + playerScore + "]");
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
				label_Client2.setText("[닉네임 / 점수]");
				label_Client3_Image.setIcon(ii2);
				label_Client3.setText("[닉네임 / 점수]");
				label_Client4_Image.setIcon(ii2);
				label_Client4.setText("[닉네임 / 점수]");
			} else if (Integer.parseInt(playerIdx) == 1) {
				label_Client3_Image.setIcon(ii2);
				label_Client3.setText("[닉네임 / 점수]");
				label_Client4_Image.setIcon(ii2);
				label_Client4.setText("[닉네임 / 점수]");
			} else if (Integer.parseInt(playerIdx) == 2) {
				label_Client4_Image.setIcon(ii2);
				label_Client4.setText("[닉네임 / 점수]");
			}
		}
	}
}