package chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.DataBufferDouble;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient extends JFrame {
	private static final String TAG = "ChatClient : ";
	private ChatClient chatClient = this;

	private static final int PORT = 10000;

	// 기본적인 구성 컴포넌트들
	private JButton btnConnect, btnSend;
	private JTextField tfHost, tfChat;
	private JTextArea taChatList;
	private ScrollPane scrollPane;

	// 텍스트 필드와 버튼을 담을 패널
	private JPanel topPanel, bottomPanel;

	// 통신하기위해 필요한 변수
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;

	// 채팅 내용을 파일에 저장하기 위한 변수
	private FileWriter saveBs;

	// 시간
	private SimpleDateFormat format;
	public Calendar time;
	private String timeLine;

	private String id = null;

	public ChatClient() {
		init();
		setting();
		batch();
		listener();
		setVisible(true);

	}

	private void listener() {
		btnConnect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				chatManual();
				connect();
			}
		});

		btnSend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {

				saveChat();
			}
		});
	}

	private void send() {
		String chat = tfChat.getText();
		
//			String[] chatParsing = chat.split(":");
//			if (chatParsing[0].equals("ID")) {
//				id = chatParsing[1];
//				taChatList.append("현재 당신의 ID는 [" + id + "] 입니다. \n");
//			} else {
//			}
			//taChatList.append("["+id+"] " + chat + "  ("+timeLine+")\n");				
			// 1. taChatList 뿌리기
			// append 글자를 덮어씌우지 않고 추가한다.

			// 2. 서버로 전송
			try {
				writer = new PrintWriter(socket.getOutputStream(), true);
				writer.println(chat);
			} catch (IOException e) {
				System.out.println("전송 실패");
			}

			// 3. chat 비우기
			tfChat.setText("");
		
	}

	private void connect() {
		String host = tfHost.getText();

		try {
			socket = new Socket(host, PORT);
			// 읽는 것은 추가적인 스레드가 해야한다.
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// 쓰는 건 메인 스레드가 대기하고 있으면 된다.
			writer = new PrintWriter(socket.getOutputStream(), true);
			ReaderThread rt = new ReaderThread();
			rt.start();
		} catch (Exception e1) {
			System.out.println(TAG + "서버 연결 에러 : " + e1.getMessage());
		}
	}

	private void saveChat() {
		try {
			saveBs = new FileWriter("/Users/shinyulpark/Desktop/Spring Frame Work/text2.txt");
			saveBs.write(taChatList.getText());
			System.out.println(TAG + "채팅 내용이 저장 되었습니다.");
			saveBs.close();

		} catch (Exception e) {
			System.out.println(TAG + "채팅 내용 저장 실패");
		}
	}

	private void chatManual() {
		taChatList.append("채팅방에 오신걸 환영합니다!.\n");
		taChatList.append("채팅을 하시기 전 [ID:닉네임]을 통해 아이디를 만드셔야 합니다.\n");
		taChatList.append("아이디를 만드신 후 [ALL:메세지]를 통해 메세지가 보내집니다.\n");
		taChatList.append("즐거운 채팅 되시길 바랍니다.\n");
	}
	private void batch() {
		topPanel.add(tfHost);
		topPanel.add(btnConnect);
		bottomPanel.add(tfChat);
		bottomPanel.add(btnSend);
		scrollPane.add(taChatList);

		add(topPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

	}

	private void setting() {
		setTitle("채팅 다대다 클라이언트");
		setSize(500, 350);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 사진을 가운대로 세팅한다.
		setLocationRelativeTo(null);
		// 채팅방 색을 지정
		taChatList.setBackground(Color.ORANGE);
		// 채팅방 글자색을 지정
		taChatList.setForeground(Color.BLUE);
		timeLine = format.format(time.getTime());

	}

	private void init() {
		btnConnect = new JButton("connect");
		btnSend = new JButton("send");
		// 20자 까지 작성 가능 column : 20
		tfHost = new JTextField("127.0.0.1", 20);
		tfChat = new JTextField(20);
		// rows column 가로30 높이10
		taChatList = new JTextArea(10, 30);
		scrollPane = new ScrollPane();
		topPanel = new JPanel();
		bottomPanel = new JPanel();

		// 시간
		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		time = Calendar.getInstance();

	}

	class ReaderThread extends Thread {
		// while을 돌면서 서버로 부터 메세지를 받아서 taChatList에 뿌리기
		// 직접해야한다.

		@Override
		public void run() {
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String readerChat = null;
				while ((readerChat = reader.readLine()) != null) {
					String[] readerChatParsing = readerChat.split(":");
					if(readerChatParsing[0].equals("SERVER")) {
						taChatList.append("[서버메세지] "  + readerChatParsing[1] + "  ("+timeLine+") \n");
					} else {
					//taChatList.append("["+id+"] " + readerChat + "  ("+timeLine+") \n");
					taChatList.append(readerChat+"  ("+timeLine+") \n");
					}
				} // end of while
			} catch (Exception e) {
				System.out.println(TAG + "읽어오기 실패");
			}

		}
	}

	public static void main(String[] args) {
		new ChatClient();
	}
}
