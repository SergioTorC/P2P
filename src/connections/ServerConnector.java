package connections;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import controller.P2P;

public class ServerConnector {

	private final int serverPort;
	private final P2P p2p;
	private boolean runState;
	
	public ServerConnector(P2P p2p, int serverPort) {
		this.p2p = p2p;
		this.serverPort = serverPort;
		runState = true;
		new Thread(this::run).start();
	}

	//Metodos de ejecucion y control


	private void run() {
		try(ServerSocket serverSocket = new ServerSocket(serverPort)){
			System.out.println("Servidor: Activando puerto para servidor -  " + serverPort);
			while(runState) {
				if(!serverSocket.isClosed()) {
					try {
						Socket socket = serverSocket.accept();
						System.out.println("Servidor: Conexión establecida con " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
						p2p.addConnection(socket);
					} catch(IOException e) {
						System.out.println("Servidor: Conexión con el socket del cliente");
					}
				}
			}
			System.err.println("Parando ServerConnector");
		} catch (IOException e) {
			e.printStackTrace();
			runState = false;
		}
	}

	public void stopServerConnection() {
		System.err.println("Parando servidor");
		runState = false;
	}

}
