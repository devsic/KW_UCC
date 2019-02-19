package server;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class SimpleServer {
	 public static void main(String[] args) throws Exception {
		    System.out.println("서버 실행 중...");
		   
		    ServerSocket serverSocket = new ServerSocket(12345);
		  		    
		    Socket socket = serverSocket.accept();
		    System.out.println("=> 클라이언트 연결 승인!");
		   
		    InputStream in = socket.getInputStream();
		    Scanner in1=new Scanner(in);
		    String str=in1.nextLine();
		    System.out.println(str);
		    in.close();
		    in1.close();
		    socket.close();
		    serverSocket.close();
	 }
		    
}
