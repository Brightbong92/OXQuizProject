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
	int readyPlayer; // ���� �غ�� Ŭ���̾�Ʈ ī��Ʈ
	int score;
	boolean gameStart; // ���� ���� ����
	static LinkedHashMap<String, DataOutputStream> clientList = new LinkedHashMap<String, DataOutputStream>(); // Ŭ���̾�Ʈ�̸�,��Ʈ������
	LinkedHashMap<String, Integer> clientInfo = new LinkedHashMap<String, Integer>(); // Ŭ���̾�Ʈ chatId, ���� ����
	String word = "";
	String tmp_nickName;
	// int quizCnt = 1;
	int quizCnt;
	String rQuizStr;
	int rQuizIdx;
	Vector<String> v_quiz = new Vector<String>();// ������´�.
	// LinkedHashMap<String> map_tmp_nick = new LinkedHashMap<String>();//��ư��������� ��´�.
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
		btn_server_Open.addActionListener(this);// �������� ��ưŬ����
		btn_server_Close.addActionListener(this);// ����Ŭ���� ��ưŬ����
		textArea = new JTextArea();
		textArea.setEditable(false);// �ؽ�Ʈarea edit �Ұ�
		textArea.setFont(new Font("�����ٸ����", Font.PLAIN, 15));
		scrollPane = new JScrollPane(textArea);// ��ũ���ҿ��ٰ� �ؽ�Ʈ������ �������

		main_Layout = new BorderLayout();// ���� ����
		setLayout(main_Layout);// ���� ���η���

		p = new JPanel() {// ���� ����
			public void paintComponent(Graphics g) {
				g.drawImage(back.getImage(), 0, 0, null);
				setOpaque(false); // �׸��� ǥ���ϰ� ����,�����ϰ� ����
				super.paintComponent(g);
			}
		};
		p.setLayout(new GridLayout(1, 2));// �׸��� ���극��
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
						// �������ġ ���� �� �������� ��ư Ŭ���� bgm ����
						/*
						 * File file = new File("bgm\\start.wav"); //
						 * https://online-audio-converter.com/ko/ clip = AudioSystem.getClip();
						 * clip.open(AudioSystem.getAudioInputStream(file.toURL())); clip.start();
						 * Thread.sleep(2400); clip.stop();
						 */

						Collections.synchronizedMap(clientList);
						ss = new ServerSocket(port);
						textArea.append("[ ������ ���۵Ǿ����ϴ� ]" + "\n");
						btn_server_Open.setEnabled(false);
						btn_server_Close.setEnabled(true);
						while (true) {
							s = ss.accept();
							if ((clientList.size() + 1) > MAX_CLIENT || gameStart == true) { // ������ �ʰ��Ǿ��ų�, �������̶�� ���� ���� �ź�
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
			int c = JOptionPane.showConfirmDialog(null, "������ �����Ͻó���?", "��������", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,iic);
			if (c == JOptionPane.YES_OPTION) {
				try {
					ss.close();
					textArea.append("[ ������ ����Ǿ����ϴ� ]" + "\n");
					btn_server_Open.setEnabled(true);
					btn_server_Close.setEnabled(false);
				} catch (IOException ie) {
				}
			}
		}
	}// actionPerformed

	public void showSystemMsg(String msg) { // �ý��� �޽��� �� ��ɾ� �۽�
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
				if (!clientList.containsKey(clientName)) {// �ߺ� �г۹���
					clientList.put(clientName, dos);
					clientInfo.put(clientName, score);
				} else if (clientList.containsKey(clientName)) {
					showSystemMsg("/idOver"+clientName);
					s.close(); // �г��� �ߺ���, ���Ͽ���ź�
				}
				showSystemMsg("[ " + clientName + "���� �����ϼ̽��ϴ�. ]\n(���� ������ �� : " + clientList.size() + "�� / 4��)");
				textArea.append("[ ���� ������ ��� (�� " + clientList.size() + "�� ������) ]\n");
				Iterator<String> it1 = clientList.keySet().iterator();
				while (it1.hasNext())
					textArea.append(it1.next() + "\n");// ������id ����textarea�� ���

				setClientInfo();

				while (dis != null) {// ������ ��ºκ�
					String msg = dis.readUTF();
					filter(msg);// ��ɾ� ����
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
				showSystemMsg("[ " + clientName + "���� �����ϼ̽��ϴ�. ]\n(���� ������ �� : " + clientList.size() + "�� / 4��)");
				textArea.append("[ ���� ������ ��� (�� " + clientList.size() + "�� ������) ]\n");
				Iterator<String> it1 = clientList.keySet().iterator();
				while (it1.hasNext())
				textArea.append(it1.next() + "\n");
				setClientInfo();
				readyPlayer = 0;
				if(gameStart == true) {
					gameStart = false;
					showSystemMsg("//GmEnd");// Ŭ���̾�Ʈ �����, ��� ���� ����
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
				// map���� entrySet()�޼ҵ�� key, value ��δ� , keySet()�� key����
				keys[index] = mapEntry.getKey();
				values[index] = mapEntry.getValue();
				index++;
			}
			for (int i = 0; i < clientList.size(); i++) {
				showSystemMsg("//CList" + keys[i] + " " + values[i] + "#" + i);// CList ��� ���к���
			}
		}

		public void filter(String msg) {
			String filter_msg = msg; // ��ü���� �޴´� / ������ �޴´� ���� 330����
			String temp = filter_msg.substring(0, 7);
			// String temp = msg.substring(0, 7);//0~7���� //msg �� ¥����
			String filter_nickName = filter_msg.substring(7);
			if (temp.equals("//Chat ")) {// client btn_ready, btn_cancel ������ �˿�
				showSystemMsg(msg.substring(7));
			} else if (temp.equals("//Cance")) {
				readyPlayer--;
			} else if (temp.equals("//Ready")) {// ��ɾ� : Ŭ���̾�Ʈ �غ� ���� üũ
				readyPlayer++;
				if (readyPlayer >= 2 && readyPlayer == clientList.size()) {
					for (int i = 3; i > 0; i--) {
						try {
							showSystemMsg("[ ��� �����ڵ��� �غ�Ǿ����ϴ�. ]\n[ " + i + "�� �� ������ �����մϴ� .. ]");
							showSystemMsg("//Tstop");//Ŭ���̾�Ʈ ��ҹ�ư ��Ȱ��ȭ �϶�� ��� �޼���
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
						}
					}
					quizCnt = 1;// �������� ���� �ʱ�ȭ
					QuizCreate qc = new QuizCreate();
					qc.start();// ��������
					StopWatch tm = new StopWatch();
					tm.start();
					gameStart = true;
					showSystemMsg("//Start");
					switch_Cnt = 0;//���� ���۽� ���䴩�� Ƚ�� �ʱ�ȭ
				}
			} else if (temp.equals("//OKiii")) {// ���� �� ��� ++;
				tmp_nickName = filter_nickName;
				// clientInfo.put(tmp_nickName, clientInfo.get(tmp_nickName) + 1);//������ ����up
					if (v_quiz.size() == 0) {
						showSystemMsg("//QuEnd");
						textArea.append("�������� �־��� ������ ��Ǯ�����ϴ�.\n");
						//switch_Cnt=0;//���� �� Ǯ���� �� ���䴩�� Ƚ�� �ʱ�ȭ
					} else {
						switch_Cnt=0;//������ ��� ���䴩�� Ƚ�� �ʱ�ȭ
						textArea.append(tmp_nickName + "�� �������\n");// ������ ������ ����
						clientInfo.put(tmp_nickName, clientInfo.get(tmp_nickName) + 1);// ������ ����up
						showSystemMsg("//Check" + tmp_nickName);// ��������� �̸��� �������� ������.
						QuizCreate qc = new QuizCreate();
						qc.start();// ��������
					}
				
				// clientInfo.put(nickName, clientInfo.get(nickName) + 1);
			} else if (temp.equals("//NOiii")) {// ���� �� ��� ���� �ȿö�
				tmp_nickName = filter_nickName;
						switch_Cnt++;//���� ���� Ƚ��
					if (v_quiz.size() == 0) {
						showSystemMsg("//QuEnd");
						textArea.append("�������� �־��� ������ ��Ǯ�����ϴ�.\n");
						//switch_Cnt=0;//���� �� Ǯ���� �� ���䴩�� Ƚ�� �ʱ�ȭ
					}else if (clientList.size() == switch_Cnt) {// client�� == ���䴩��Ƚ��
						switch_Cnt = 0;//���� ���� Ƚ�� �ʱ�ȭ
						showSystemMsg("//AllNo");
						QuizCreate qc = new QuizCreate();
						qc.start();// ��������
						textArea.append(tmp_nickName + "�� �ڿ����\n");
					}
				}
		}
	}// GameManager

	class QuizCreate extends Thread {

		public void run() {
			r = new Random();
			if (quizCnt == 1) {// ù���Ͻ�
				try {
					fr = new FileReader("quizlist/QuizList.txt");
					brF = new BufferedReader(fr);
					while ((word = brF.readLine()) != null) {
						word = word.trim();
						if (word.length() != 0) {
							v_quiz.add(word);// �������� ����.
						}
					}
					rQuizIdx = r.nextInt(v_quiz.size());// ������ȣ �ϳ� �̴´�
					rQuizStr = v_quiz.get(rQuizIdx);// ���������� ��Ʈ������ ��´�
					textArea.append("����: " + rQuizStr + "\n");// ����ȭ�鿡 ������
					showSystemMsg("//FQuiz" + rQuizStr);// �������Թ��� ������

					v_quiz.remove(rQuizIdx);// ���� ���� ����
					quizCnt++;// ����Ƚ�� ++
				} catch (IOException ie) {
				}
			} else if (quizCnt == 2) {// �ι�°�������ý�
				rQuizIdx = r.nextInt(v_quiz.size());// ������ȣ �ϳ� �̴´�
				rQuizStr = v_quiz.get(rQuizIdx);// ���������� ��Ʈ������ ��´�
				textArea.append("����: " + rQuizStr + "\n");// ����ȭ�鿡 ������
				showSystemMsg("//RQuiz" + rQuizStr);// �������Թ��� ������
				v_quiz.remove(rQuizIdx);// ���� ���� ����
				quizCnt++;// ����Ƚ�� ++
			} else if (quizCnt == 3) {// ����°�������ý�
				rQuizIdx = r.nextInt(v_quiz.size());// ������ȣ �ϳ� �̴´�
				rQuizStr = v_quiz.get(rQuizIdx);// ���������� ��Ʈ������ ��´�
				textArea.append("����: " + rQuizStr + "\n");// ����ȭ�鿡 ������
				showSystemMsg("//RQuiz" + rQuizStr);// �������Թ��� ������
				v_quiz.remove(rQuizIdx);// ���� ���� ����
				quizCnt++;// ����Ƚ�� ++
			} else if (quizCnt >= 4) {// �׹�°���� �̻� ���� ���ý�
				rQuizIdx = r.nextInt(v_quiz.size());// ������ȣ �ϳ� �̴´�
				rQuizStr = v_quiz.get(rQuizIdx);// ���������� ��Ʈ������ ��´�
				textArea.append("����: " + rQuizStr + "\n");// ����ȭ�鿡 ������
				showSystemMsg("//RQuiz" + rQuizStr);// �������Թ��� ������
				v_quiz.remove(rQuizIdx);// ���� ���� ����
				quizCnt++;// ����Ƚ�� ++
			} else if (v_quiz.size() == 0) {
				showSystemMsg("������ �� Ǯ�����ϴ�");
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
						updateClientInfo();// Ÿ�ӿ��� �� Ŭ���̾�Ʈ���� ������Ʈ
						showSystemMsg("//GmEnd");
						switch_Cnt = 0;//���� ����� ���䴩�� Ƚ�� �ʱ�ȭ
						readyPlayer = 0;
						gameStart = false;// ������ �� �������� ����Ǿ���.
						break;
					} else if (readyPlayer == 0) {
						break;
					}
				}
			} catch (Exception e) {
			}
		}

		public void updateClientInfo() {// ���� ������ client���� ������Ʈ
			String[] keys = new String[clientInfo.size()];
			int[] values = new int[clientInfo.size()];
			int index = 0;
			for (Map.Entry<String, Integer> mapEntry : clientInfo.entrySet()) {
				// map���� entrySet()�޼ҵ�� key, value ��δ� , keySet()�� key����
				keys[index] = mapEntry.getKey();
				values[index] = mapEntry.getValue();
				index++;
			}
			for (int i = 0; i < clientList.size(); i++) {
				showSystemMsg("//CList" + keys[i] + " " + values[i] + "#" + i);// CList ��� ���к���
			}
		}

		String toTime(long time) {// Ÿ�̸� ���
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