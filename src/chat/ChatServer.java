package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {
	private static final String TAG = "ChatServer :";
	private ServerSocket serverSocket;

	// 연결된 클라이언트 클래스(소켓)을 담는 컬렉션
	private Vector<ClientInfo> vc;

	public ChatServer() {
		try {
			serverSocket = new ServerSocket(10000);
			vc = new Vector<>();
			System.out.println(TAG + "클라이언트 연결 대기중.....");
			// 메인 스레드의 역할
			while (true) {
				// 클라이언트 연결 대기 중
				Socket socket = serverSocket.accept();
				System.out.println(TAG + "새로운 클라이언트 접속 확인.");
				// 스레드 타겟 설정
				ClientInfo clientInfo = new ClientInfo(socket);
				clientInfo.start();
				// 해당 클라이언트를 백터에 저장
				vc.add(clientInfo);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 콤포지션해서 소켓을 가지고 있어야한다.
	class ClientInfo extends Thread {

		Socket socket;
		BufferedReader reader;
		// BufferedWriter와 다른점은 내려쓰기 함수를 지원. 객체 만들기가 편하다.
		PrintWriter writer;
		String id = null;

		public ClientInfo(Socket socket) {
			this.socket = socket;
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream());
			} catch (Exception e) {
				// 핵심적인 메세지만 알려준다.
				System.out.println("서버 연결 실패" + e.getMessage());
			}
		}

		// 역할 : 클라이언트로 부터 받은 메세지를 모든 클라이언트 한테 재전송
		// 읽어서 써주는 것은 직접 구현
		@Override
		public void run() {
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream(), true);
				String readerLine = null;
				while ((readerLine = reader.readLine()) != null) {
					String[] readerLineParsing = readerLine.split(":");
					if(id == null) {
						if (readerLineParsing[0].equals("ID")) {
							id = readerLineParsing[1];
							for (int i = 0; i < vc.size(); i++) {
								if (vc.get(i) == this) {
									vc.get(i).writer.println("SERVER:클라이언트님의 아이디는 [" + id + "] 입니다.");
									// vc.get(i).writer.println("SERVER:(ALL메세지)를 입력하면 메세지가 보내집니다.");

								}
							}
						} else {
							for (int i = 0; i < vc.size(); i++) {
								if (vc.get(i) == this) {
									vc.get(i).writer.println("SERVER:먼저 아이디를 정해주세요");
								}
							}
						} // end of else
					}
					
					else {
						if (readerLineParsing[0].equals("ALL")) {
							for (int i = 0; i < vc.size(); i++) {
									vc.get(i).writer.println("["+id+"] "+readerLineParsing[1]);
							}
						} else {
							for (int i = 0; i < vc.size(); i++) {
								if (vc.get(i) == this) {
									vc.get(i).writer.println("SERVER:ALL을 먼저 입력해주세요.");
								}
							}
						}
					}
					
				} // end of while
			} catch (

			Exception e) {
				e.printStackTrace();
			}

		} // end of run

	}

	public static void main(String[] args) {
		new ChatServer();
	}
}
