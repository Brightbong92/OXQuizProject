import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javafx.scene.media.*;
import javax.swing.border.*;
import javafx.embed.swing.*;

public class QuizServer extends JFrame implements ActionListener {
	Cursor cursor;
	Image img;
	ImageIcon back;
	Container cp;
	BorderLayout main_Layout;
	JPanel p;
	JButton btn_server_Open, btn_server_Close;
	JTextArea textArea;
	JScrollPane scrollPane;
	Clip clip;
	ImageIcon iic = new ImageIcon("img\\msm.png");
	ServerSocket ss;
	Socket s;
	int port = 5000;
	int switch_Cnt;

	public static final int MAX_CLIENT = 4;
	int readyPlayer; // 게임 준비된 클라이언트 카운트
	int score;
	boolean gameStart; // 게임 시작 상태
	static LinkedHashMap<String, DataOutputStream> clientList = new LinkedHashMap<String, DataOutputStream>(); // 클라이언트이름,스트림관리
	LinkedHashMap<String, Integer> clientInfo = new LinkedHashMap<String, Integer>(); // 클라이언트 chatId, 점수 관리
	String word = "";
	String tmp_nickName;
	// int quizCnt = 1;
	int quizCnt;
	String rQuizStr;
	int rQuizIdx;
	Vector<String> v_quiz = new Vector<String>();// 문제담는다.
	// LinkedHashMap<String> map_tmp_nick = new LinkedHashMap<String>();//버튼누른사람을 담는다.
	Random r;
	FileReader fr;
	BufferedReader brF;

	public QuizServer() {
		super("User Defined Cursor");

		Toolkit tk = Toolkit.getDefaultToolkit();
		img = tk.getImage("img\\m8.png");
		Point point = new Point(0, 0);
		cursor = tk.createCustomCursor(img, point, "maple");
		setCursor(cursor);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(10, 10);
		setVisible(false);
	}

	void init() {
		cp = getContentPane();

		loadImg();
		btn_server_Open = new JButton(new ImageIcon("img\\op.png"));
		btn_server_Close = new JButton(new ImageIcon("img\\cl.png"));
		btn_server_Close.setEnabled(false);
		btn_server_Open.setFocusPainted(false);
		btn_server_Open.setBorderPainted(false);
		btn_server_Open.setContentAreaFilled(false);
		btn_server_Close.setFocusPainted(false);
		btn_server_Close.setBorderPainted(false);
		btn_server_Close.setContentAreaFilled(false);
		btn_server_Open.addActionListener(this);// 서버오픈 버튼클릭시
		btn_server_Close.addActionListener(this);// 서버클로즈 버튼클릭시
		textArea = new JTextArea();
		textArea.setEditable(false);// 텍스트area edit 불가
		textArea.setFont(new Font("나눔바른고딕", Font.PLAIN, 15));
		scrollPane = new JScrollPane(textArea);// 스크롤팬에다가 텍스트에리어 집어넣음

		main_Layout = new BorderLayout();// 메인 레아
		setLayout(main_Layout);// 보더 메인레아

		p = new JPanel() {// 서브 레아
			public void paintComponent(Graphics g) {
				g.drawImage(back.getImage(), 0, 0, null);
				setOpaque(false); // 그림을 표시하게 설정,투명하게 조절
				super.paintComponent(g);
			}
		};
		p.setLayout(new GridLayout(1, 2));// 그리드 서브레아
		p.add(btn_server_Open);
		p.add(btn_server_Close);
		// p.setBackground(Color.BLACK);
		cp.add(p, BorderLayout.NORTH);
		cp.add(scrollPane, BorderLayout.CENTER);

		setUI();

	}

	void loadImg() {
		try {
			back = new ImageIcon(ImageIO.read(new File("img/lo.png")));
		} catch (IOException ie) {
		}
	}

	void setUI() {
		setTitle("Quiz Server");
		setSize(450, 700);
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == btn_server_Open) {
			new Thread() {
				public void run() {
					try {
						// 오디오장치 연결 시 서버오픈 버튼 클릭시 bgm 실행
						/*
						 * File file = new File("bgm\\start.wav"); //
						 * https://online-audio-converter.com/ko/ clip = AudioSystem.getClip();
						 * clip.open(AudioSystem.getAudioInputStream(file.toURL())); clip.start();
						 * Thread.sleep(2400); clip.stop();
						 */

						Collections.synchronizedMap(clientList);
						ss = new ServerSocket(port);
						textArea.append("[ 서버가 시작되었습니다 ]" + "\n");
						btn_server_Open.setEnabled(false);
						btn_server_Close.setEnabled(true);
						while (true) {
							s = ss.accept();
							if ((clientList.size() + 1) > MAX_CLIENT || gameStart == true) { // 정원이 초과되었거나, 게임중이라면 소켓 연결 거부
								s.close();
							} else {
								Thread gm = new GameManager(s);
								gm.start();
							}
						}
					} catch (Exception io) {
					}
				}
			}.start();
		} else if (e.getSource() == btn_server_Close) {
			int c = JOptionPane.showConfirmDialog(null, "서버를 종료하시나요?", "서버종료", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,iic);
			if (c == JOptionPane.YES_OPTION) {
				try {
					ss.close();
					textArea.append("[ 서버가 종료되었습니다 ]" + "\n");
					btn_server_Open.setEnabled(true);
					btn_server_Close.setEnabled(false);
				} catch (IOException ie) {
				}
			}
		}
	}// actionPerformed

	public void showSystemMsg(String msg) { // 시스템 메시지 및 명령어 송신
		Iterator<String> it = clientList.keySet().iterator();
		while (it.hasNext()) {
			try {
				DataOutputStream dos = clientList.get(it.next());
				dos.writeUTF(msg);
				dos.flush();
			} catch (IOException io) {
			}
		}
	}

	public class GameManager extends Thread {
		Socket s;
		InputStream is;
		OutputStream os;
		DataInputStream dis;
		DataOutputStream dos;

		public GameManager(Socket s) {
			this.s = s;
			try {
				is = s.getInputStream();
				dis = new DataInputStream(is);
				os = s.getOutputStream();
				dos = new DataOutputStream(os);
			} catch (IOException ie) {
			}
		}

		public void run() {
			String clientName = "";
			try {
				clientName = dis.readUTF();
				if (!clientList.containsKey(clientName)) {// 중복 닉넴방지
					clientList.put(clientName, dos);
					clientInfo.put(clientName, score);
				} else if (clientList.containsKey(clientName)) {
					showSystemMsg("/idOver"+clientName);
					s.close(); // 닉네임 중복시, 소켓연결거부
				}
				showSystemMsg("[ " + clientName + "님이 입장하셨습니다. ]\n(현재 접속자 수 : " + clientList.size() + "명 / 4명)");
				textArea.append("[ 현재 접속자 명단 (총 " + clientList.size() + "명 접속중) ]\n");
				Iterator<String> it1 = clientList.keySet().iterator();
				while (it1.hasNext())
					textArea.append(it1.next() + "\n");// 입장자id 서버textarea에 출력

				setClientInfo();

				while (dis != null) {// 서버가 듣는부분
					String msg = dis.readUTF();
					filter(msg);// 명령어 필터
				}
			} catch (IOException ie) {
				clientList.remove(clientName);
				clientInfo.remove(clientName);
				closeAll();
				if (clientList.isEmpty() == true) {
					try {
						ss.close();
						System.exit(0);
					} catch (IOException io) {
					}
				}
				showSystemMsg("[ " + clientName + "님이 퇴장하셨습니다. ]\n(현재 접속자 수 : " + clientList.size() + "명 / 4명)");
				textArea.append("[ 현재 접속자 명단 (총 " + clientList.size() + "명 접속중) ]\n");
				Iterator<String> it1 = clientList.keySet().iterator();
				while (it1.hasNext())
				textArea.append(it1.next() + "\n");
				setClientInfo();
				readyPlayer = 0;
				if(gameStart == true) {
					gameStart = false;
					showSystemMsg("//GmEnd");// 클라이언트 퇴장시, 즉시 라운드 종료
				}

			}
		}

		public void closeAll() {
			try {
				if (dos != null)
					dos.close();
				if (dis != null)
					dis.close();
				if (s != null)
					s.close();
			} catch (IOException ie) {
			}
		}

		public void setClientInfo() {
			String[] keys = new String[clientInfo.size()];
			int[] values = new int[clientInfo.size()];
			int index = 0;
			for (Map.Entry<String, Integer> mapEntry : clientInfo.entrySet()) {
				// map에서 entrySet()메소드는 key, value 모두다 , keySet()은 key값만
				keys[index] = mapEntry.getKey();
				values[index] = mapEntry.getValue();
				index++;
			}
			for (int i = 0; i < clientList.size(); i++) {
				showSystemMsg("//CList" + keys[i] + " " + values[i] + "#" + i);// CList 라는 구분보냄
			}
		}

		public void filter(String msg) {
			String filter_msg = msg; // 전체줄을 받는다 / 문제도 받는다 유저 330줄쯤
			String temp = filter_msg.substring(0, 7);
			// String temp = msg.substring(0, 7);//0~7까지 //msg 를 짜른다
			String filter_nickName = filter_msg.substring(7);
			if (temp.equals("//Chat ")) {// client btn_ready, btn_cancel 누를시 검열
				showSystemMsg(msg.substring(7));
			} else if (temp.equals("//Cance")) {
				readyPlayer--;
			} else if (temp.equals("//Ready")) {// 명령어 : 클라이언트 준비 상태 체크
				readyPlayer++;
				if (readyPlayer >= 2 && readyPlayer == clientList.size()) {
					for (int i = 3; i > 0; i--) {
						try {
							showSystemMsg("[ 모든 참여자들이 준비되었습니다. ]\n[ " + i + "초 후 게임을 시작합니다 .. ]");
							showSystemMsg("//Tstop");//클라이언트 취소버튼 비활성화 하라는 명령 메세지
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
						}
					}
					quizCnt = 1;// 문제생성 변수 초기화
					QuizCreate qc = new QuizCreate();
					qc.start();// 문제출제
					StopWatch tm = new StopWatch();
					tm.start();
					gameStart = true;
					showSystemMsg("//Start");
					switch_Cnt = 0;//게임 시작시 오답누른 횟수 초기화
				}
			} else if (temp.equals("//OKiii")) {// 정답 일 경우 ++;
				tmp_nickName = filter_nickName;
				// clientInfo.put(tmp_nickName, clientInfo.get(tmp_nickName) + 1);//정답자 점수up
					if (v_quiz.size() == 0) {
						showSystemMsg("//QuEnd");
						textArea.append("유저들이 주어진 문제를 다풀었습니다.\n");
						//switch_Cnt=0;//문제 다 풀었을 시 오답누른 횟수 초기화
					} else {
						switch_Cnt=0;//정답일 경우 오답누른 횟수 초기화
						textArea.append(tmp_nickName + "님 ★정답★\n");// 서버에 정답자 띄운다
						clientInfo.put(tmp_nickName, clientInfo.get(tmp_nickName) + 1);// 정답자 점수up
						showSystemMsg("//Check" + tmp_nickName);// 정답맞춘사람 이름을 소켓으로 보낸다.
						QuizCreate qc = new QuizCreate();
						qc.start();// 문제출제
					}
				
				// clientInfo.put(nickName, clientInfo.get(nickName) + 1);
			} else if (temp.equals("//NOiii")) {// 오답 일 경우 점수 안올라감
				tmp_nickName = filter_nickName;
						switch_Cnt++;//오답 누른 횟수
					if (v_quiz.size() == 0) {
						showSystemMsg("//QuEnd");
						textArea.append("유저들이 주어진 문제를 다풀었습니다.\n");
						//switch_Cnt=0;//문제 다 풀었을 시 오답누른 횟수 초기화
					}else if (clientList.size() == switch_Cnt) {// client수 == 오답누른횟수
						switch_Cnt = 0;//오답 누른 횟수 초기화
						showSystemMsg("//AllNo");
						QuizCreate qc = new QuizCreate();
						qc.start();// 문제출제
						textArea.append(tmp_nickName + "님 ★오답★\n");
					}
				}
		}
	}// GameManager

	class QuizCreate extends Thread {

		public void run() {
			r = new Random();
			if (quizCnt == 1) {// 첫판일시
				try {
					fr = new FileReader("quizlist/QuizList.txt");
					brF = new BufferedReader(fr);
					while ((word = brF.readLine()) != null) {
						word = word.trim();
						if (word.length() != 0) {
							v_quiz.add(word);// 문제들이 담긴다.
						}
					}
					rQuizIdx = r.nextInt(v_quiz.size());// 문제번호 하나 뽑는다
					rQuizStr = v_quiz.get(rQuizIdx);// 뽑은문제를 스트링으로 담는다
					textArea.append("문제: " + rQuizStr + "\n");// 서버화면에 보여줌
					showSystemMsg("//FQuiz" + rQuizStr);// 유저에게문제 보내기

					v_quiz.remove(rQuizIdx);// 나온 문제 제거
					quizCnt++;// 퀴즈횟수 ++
				} catch (IOException ie) {
				}
			} else if (quizCnt == 2) {// 두번째문제나올시
				rQuizIdx = r.nextInt(v_quiz.size());// 문제번호 하나 뽑는다
				rQuizStr = v_quiz.get(rQuizIdx);// 뽑은문제를 스트링으로 담는다
				textArea.append("문제: " + rQuizStr + "\n");// 서버화면에 보여줌
				showSystemMsg("//RQuiz" + rQuizStr);// 유저에게문제 보내기
				v_quiz.remove(rQuizIdx);// 나온 문제 제거
				quizCnt++;// 퀴즈횟수 ++
			} else if (quizCnt == 3) {// 세번째문제나올시
				rQuizIdx = r.nextInt(v_quiz.size());// 문제번호 하나 뽑는다
				rQuizStr = v_quiz.get(rQuizIdx);// 뽑은문제를 스트링으로 담는다
				textArea.append("문제: " + rQuizStr + "\n");// 서버화면에 보여줌
				showSystemMsg("//RQuiz" + rQuizStr);// 유저에게문제 보내기
				v_quiz.remove(rQuizIdx);// 나온 문제 제거
				quizCnt++;// 퀴즈횟수 ++
			} else if (quizCnt >= 4) {// 네번째문제 이상 부터 나올시
				rQuizIdx = r.nextInt(v_quiz.size());// 문제번호 하나 뽑는다
				rQuizStr = v_quiz.get(rQuizIdx);// 뽑은문제를 스트링으로 담는다
				textArea.append("문제: " + rQuizStr + "\n");// 서버화면에 보여줌
				showSystemMsg("//RQuiz" + rQuizStr);// 유저에게문제 보내기
				v_quiz.remove(rQuizIdx);// 나온 문제 제거
				quizCnt++;// 퀴즈횟수 ++
			} else if (v_quiz.size() == 0) {
				showSystemMsg("문제를 다 풀었습니다");
			}
		}
	}

	class StopWatch extends Thread {
		long preTime = System.currentTimeMillis();

		public void run() {
			try {
				while (gameStart == true) {
					// sleep(10);
					sleep(10);
					long time = System.currentTimeMillis() - preTime;
					showSystemMsg("//Timer" + (toTime(time)));
					if (toTime(time).equals("00 : 00")) {
						updateClientInfo();// 타임오버 후 클라이언트정보 업데이트
						showSystemMsg("//GmEnd");
						switch_Cnt = 0;//게임 종료시 오답누른 횟수 초기화
						readyPlayer = 0;
						gameStart = false;// 문제가 다 나왔을시 실행되야함.
						break;
					} else if (readyPlayer == 0) {
						break;
					}
				}
			} catch (Exception e) {
			}
		}

		public void updateClientInfo() {// 정답 맞춘후 client정보 업데이트
			String[] keys = new String[clientInfo.size()];
			int[] values = new int[clientInfo.size()];
			int index = 0;
			for (Map.Entry<String, Integer> mapEntry : clientInfo.entrySet()) {
				// map에서 entrySet()메소드는 key, value 모두다 , keySet()은 key값만
				keys[index] = mapEntry.getKey();
				values[index] = mapEntry.getValue();
				index++;
			}
			for (int i = 0; i < clientList.size(); i++) {
				showSystemMsg("//CList" + keys[i] + " " + values[i] + "#" + i);// CList 라는 구분보냄
			}
		}

		String toTime(long time) {// 타이머 계산
			// int m = (int)(3-(time / 1000.0 / 60.0));
			// int s = (int)(60-(time % (1000.0 * 60) / 1000.0));
			// return String.format("%02d : %02d", m, s);
			// int m = (int)(1-(time / 1000.0 / 60.0));
			int m = 0;
			int s = (int) (30 - (time % (1000.0 * 60) / 1000.0));
			return String.format("%02d : %02d", m, s);
		}
	}

	public static void main(String[] args) {
		QuizServer qs = new QuizServer();
		qs.init();
	}
}